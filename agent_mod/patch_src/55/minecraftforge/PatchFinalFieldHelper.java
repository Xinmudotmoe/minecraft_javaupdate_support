package moe.xinmu.minecraft.patcher;
import java.io.*;
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

@TargetClass({
		"net.minecraftforge.registries.ObjectHolderRef$FinalFieldHelper",
		"net.minecraftforge.fml.common.registry.FinalFieldHelper",
		"cpw.mods.fml.common.registry.ObjectHolderRef"
})
//TODO: Plan to modify.
public class PatchFinalFieldHelper implements ClassFileTransformer {
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		try {
			JavaClass jc = new ClassParser(new ByteArrayInputStream(classfileBuffer), className).parse();
			ClassGen cg = new ClassGen(jc);
			ConstantPoolGen cpg = cg.getConstantPool();
			for (Field f : cg.getFields())
				cg.removeField(f);
			Method m = cg.getMethodAt(1);
			Code c = m.getCode();
			InstructionList il = new InstructionList();
			il.append(new ALOAD(0));
			il.append(new ARETURN());
			c.setMaxStack(2);
			c.setAttributes(new Attribute[0]);
			c.setCode(il.getByteCode());
			ClassPool classPool = new ClassPool();
			classPool.insertClassPath(new ClassClassPath(java.lang.reflect.Field.class));
			classPool.insertClassPath(new ClassClassPath(moe.xinmu.minecraft_agent.Utils.class));
			CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(cg.getJavaClass().getBytes()));
			try{
				CtMethod cm = ctClass.getMethod("setField", "(Ljava/lang/reflect/Field;Ljava/lang/Object;Ljava/lang/Object;)V");
				cm.setBody(
						"{\n" +
								"moe.xinmu.minecraft.patcher.FieldValueUtils.setFieldValue($2,$1,$3);"+
								"}"
				);
			}catch (javassist.NotFoundException e){}
			//TODO Forge 10.13.4
			return ctClass.toBytecode();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}
}
