package moe.xinmu.minecraft.patcher;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import moe.xinmu.minecraft_agent.Utils;
import moe.xinmu.minecraft_agent.annotation.Main;
import moe.xinmu.minecraft_agent.annotation.PreMain;
import moe.xinmu.minecraft_agent.annotation.TargetClass;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.io.IOException;

import static moe.xinmu.minecraft_agent.Utils.getUnsafe;

public class FieldValueUtils {
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
