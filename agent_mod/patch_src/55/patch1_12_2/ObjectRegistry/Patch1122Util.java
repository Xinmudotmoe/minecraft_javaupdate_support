package moe.xinmu.minecraft.patcher.patch1122;

import moe.xinmu.minecraft_agent.Utils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;

public class Patch1122Util {
	static final boolean canhide;

	static {
		canhide = ClassLoader.getSystemClassLoader().getResource("java/lang/invoke/LambdaForm$Hidden.class") != null;
	}

	public static byte[] GenIClassTransformer(Class<? extends ClassFileTransformer> clazz) {
		String iClassName = GenIClassName(clazz);
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		cw.visit(Utils.getJavaVersion(), Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, iClassName.replace(".", "/"), null,
				Object.class.getName().replace(".", "/"), new String[] {"net/minecraft/launchwrapper/IClassTransformer"});
		cw.visitField(Opcodes.ACC_PRIVATE, "classFileTransformer", Type.getDescriptor(ClassFileTransformer.class), null,
				null);
		if (canhide)
			cw.visitAnnotation("Ljava/lang/invoke/LambdaForm$Hidden;", true);
		GeneratorAdapter ga = new GeneratorAdapter(cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null), Opcodes.ACC_PUBLIC, "<init>", "()V");
		ga.loadThis();
		ga.invokeConstructor(Type.getType(Object.class), new Method("<init>", "()V"));
		ga.loadThis();
		ga.newInstance(Type.getType(clazz));
		ga.dup();
		ga.invokeConstructor(Type.getType(clazz), new Method("<init>", "()V"));
		ga.putField(Type.getType(iClassName.replace(".", "/")), "classFileTransformer",
				Type.getType(ClassFileTransformer.class));
		ga.returnValue();
		ga.endMethod();
		ga = new GeneratorAdapter(cw.visitMethod(Opcodes.ACC_PUBLIC, "transform", "(Ljava/lang/String;Ljava/lang/String;[B)[B",
				null, null), Opcodes.ACC_PUBLIC, "transform", "(Ljava/lang/String;Ljava/lang/String;[B)[B");
		Label l0 = ga.newLabel();
		ga.mark(l0);
		ga.loadThis();
		ga.getField(Type.getType(iClassName.replace(".", "/")), "classFileTransformer",
				Type.getType(ClassFileTransformer.class));
		ga.push((String) null);
		ga.loadArg(0);
		ga.push((String) null);
		ga.push((String) null);
		ga.loadArg(2);

		ga.invokeInterface(Type.getType(ClassFileTransformer.class), new Method("transform",
				"(Ljava/lang/ClassLoader;Ljava/lang/String;Ljava/lang/Class;Ljava/security/ProtectionDomain;[B)[B"));
		Label l1 = ga.newLabel();
		ga.mark(l1);
		ga.visitInsn(Opcodes.ARETURN);
		Label l2 = ga.newLabel();
		ga.mark(l2);
		ga.loadArg(2);
		ga.returnValue();
		ga.visitTryCatchBlock(l0, l1, l2, "java/lang/instrument/IllegalClassFormatException");
		ga.returnValue();
		ga.endMethod();
		return cw.toByteArray();
	}

	public static String GenIClassName(Class<?> clazz) {
		return clazz.getName() + "$$$IClassTransformer";
	}
}
