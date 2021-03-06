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

@Deprecated
/*@TargetClass(
		"cpw.mods.fml.common.registry.ObjectHolderRef"//TODO Minecraft Forge Legacy
)*/
public class PatchUnsafe implements ClassFileTransformer {
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		try {
			ClassParser cp = new ClassParser(new ByteArrayInputStream(classfileBuffer), className);
			JavaClass jc = cp.parse();
			ClassGen cg = new ClassGen(jc);
			ConstantPoolGen cop = cg.getConstantPool();
			boolean change = false;
			int index;
			if ((index = cop.lookupUtf8("sun.reflect.ConstructorAccessor")) != -1) {
				change = true;
				cop.setConstant(index,
						new ConstantUtf8("jdk.internal.reflect.ConstructorAccessor"));
			}
			if ((index = cop.lookupUtf8("sun.reflect.ReflectionFactory")) != -1) {
				change = true;
				cop.setConstant(index,
						new ConstantUtf8("jdk.internal.reflect.ReflectionFactory"));
			}
			if ((index = cop.lookupUtf8("sun.reflect.FieldAccessor")) != -1) {
				change = true;
				cop.setConstant(index,
						new ConstantUtf8("jdk.internal.reflect.FieldAccessor"));
			}
			if (change) {
				ModuleOpenHelper.OpenModule("java.base", "jdk.internal.reflect");
				return cg.getJavaClass().getBytes();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
