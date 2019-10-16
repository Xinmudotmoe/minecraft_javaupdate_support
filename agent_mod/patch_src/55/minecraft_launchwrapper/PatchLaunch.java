package moe.xinmu.minecraft.patcher;

import moe.xinmu.minecraft_agent.Utils;
import moe.xinmu.minecraft_agent.annotation.TargetClass;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.*;

@TargetClass("net.minecraft.launchwrapper.Launch")
public class PatchLaunch implements ClassFileTransformer {
	public final static List<String> addClassLoaderExclusion = Collections.synchronizedList(new ArrayList<>());
	public final static List<String> addTransformerExclusion = Collections.synchronizedList(new ArrayList<>());
	public final static List<String> registerTransformer = Collections.synchronizedList(new ArrayList<>());

	static {
		addClassLoaderExclusion.add("moe.xinmu.minecraft_agent.Utils");
		addClassLoaderExclusion.add("moe.xinmu.minecraft_agent.version");
		addClassLoaderExclusion.add("moe.xinmu.minecraft_agent.DeobfUtils");
		addClassLoaderExclusion.add("jdk.internal");
	}

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
							ProtectionDomain protectionDomain, byte[] classfileBuffer) {
		try {
			Method constructor = new Method("<init>", "()V");
			ClassReader cr = new ClassReader(classfileBuffer);
			Type classname = Type.getType("L" + cr.getClassName() + ";");
			ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
			cr.accept(new ClassVisitor(Opcodes.ASM5, cw) {
				@Override
				public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
					if (name.equals("<init>") && descriptor.equals("()V"))
						return null;
					return super.visitMethod(access, name, descriptor, signature, exceptions);
				}
			}, 0);
			MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PRIVATE, "<init>", "()V", null, null);
			GeneratorAdapter ga = new GeneratorAdapter(mv, Opcodes.ACC_PRIVATE, "<init>", "()V");
			ga.loadThis();
			ga.invokeConstructor(Type.getType(Object.class), constructor);
			ga.loadThis();
			ga.newInstance(Type.getType("Lnet/minecraft/launchwrapper/LaunchClassLoader;"));
			ga.dup();
			ga.invokeStatic(Type.getType(Utils.class),
					Method.getMethod(Utils.class.getDeclaredMethod("getClassLoaderURLs")));
			ga.invokeConstructor(Type.getType("Lnet/minecraft/launchwrapper/LaunchClassLoader;"),
					new Method("<init>", Type.VOID_TYPE, new Type[] {Type.getType("[Ljava/net/URL;")}));
			ga.putStatic(classname, "classLoader",
					Type.getType("Lnet/minecraft/launchwrapper/LaunchClassLoader;"));
			ga.newInstance(Type.getType(HashMap.class));
			ga.dup();
			ga.invokeConstructor(Type.getType(HashMap.class), constructor);
			ga.putStatic(classname, "blackboard", Type.getType(Map.class));
			ga.invokeStatic(Type.getType(Thread.class),
					Method.getMethod(Thread.class.getDeclaredMethod("currentThread")));
			ga.getStatic(classname, "classLoader",
					Type.getType("Lnet/minecraft/launchwrapper/LaunchClassLoader;"));
			ga.invokeVirtual(Type.getType(Thread.class),
					Method.getMethod(Thread.class.getDeclaredMethod("setContextClassLoader", ClassLoader.class)));
			for (String s : addClassLoaderExclusion) {
				ga.getStatic(classname, "classLoader",
						Type.getType("Lnet/minecraft/launchwrapper/LaunchClassLoader;"));
				ga.push(s);
				ga.invokeVirtual(Type.getType("Lnet/minecraft/launchwrapper/LaunchClassLoader;"),
						new Method("addClassLoaderExclusion", Type.VOID_TYPE, new Type[] {Type.getType(String.class)}));
			}
			for (String s : addTransformerExclusion) {
				ga.getStatic(classname, "classLoader",
						Type.getType("Lnet/minecraft/launchwrapper/LaunchClassLoader;"));
				ga.push(s);
				ga.invokeVirtual(Type.getType("Lnet/minecraft/launchwrapper/LaunchClassLoader;"),
						new Method("addTransformerExclusion", Type.VOID_TYPE, new Type[] {Type.getType(String.class)}));
			}
			for (String s : registerTransformer) {
				ga.getStatic(classname, "classLoader",
						Type.getType("Lnet/minecraft/launchwrapper/LaunchClassLoader;"));
				ga.push(s);
				ga.invokeVirtual(Type.getType("Lnet/minecraft/launchwrapper/LaunchClassLoader;"),
						new Method("registerTransformer", Type.VOID_TYPE, new Type[] {Type.getType(String.class)}));
			}
			ga.returnValue();
			ga.endMethod();
			cw.visitEnd();
			return cw.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
