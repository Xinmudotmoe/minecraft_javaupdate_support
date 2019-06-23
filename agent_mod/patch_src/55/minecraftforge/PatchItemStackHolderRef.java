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

@TargetClass("net.minecraftforge.fml.common.registry.ItemStackHolderRef")
public class PatchItemStackHolderRef implements ClassFileTransformer{
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        try{
/*            byte[] data=new PatchUnsafe().transform(loader,className,classBeingRedefined,protectionDomain,classfileBuffer);
            if(data==null)
                return null;*/
            ClassPool classPool = new ClassPool();
            CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
            classPool.insertClassPath(new ByteArrayClassPath("java.lang.reflect.Field",ClassLoader.getSystemResourceAsStream("java/lang/reflect/Field.class").readAllBytes()));
            CtClass runtimeException = classPool.makeClass(ClassLoader.getSystemResourceAsStream("java/lang/RuntimeException.class"));
            CtMethod cm=ctClass.getMethod("makeWritable","(Ljava/lang/reflect/Field;)V");
            cm.setBody("return;");
            return ctClass.toBytecode();
        }   catch (Throwable e){
            e.printStackTrace();
        }
        return null;
    }
}
