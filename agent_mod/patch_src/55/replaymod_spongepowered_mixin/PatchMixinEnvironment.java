package moe.xinmu.minecraft.patcher;

import moe.xinmu.minecraft_agent.annotation.TargetClass;
import org.objectweb.asm.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

@TargetClass("org.spongepowered.asm.mixin.MixinEnvironment")
public class PatchMixinEnvironment implements ClassFileTransformer {
	@Override
	public byte[] transform(ClassLoader loader,
							String className,
							Class<?> classBeingRedefined,
							ProtectionDomain protectionDomain,
							byte[] classfileBuffer) throws IllegalClassFormatException {
		ClassReader cr = new ClassReader(classfileBuffer);
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		cr.accept(new ClassVisitor(Opcodes.ASM5, cw) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				if (name.equals("setCompatibilityLevel")) {
					return new MethodVisitor(Opcodes.ASM5, super.visitMethod(access, name, desc, signature, exceptions)) {
						@Override
						public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
							super.visitMethodInsn(opcode, owner, name, desc, itf);
							if (name.equals("isSupported")) {
								visitInsn(Opcodes.POP);
								visitInsn(Opcodes.ICONST_1);
							}
						}
					};
				}
				return super.visitMethod(access, name, desc, signature, exceptions);
			}
		}, Opcodes.ASM5);
		return cw.toByteArray();
	}
}
