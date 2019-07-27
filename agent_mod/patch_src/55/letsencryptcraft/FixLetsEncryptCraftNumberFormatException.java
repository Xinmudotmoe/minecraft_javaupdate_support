package moe.xinmu.minecraft.patcher;

import moe.xinmu.minecraft_agent.annotation.TargetClass;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

@TargetClass("uk.co.cloudhunter.letsencryptcraft.LetsEncryptCraft")
public class FixLetsEncryptCraftNumberFormatException implements ClassFileTransformer {
	/* URL: https://letsencrypt.org/docs/certificate-compatibility/
	 * Known Compatible
	 *   Java 7 >= 7u111
	 *   Java 8 >= 8u101
	 * */
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		ClassReader cr = new ClassReader(classfileBuffer);
		ClassWriter cw = new ClassWriter(cr, 1);
		cr.accept(new ClassVisitor(Opcodes.ASM5, cw) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				if (name.equals("preInit")) {
					GeneratorAdapter ga = new GeneratorAdapter(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc);
					ga.returnValue();
					ga.endMethod();
					return null;
				}
				return super.visitMethod(access, name, desc, signature, exceptions);
			}
		}, 0);
		return cw.toByteArray();
	}
}
