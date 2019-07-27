package moe.xinmu.minecraft.patcher;

import moe.xinmu.minecraft_agent.annotation.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.concurrent.atomic.AtomicBoolean;

@TargetClass(PatchClassVisitor.target)
public class PatchClassVisitor implements ClassFileTransformer {
	static final String target = "org.objectweb.asm.ClassVisitor";

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		ClassReader cr = new ClassReader(classfileBuffer);
		ClassWriter cw = new ClassWriter(cr, 1);
		final AtomicBoolean flag = new AtomicBoolean(false);
		cr.accept(new ClassVisitor(Opcodes.ASM5, cw) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
				MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
				switch (name) {
					case "visitNestMemberExperimental":
					case "visitNestHostExperimental":
						GeneratorAdapter ga = new GeneratorAdapter(mv, access, name, descriptor);
						ga.returnValue();
						ga.endMethod();
						flag.set(true);
						return null;
				}
				return mv;
			}
		}, 0);
		if (flag.get())
			return cw.toByteArray();
		return null;
	}

	@Main
	public static class PatchASM implements $Main {
		public void main(moe.xinmu.minecraft_agent.AgentModClassLoader amcl, Instrumentation instrumentation) {
			System.out.println("Try to read the ClassVisitor in the agent environment.");
			try {
				Class<?> c = amcl.loadClass(target);
				byte[] v = c.getResourceAsStream(target.replace(".", "/") + ".class").readAllBytes();
				instrumentation.redefineClasses(new ClassDefinition(c, new PatchClassVisitor().transform(c.getClassLoader(), c.getName(), c, null, v)));
			} catch (Exception e) {
			}
		}
	}
}
