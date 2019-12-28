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

@PreMain
@TargetClass("net/minecraftforge/common/util/EnumHelper")
// TODO c4cf950863256ed048a46e098401a969dfe4cb2ec4cf950
// -Fix the rest of the "easy" compile errors (#5151)
public class PatchEnumHelperImpl implements ClassFileTransformer {
	static String targetname;

	public static void main(String[] args) {
		targetname = FieldValueUtils.class.getName();
		System.err.println("PatchEnumHelper");
		String resourcename = targetname.replace(".", "/").concat(".class");
		if (new URLClassLoader(Utils.getClassLoaderURLs(), null).getResource(resourcename) == null) {
			try {
				Utils.TempJar.INSTANCE.addFile(resourcename,
						PatchEnumHelperImpl.class.getClassLoader().getResource(resourcename).openStream());
			}catch (IOException e){
				e.printStackTrace();
			}
		}
	}

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
		try {
			byte[] cb = classfileBuffer;
			byte[] un = new PatchUnsafe().transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
			if (un != null)
				cb = un;
			ClassPool cp = new ClassPool();
			cp.insertClassPath(new LoaderClassPath(ClassLoader.getSystemClassLoader()));
			CtClass cc = cp.makeClass(new ByteArrayInputStream(cb));
			CtMethod cm = cc.getMethod("setFailsafeFieldValue", "(Ljava/lang/reflect/Field;Ljava/lang/Object;Ljava/lang/Object;)V");
			cm.setBody("{" + targetname + ".setFieldValue($2,$1,$3);}");
			return cc.toBytecode();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}