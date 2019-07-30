package moe.xinmu.minecraft_agent;

import java.io.PrintStream;
import java.lang.instrument.*;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class Transformer implements ClassFileTransformer {

	private ConcurrentHashMap<String, Set<ClassFileTransformer>> map = new ConcurrentHashMap<>();
	private Instrumentation instrumentation;
	private PrintStream err = System.err;

	Transformer(Instrumentation instrumentation) {
		this.instrumentation = instrumentation;
	}

	private Class<?> findLoadedClass(ClassLoader cl, String s) {
		for (Class<?> i : instrumentation.getInitiatedClasses(cl))
			if (i.getName().equals(s))
				return i;
		return Transformer.class;
	}

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain
			protectionDomain, byte[] classfileBuffer) {
		try {
			if (className == null)
				return null;
			ArrayList<ClassFileTransformer> al = new ArrayList<>();
			if (map.containsKey(className))
				al.addAll(Utils.requireNonNullElse(map.get(className), new HashSet<>()));
			byte[] a = classfileBuffer.clone();
			for (ClassFileTransformer c : al)
				try {
					byte[] by = c.transform(loader, className, classBeingRedefined, protectionDomain, a);
					if (!(by == null || by.length < 10))
						if (!Arrays.equals(classfileBuffer, by)) {
							String clazzName = className.replace("/", ".");
							Class target = findLoadedClass(loader, clazzName);
							if (target == null || Transformer.class.equals(target)) {
								err.printf("Change `%s` by `%s` in `%s`.\n", clazzName, c.getClass().getName(), loader);
								return by;
							} else {
								err.printf("Warning, `%s` has been loaded before the transition and will attempt to redefine this class. The converter is `%s`.\n", clazzName, c.getClass().getName());
								redefine(target, by);
								return null;
							}
						}
				} catch (IllegalClassFormatException ignored) {
				}
		} catch (Throwable t) {
			t.printStackTrace(err);
		}
		return null;
	}

	private void redefine(Class<?> c, byte[] data) {
		try {
			instrumentation.redefineClasses(new ClassDefinition(c, data));
		} catch (ClassNotFoundException | UnmodifiableClassException e) {
			e.printStackTrace(err);
		}
	}

	void addClassFileTransformer(String classname, ClassFileTransformer classFileTransformer) {
		classname = classname.replace(".", "/");
		if (!map.containsKey(classname))
			map.put(classname, new HashSet<>());
		map.get(classname).add(classFileTransformer);
	}
}
