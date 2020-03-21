package moe.xinmu.minecraft.patcher.patch1122;

import moe.xinmu.minecraft_agent.DeobfUtils;
import moe.xinmu.minecraft_agent.version.MinecraftVersion;
import org.objectweb.asm.*;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.*;

public class P1122ObjectRegistry implements ClassFileTransformer {
	public static P1122ObjectRegistry INSTANCE;
	public Map<String, Set<String>> maper = Collections.synchronizedMap(new HashMap<>());
	public Set<String> modifys = Collections.synchronizedSet(new HashSet<>());
	boolean isStop = false;

	public void stop() {
		isStop = true;
	}

	public P1122ObjectRegistry() {
		if (INSTANCE != null) {
			throw new Error();
		}
		INSTANCE = this;
		final List<String> s = new ArrayList<>();
		if (MinecraftVersion.checkVersion(MinecraftVersion.V1_12_2)) {
			try {
				ClassReader cr = new ClassReader(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResourceAsStream(
						"net.minecraftforge.registries.ObjectHolderRegistry".replace(".", "/").concat(".class"))));
				cr.accept(new ClassVisitor(Opcodes.ASM5, null) {
					@Override
					public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
						if (name.equals("findObjectHolders")) {
							return new MethodVisitor(Opcodes.ASM5, null) {
								@Override
								public void visitLdcInsn(Object cst) {
									if (cst instanceof String)
										s.add((String) cst);
								}
							};
						}
						return null;
					}
				}, ClassReader.EXPAND_FRAMES);
			} catch (IOException | NullPointerException e) {
				e.printStackTrace();
			}
		}
		s.stream().filter(ss -> ss.startsWith("net.minecraft")).forEach(modifys::add);
		String[] sss = modifys.stream().map(ss -> ss.replace(".", "/")).toArray(String[]::new);
		modifys.clear();
		modifys.addAll(Arrays.asList(sss));
		Arrays.stream(modifys.toArray(String[]::new))
				.map(DeobfUtils::checkClassDeobfString).forEach(modifys::add);

	}

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
							ProtectionDomain protectionDomain, byte[] classfileBuffer) {
		if (className.contains("moe/xinmu/minecraft/patcher/patch1122")) {
			return classfileBuffer;
		}
		if (className.contains("org/objectweb/asm")) {
			return classfileBuffer;
		}
		return transform(classfileBuffer);
	}

	public byte[] transform(byte[] bytes) {
		if (isStop) {
			return bytes;
		}
		if (bytes == null) {
			return null;
		}
		ClassReader cr = new ClassReader(bytes);
		if (Modifier.isInterface(cr.getAccess())) {
			return bytes;
		}
		ClassWriter cw = new ClassWriter(cr, 0);
		Set<String> change_access = Collections.synchronizedSet(new HashSet<>());
		boolean fullcheck = modifys.contains(cr.getClassName());
		cr.accept(new ClassVisitor(Opcodes.ASM5, null) {
			@Override
			public FieldVisitor visitField(final int access, final String name, String desc,
										   String signature, Object value) {
				if (fullcheck && Modifier.isPublic(access) && Modifier.isStatic(access))
					change_access.add(name);
				return new FieldVisitor(Opcodes.ASM5) {
					public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
						switch (desc) {
							case "Lnet/minecraftforge/registries/ObjectHolder;"://todo
							case "Lnet/minecraftforge/fml/common/registry/GameRegistry$ObjectHolder;":
								change_access.add(name);
						}
						return super.visitAnnotation(desc, visible);
					}
				};
			}
		}, 0);
		if (change_access.size() == 0) {
			return bytes;
		}
		cr.accept(new ClassVisitor(Opcodes.ASM5, cw) {
			@Override
			public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
				if (change_access.contains(name))
					access = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_VOLATILE;
				return super.visitField(access, name, desc, signature, value);
			}
		}, 0);
		if (!fullcheck) {
			maper.put(DeobfUtils.checkClassReDeobfString(cr.getClassName()).replace("/", "."), change_access);
		}
		return cw.toByteArray();
	}
}
