package moe.xinmu.minecraft.patcher;

import java.io.ByteArrayInputStream;
import java.security.ProtectionDomain;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;

import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;

import moe.xinmu.minecraft_agent.annotation.*;

@TargetClass(
		"nonupdate.forge.ReflectionHelper"
)
public class PatchNonUpdateReflectionHelper implements ClassFileTransformer  {
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		try {
			ClassParser cp = new ClassParser(new ByteArrayInputStream(classfileBuffer), className);
			JavaClass jc = cp.parse();
			ClassGen cg = new ClassGen(jc);
			ConstantPoolGen cop = cg.getConstantPool();
			boolean change = false;
			int index;
			if ((index = cop.lookupUtf8("sun/reflect/Reflection")) != -1) {
				change = true;
				cop.setConstant(index,
						new ConstantUtf8("jdk/internal/reflect/Reflection"));
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