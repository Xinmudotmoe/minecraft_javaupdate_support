package moe.xinmu.minecraft_agent;

import moe.xinmu.minecraft_agent.annotation.$Main;
import moe.xinmu.minecraft_agent.annotation.Main;
import moe.xinmu.minecraft_agent.annotation.TargetClass;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;

public final class SMain implements $Main {
	public void main(AgentModClassLoader amcl, Instrumentation instrumentation) {
		String[] s = amcl.compile();
		ArrayList<Class<? extends $Main>> mains = new ArrayList<>();
		ArrayList<Class<?>> nativemains = new ArrayList<>();
		for (String name : s) {
			try {
				Class<?> c = amcl.loadClass(name);
				TargetClass tc = c.getDeclaredAnnotation(TargetClass.class);
				Main m = c.getDeclaredAnnotation(Main.class);
				if (tc != null) {
					ClassFileTransformer o = (ClassFileTransformer) c.getConstructor().newInstance();
					if (tc.value().length == 0)
						instrumentation.addTransformer(new ClassFileTransformer() {
							private ClassFileTransformer c = o;
							private PrintStream err = System.err;

							public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
								try {
									byte[] source = classfileBuffer.clone();
									byte[] bytes = c.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
									if (bytes != null && Arrays.equals(source, bytes)) {
										err.printf("Change `%s` by `%s` in `%s`.\n", className.replace("/", "."), c.getClass().getName(), loader);
										return bytes;
									}
								} catch (Throwable e) {
									e.printStackTrace();
								}
								return null;
							}
						});
					else
						for (String key : tc.value())
							amcl.addClassFileTransformer(key, o);
				}
				if (m != null) {
					if ($Main.class.isAssignableFrom(c))
						mains.add(c.asSubclass($Main.class));
					else
						nativemains.add(c);
				}
			} catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException | ClassCastException e) {
				e.printStackTrace(System.err);
			}
		}
		for (Class<? extends $Main> main : mains) {
			try {
				main.getConstructor().newInstance().main(amcl, instrumentation);
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
		for (Class<?> main : nativemains) {
			try {
				main.getMethod("main", String[].class).invoke(null, (Object) new String[0]);
			} catch (Exception ignored) {
			}
		}

	}
}
