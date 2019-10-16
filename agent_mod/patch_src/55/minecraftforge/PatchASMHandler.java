package moe.xinmu.minecraft.patcher;

import moe.xinmu.minecraft_agent.Utils;
import moe.xinmu.minecraft_agent.annotation.TargetClass;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

@TargetClass("net.minecraftforge.fml.common.eventhandler.ASMEventHandler")
public class PatchASMHandler implements ClassFileTransformer {
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
		try {
			ClassReader cr = new ClassReader(classfileBuffer);
			ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
			cr.accept(new ClassVisitor(Opcodes.ASM5, cw) {
				@Override
				public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
					MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
					if (name.equals("createWrapper") && desc.equals("(Ljava/lang/reflect/Method;)Ljava/lang/Class;")) {
						return new MethodVisitor(Opcodes.ASM5, new GeneratorAdapter(mv, access, name, desc)) {
							@Override
							public void visitIntInsn(int opcode, int operand) {
								if (opcode == Opcodes.BIPUSH && operand == 50)
									operand = Utils.getJavaVersion();
								super.visitIntInsn(opcode, operand);
							}
							boolean change=false;
							@Override
							public void visitLdcInsn(Object cst) {
								if(cst instanceof String){
									if("invoke".equals(cst))
										change=true;
								}
								super.visitLdcInsn(cst);
							}

							@Override
							public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
								if (change&&opcode == Opcodes.INVOKEVIRTUAL && owner.equals("org/objectweb/asm" +
										"/MethodVisitor") && name.equals("visitMethodInsn")) {
									GeneratorAdapter ga = (GeneratorAdapter) mv;
									ga.pop();
									ga.loadArg(0);
									ga.invokeVirtual(Type.getType(java.lang.reflect.Method.class), new org.objectweb.asm.commons.Method("getDeclaringClass", Type.getType(Class.class), new Type[0]));
									ga.invokeVirtual(Type.getType(Class.class), new org.objectweb.asm.commons.Method("isInterface", Type.BOOLEAN_TYPE, new Type[0]));
								}
								super.visitMethodInsn(opcode, owner, name, desc, itf);
							}
						};
					}
					return mv;
				}
			}, ClassReader.EXPAND_FRAMES);
			return cw.toByteArray();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}
}
