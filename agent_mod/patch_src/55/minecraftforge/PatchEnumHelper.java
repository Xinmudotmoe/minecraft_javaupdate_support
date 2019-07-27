package moe.xinmu.minecraft.patcher;

import moe.xinmu.minecraft_agent.Utils;

import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.io.ByteArrayInputStream;
import org.objectweb.asm.*;
import javassist.*;
import moe.xinmu.minecraft_agent.annotation.TargetClass;
import static moe.xinmu.minecraft_agent.Utils.getUnsafe;

public class PatchEnumHelper {
	@TargetClass("net/minecraftforge/common/util/EnumHelper")
	public static class Patch implements ClassFileTransformer {
		static String targetname;

		static {
			targetname = PatchEnumHelper.class.getName();
			String resourcename = targetname.replace(".", "/").concat(".class");
			if (new URLClassLoader(Utils.getClassLoaderURLs(), null).getResource(resourcename) == null) {
				Utils.addClassLoaderURLs(PatchEnumHelper.class.getClassLoader().getResource(resourcename));
			}
		}

		@Override
		public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
			try {
				byte[] cb = classfileBuffer;
				byte[] un = ((ClassFileTransformer) (this.getClass().getClassLoader().loadClass("moe.xinmu.minecraft.patcher.PatchUnsafe").newInstance())).transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
				if (un != null)
					cb = un;
				ClassPool cp = new ClassPool();
				cp.insertClassPath(new LoaderClassPath(ClassLoader.getSystemClassLoader()));
				CtClass cc = cp.makeClass(new ByteArrayInputStream(cb));
				CtMethod cm = cc.getMethod("setFailsafeFieldValue", "(Ljava/lang/reflect/Field;Ljava/lang/Object;Ljava/lang/Object;)V");
				cm.setBody("{" + targetname + ".setFieldValue($2,$1,$3);}");
				return cc.toBytecode();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	public static void setFieldValue(/*Nullable*/Object mirror,/*NotNull*/Field field,/*NotNull*/Object o) {
		boolean is_static = Modifier.isStatic(field.getModifiers());
		if (!is_static && mirror == null)
			throw new NullPointerException("Not Static.");
		long offset;
		if (is_static)
			offset = getUnsafe().staticFieldOffset(field);
		else
			offset = getUnsafe().objectFieldOffset(field);
		if (is_static)
			mirror = getUnsafe().staticFieldBase(field);
		getUnsafe().putObjectVolatile(mirror, offset, o);

		Utils.setAccessible(field, true);
		try {
			field.get(mirror);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
