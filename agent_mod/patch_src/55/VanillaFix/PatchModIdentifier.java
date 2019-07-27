package moe.xinmu.minecraft.patcher.future;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import moe.xinmu.minecraft_agent.annotation.TargetClass;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;

@TargetClass("org.dimdev.utils.ModIdentifier")
public class PatchModIdentifier implements ClassFileTransformer {
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
		try {
			ClassPool cp = new ClassPool();
			cp.insertClassPath(new LoaderClassPath(new URLClassLoader(((URLClassLoader) loader).getURLs(), null)));
			CtClass cc = cp.makeClass(new ByteArrayInputStream(classfileBuffer));
			CtMethod m = cc.getDeclaredMethod("identifyFromClass", new CtClass[] {cp.makeClass("java.lang.String"), cp.makeClass("java.util.Map")});
			m.addCatch("{return java.util.Collections.emptySet();}", cp.makeClass("java.lang.IllegalArgumentException"));
			return cc.toBytecode();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
