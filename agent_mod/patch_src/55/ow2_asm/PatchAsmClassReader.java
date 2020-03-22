package moe.xinmu.asm_upgrade_patcher;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import moe.xinmu.minecraft_agent.Utils;
import moe.xinmu.minecraft_agent.annotation.*;

@TargetClass(
		{
				"org.objectweb.asm.ClassReader",
				"org.spongepowered.asm.lib.ClassReader"
		})
public class PatchAsmClassReader implements ClassFileTransformer {
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
		try {
			ClassGen clazz = new ClassGen(new ClassParser(new ByteArrayInputStream(classfileBuffer), className).parse());
			try {
				Method m = clazz.containsMethod("<init>", "([BII)V");
				Code code = m.getCode();
				InstructionList il = new InstructionList(code.getCode());
				if (DisableASMVersionCheck.change(il)) {
					code.setCode(il.getByteCode());
					return clazz.getJavaClass().getBytes();
				}
			} catch (Exception e) {

			}
			try {
				Method m = clazz.containsMethod("<init>", "([BIZ)V");
				Code code = m.getCode();
				InstructionList il = new InstructionList(code.getCode());
				if (DisableASMVersionCheck.change(il)) {
					code.setCode(il.getByteCode());
					return clazz.getJavaClass().getBytes();
				}
			} catch (Exception e) {

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@PreMain
	public static class PatchASM implements $Main {
		public void main(moe.xinmu.minecraft_agent.AgentModClassLoader amcl, Instrumentation instrumentation) {
			System.out.println("Try to read the ClassReader in the agent environment.");
			try {
				ClassLoader cl = amcl.getClassLoader();
				Class<?> c = cl.loadClass("org.objectweb.asm.ClassReader");
				byte[] v = cl.getResourceAsStream("org.objectweb.asm.ClassReader".replace(".", "/") + ".class").readAllBytes();
				instrumentation.redefineClasses(new ClassDefinition(c, new PatchAsmClassReader().transform(c.getClassLoader(), c.getName(), c, null, v)));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
