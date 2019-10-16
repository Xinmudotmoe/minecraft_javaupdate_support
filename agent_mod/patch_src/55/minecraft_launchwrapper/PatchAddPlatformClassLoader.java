package moe.xinmu.minecraft.patcher;

import moe.xinmu.minecraft_agent.annotation.TargetClass;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

@TargetClass("net.minecraft.launchwrapper.LaunchClassLoader")
public class PatchAddPlatformClassLoader implements ClassFileTransformer {
	private static ClassLoader cl = ClassLoader.getSystemClassLoader();

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
							ProtectionDomain protectionDomain, byte[] classfileBuffer) {
		try {
			if (cl.loadClass("javax.script.ScriptEngineManager").getClassLoader() != ClassLoader.getPlatformClassLoader())
				return null;
		} catch (ClassNotFoundException ignored) {
		}
		if (loader == cl) {
			ClassReader cr = new ClassReader(classfileBuffer);
			ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
			cr.accept(new ClassVisitor(Opcodes.ASM5, cw) {
				@Override
				public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
					MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
					if (name.equals("<init>")) {
						return new MethodVisitor(Opcodes.ASM5, new GeneratorAdapter(mv, access, name, desc)) {
							boolean flag = true;

							@Override
							public void visitInsn(int opcode) {
								if (opcode == Opcodes.ACONST_NULL && flag) {
									GeneratorAdapter ga = (GeneratorAdapter) this.mv;
									Type cll = Type.getType(ClassLoader.class);
									ga.invokeStatic(cll, new Method(
											"getPlatformClassLoader", cll, new Type[0]));
								} else
									super.visitInsn(opcode);
							}

							@Override
							public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
								if (name.equals("<init>"))
									flag = false;
								super.visitMethodInsn(opcode, owner, name, desc, itf);
							}
						};
					}
					return mv;
				}
			}, ClassReader.EXPAND_FRAMES);
			return cw.toByteArray();
		}
		return null;
	}
}
