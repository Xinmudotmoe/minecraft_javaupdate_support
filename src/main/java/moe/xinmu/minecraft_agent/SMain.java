package moe.xinmu.minecraft_agent;
import moe.xinmu.minecraft_agent.annotation.$Main;
import moe.xinmu.minecraft_agent.annotation.Main;
import moe.xinmu.minecraft_agent.annotation.PreMain;
import moe.xinmu.minecraft_agent.annotation.TargetClass;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;

public final class SMain implements $Main {
	public static SMain INSTANCE;
	public SMain(){
		INSTANCE=this;
	}
	private AgentModClassLoader amcl;
	private Instrumentation instrumentation;
	private static final PrintStream err = System.err;
	public String[] class_names;
	public void main(AgentModClassLoader amcl, Instrumentation instrumentation) {
		this.amcl=amcl;
		this.instrumentation=instrumentation;
		class_names=amcl.compile();
		ArrayList<String> s = new ArrayList<>(Arrays.asList(class_names));

		ArrayList<Class<? extends $Main>> pre_mains = new ArrayList<>();
		ArrayList<Class<?>> pre_naive_mains = new ArrayList<>();
		ArrayList<Class<? extends ClassFileTransformer>> transformers=new ArrayList<>();
		ArrayList<Class<? extends $Main>> mains = new ArrayList<>();
		ArrayList<Class<?>> naive_mains = new ArrayList<>();
		ClassLoader cl=amcl.getClassLoader();
		for (String name : s) {
			Class<?> c;
			try {
				c = cl.loadClass(name);
			} catch (ClassNotFoundException e) {
				continue;
			}
			if (!Modifier.isPublic(c.getModifiers()))
				continue;
			TargetClass tc = c.getDeclaredAnnotation(TargetClass.class);
			Main m = c.getDeclaredAnnotation(Main.class);
			PreMain p=c.getDeclaredAnnotation(PreMain.class);
			if(p!=null){
				if ($Main.class.isAssignableFrom(c))
					pre_mains.add(c.asSubclass($Main.class));
				else
					pre_naive_mains.add(c);
			}
			if (tc != null && tc.register()&& ClassFileTransformer.class.isAssignableFrom(c))
				transformers.add(c.asSubclass(ClassFileTransformer.class));

			if (m != null) {
				if ($Main.class.isAssignableFrom(c))
					mains.add(c.asSubclass($Main.class));
				else
					naive_mains.add(c);
			}

		}
		for (Class<? extends $Main> main : pre_mains) {
			try {
				main.getConstructor().newInstance().main(amcl, instrumentation);
			} catch (ReflectiveOperationException e) {
				e.printStackTrace();
			}
		}
		String[] bareas=new String[0];
		for (Class<?> main : pre_naive_mains) {
			try {
				main.getMethod("main", String[].class).invoke(null, (Object)bareas );
			} catch (Exception ignored) {
			}
		}
		for (Class<? extends ClassFileTransformer> transformer:transformers){
			try {
				TargetClass tc = transformer.getDeclaredAnnotation(TargetClass.class);
				ClassFileTransformer o = transformer.getConstructor().newInstance();
				if (tc.value().length == 0)
					registerTransformer(o);
				else
					for (String key : tc.value())
						registerLimitTransformer(key,o);
			} catch (ReflectiveOperationException e) {
				e.printStackTrace(System.err);
			}
		}
		for (Class<? extends $Main> main : mains) {
			try {
				main.getConstructor().newInstance().main(amcl, instrumentation);
			} catch (ReflectiveOperationException e) {
				e.printStackTrace();
			}
		}
		for (Class<?> main : naive_mains) {
			try {
				main.getMethod("main", String[].class).invoke(null, (Object) new String[0]);
			} catch (ReflectiveOperationException ignored) {
			}
		}
		try {
			Utils.addClassLoaderURLs(Utils.TempJar.INSTANCE.genFile().toURI().toURL());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void registerTransformer(final ClassFileTransformer cft){
		System.out.println(cft);
		instrumentation.addTransformer(new ClassFileTransformer() {
			private ClassFileTransformer c = cft;
			public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
				try {
					byte[] source = classfileBuffer.clone();
					byte[] bytes = c.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
					if (bytes != null && !Arrays.equals(source, bytes)) {
						err.printf("Change `%s` by `%s` in `%s`.\n", className.replace("/", "."), c.getClass().getName(), loader);
						return bytes;

					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
				return null;
			}
		});
	}
	public void registerLimitTransformer(String target,ClassFileTransformer cft){
		amcl.addClassFileTransformer(target, cft);
	}
}
