package moe.xinmu.minecraft.patcher;
import java.io.*;
import java.lang.Deprecated;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Arrays;

import org.apache.bcel.Const;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import moe.xinmu.minecraft_agent.*;
import moe.xinmu.minecraft_agent.annotation.*;
import javassist.*;

@TargetClass("org.spongepowered.asm.service.mojang.MixinServiceLaunchWrapper")
public class PatchMixinServiceLaunchWrapper implements ClassFileTransformer {
	private static final String gcbbscl =
			"{\n" +
					"return java.lang.ClassLoader.getSystemResourceAsStream($1).readAllBytes();\n" +
					"}";
	private static final String gcbbacl =
			"{\n" +
					"java.lang.ClassLoader app=ClassLoader.getSystemClassLoader();\n" +
					"java.lang.String v1=$1.replace('.', '/');\n" +
					"java.lang.String v2=v1+\".class\";\n" +
					"byte[] b = app.getResourceAsStream(v2).readAllBytes();\n" +
					"if(b!=null){\n" +
					"return b;\n" +
					"}\n" +
					"return getClassBytesBySystemClassLoader(v2);\n" +
					"}";
	private static final String gcb =
			"{\n" +
					"byte[] v1=net.minecraft.launchwrapper.Launch.classLoader.getClassBytes($1);\n" +
					"if(v1!=null)\n" +
					"return v1;\n" +
					"return getClassBytesByAppClassLoader($2);\n" +
					"}";

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		try {
			ClassPool cp = new ClassPool();
			CtClass string = cp.makeClass(new ClassClassPath(loader.loadClass("java.lang.String")).openClassfile("java.lang.String"));
			cp.insertClassPath(new ClassClassPath(loader.loadClass("jdk.internal.loader.ClassLoaders")));
			cp.insertClassPath(new ClassClassPath(loader.loadClass("moe.xinmu.minecraft_agent.Utils")));
			cp.insertClassPath(new ClassClassPath(loader.loadClass("net.minecraft.launchwrapper.LaunchClassLoader")));
			cp.insertClassPath(new ClassClassPath(loader.loadClass("net.minecraft.launchwrapper.Launch")));
			CtClass cc = cp.makeClass(new ByteArrayInputStream(classfileBuffer));
			CtMethod cm = cc.getMethod("getClassBytes", "(Ljava/lang/String;Ljava/lang/String;)[B");
			CtMethod cnms = CtNewMethod.make(cm.getReturnType(), "getClassBytesBySystemClassLoader", new CtClass[] {string}, cm.getExceptionTypes(), "return null;", cc);
			cnms.getMethodInfo2().setAccessFlags(cnms.getMethodInfo2().getAccessFlags() | Modifier.STATIC);
			cnms.setBody(gcbbscl);
			cc.addMethod(cnms);
			CtMethod cnm = CtNewMethod.make(cm.getReturnType(), "getClassBytesByAppClassLoader", new CtClass[] {string}, cm.getExceptionTypes(), "return null;", cc);
			cnm.getMethodInfo2().setAccessFlags(cnm.getMethodInfo2().getAccessFlags() | Modifier.STATIC);
			cnm.setBody(gcbbacl);
			cc.addMethod(cnm);
			cm.setBody(gcb);
			return cc.toBytecode();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
