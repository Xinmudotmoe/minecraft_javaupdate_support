package moe.xinmu.minecraft.patcher;

import moe.xinmu.minecraft_agent.Utils;
import moe.xinmu.minecraft_agent.annotation.TargetClass;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

@TargetClass({
		"lumien.randomthings.asm.CustomClassWriter",
		"shadows.CustomClassWriter",
		"shadows.squeezer.CustomClassWriter"
})
public class PatchCustomClassWriter implements ClassFileTransformer {
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
		ClassReader cr = new ClassReader(classfileBuffer);
		ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
		cr.accept(new ClassVisitor(Opcodes.ASM5, cw) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				if (name.equals("<clinit>"))
					return new MethodVisitor(Opcodes.ASM5, new GeneratorAdapter(super.visitMethod(access, name, desc,
							signature, exceptions), access, name, desc)) {
						@Override
						public void visitTypeInsn(int opcode, String type) {
							if (opcode == Opcodes.CHECKCAST && type.equals("java/net/URLClassLoader")) {
								GeneratorAdapter ga = (GeneratorAdapter) this.mv;
								ga.pop();
								ga.invokeStatic(Type.getType(Utils.class), new Method("getClassLoaderURLs", "()[Ljava" +
										"/net/URL;"));
								return;
							}
							super.visitTypeInsn(opcode, type);
						}

						@Override
						public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
							if (opcode == Opcodes.INVOKEVIRTUAL && owner.equals("java/net/URLClassLoader") && name.equals("getURLs"))
								return;
							super.visitMethodInsn(opcode, owner, name, desc, itf);
						}
					};
				return super.visitMethod(access, name, desc, signature, exceptions);
			}
		}, 0);
		return cw.toByteArray();
	}
}