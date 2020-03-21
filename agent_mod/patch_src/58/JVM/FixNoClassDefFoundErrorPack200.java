package moe.xinmu.minecraft.patcher;

import moe.xinmu.minecraft_agent.SMain;
import moe.xinmu.minecraft_agent.annotation.$Main;
import moe.xinmu.minecraft_agent.annotation.Main;
import org.apache.bcel.Const;
import org.apache.bcel.classfile.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

@Main
public class FixNoClassDefFoundErrorPack200 implements $Main, ClassFileTransformer {
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		try {
			JavaClass javaClass = new ClassParser(new ByteArrayInputStream(classfileBuffer), className + ".class").parse();
			ConstantPool constantPool = javaClass.getConstantPool();
			for (int index = 1; index < constantPool.getConstantPool().length; index++) {
				Constant constant = constantPool.getConstantPool()[index];
				if (constant == null) {
					continue;
				}
				if (constant.getTag() == Const.CONSTANT_Utf8) {
					ConstantUtf8 u = (ConstantUtf8) constant;
					String name = u.getBytes();

					if (name.equals("java/util/jar/Pack200")) {
						constantPool.setConstant(index, new ConstantUtf8("io/pack200/Pack200"));
						continue;
					}
					if (name.equals("java/util/jar/Pack200$Unpacker")) {
						constantPool.setConstant(index, new ConstantUtf8("io/pack200/Pack200$Unpacker"));
						continue;
					}
					if (name.equals("()Ljava/util/jar/Pack200$Unpacker;")) {
						constantPool.setConstant(index, new ConstantUtf8("()Lio/pack200/Pack200$Unpacker;"));
					}
				}
			}
			javaClass.setConstantPool(constantPool);
			return javaClass.getBytes();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void main() {
		boolean notFound = false;
		try {
			Class.forName("java.util.jar.Pack200");
		} catch (ClassNotFoundException e) {
			notFound = true;
		}
		if (notFound) {
			registerPack200Etc();
		}
	}

	private void registerPack200Etc() {
		SMain.INSTANCE.registerLimitTransformer("net.minecraftforge.fml.common.patcher.ClassPatchManager", this);
	}

}
