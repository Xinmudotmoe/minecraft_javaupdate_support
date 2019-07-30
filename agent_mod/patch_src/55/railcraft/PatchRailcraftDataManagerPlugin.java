package moe.xinmu.minecraft.patcher;

import moe.xinmu.minecraft_agent.annotation.TargetClass;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.io.PrintStream;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

@TargetClass("mods.railcraft.common.plugins.forge.DataManagerPlugin")
public class PatchRailcraftDataManagerPlugin implements ClassFileTransformer {
	private final static PrintStream err = System.err;

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
		final ClassReader cr = new ClassReader(classfileBuffer);
		final ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
		cr.accept(new ClassVisitor(Opcodes.ASM5, cw) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				final GeneratorAdapter ga = new GeneratorAdapter(mv, access, name, desc);
				return new MethodVisitor(Opcodes.ASM5, ga) {
					@Override
					public void visitMethodInsn(int opcode, String owner, String name1, String desc1, boolean itf) {
						if (opcode == Opcodes.INVOKESTATIC && owner.equals("sun/reflect/Reflection") && name1.equals(
								"getCallerClass")) {
							owner = cr.getClassName();
							GeneratorAdapter gaa =
									new GeneratorAdapter(cw.visitMethod(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, name1, desc1, null
											, null), Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, name1, desc1);
							Type ex = Type.getType(Exception.class);
							Type ste = Type.getType(StackTraceElement.class);
							Label l1 = gaa.mark();
							gaa.newInstance(ex);
							gaa.dup();
							gaa.invokeConstructor(ex, new Method("<init>", "()V"));
							gaa.invokeVirtual(ex, new Method("getStackTrace", "()[Ljava/lang/StackTraceElement;"));
							gaa.loadArg(0);
							gaa.arrayLoad(Type.getType("[" + ex.getDescriptor()));
							gaa.invokeVirtual(ste, new Method("getClassName", "()Ljava/lang/String;"));
							gaa.visitVarInsn(Opcodes.ASTORE, 1);
							gaa.push(Type.getType("L" + cr.getClassName() + ";"));
							gaa.invokeVirtual(Type.getType(Class.class), new Method("getClassLoader", "()Ljava/lang" +
									"/ClassLoader;"));
							gaa.loadLocal(1);
							gaa.invokeVirtual(Type.getType(ClassLoader.class), new Method("loadClass", "(Ljava/lang" +
									"/String;)Ljava/lang/Class;"));
							Label l2 = gaa.mark();
							gaa.returnValue();
							gaa.catchException(l1, l2, Type.getType(
									ClassNotFoundException.class
							));
							gaa.storeLocal(1);
							gaa.newInstance(Type.getType(RuntimeException.class));
							gaa.dup();
							gaa.loadLocal(1);
							gaa.invokeConstructor(Type.getType(RuntimeException.class), new Method("<init>", "(Ljava" +
									"/lang/Throwable;)V"));
							gaa.throwException();
							gaa.endMethod();
						}
						super.visitMethodInsn(opcode, owner, name1, desc1, itf);
					}
				};
			}
		}, 0);

		return cw.toByteArray();
	}
}
