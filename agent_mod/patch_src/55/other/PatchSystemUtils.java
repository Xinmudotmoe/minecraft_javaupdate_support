package moe.xinmu.minecraft.patcher;

import moe.xinmu.minecraft_agent.annotation.TargetClass;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

@TargetClass("org.apache.commons.lang3.SystemUtils")
public class PatchSystemUtils implements ClassFileTransformer {
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		ClassReader cr=new ClassReader(classfileBuffer);
		ClassWriter cw=new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
		cr.accept(new ClassVisitor(Opcodes.ASM5,cw) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				if(name.equals("isJavaVersionAtLeast")){
					GeneratorAdapter ga=new GeneratorAdapter(mv,access,name,desc);
					ga.push(true);
					ga.returnValue();
					ga.endMethod();
					return null;
				}
				return mv;
			}
		},ClassReader.EXPAND_FRAMES);
		return cw.toByteArray();
	}
}
