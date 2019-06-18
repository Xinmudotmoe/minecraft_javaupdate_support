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

@TargetClass("net/minecraftforge/common/util/EnumHelper")
public class PatchEnumHelper implements ClassFileTransformer{
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        try {
            byte[] cb = classfileBuffer;
            byte[] un = ((ClassFileTransformer)(this.getClass().getClassLoader().loadClass("moe.xinmu.minecraft.patcher.PatchUnsafe").newInstance())).transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
            if (un != null)
                cb = un;
            ClassPool cp=new ClassPool();
            cp.insertClassPath(new ClassClassPath(ClassLoader.getSystemClassLoader().loadClass("java.lang.reflect.Field")));
            cp.insertClassPath(new ClassClassPath(this.getClass().getClassLoader().loadClass("moe.xinmu.minecraft_agent.Utils")));
            CtClass cc=cp.makeClass(new ByteArrayInputStream(cb));
            CtMethod cm=cc.getMethod("setFailsafeFieldValue","(Ljava/lang/reflect/Field;Ljava/lang/Object;Ljava/lang/Object;)V");
            cm.setBody(
                    "{\n"+
                            "boolean is_static=java.lang.reflect.Modifier.isStatic($1.getModifiers());\n" +
                            "if(!is_static&&$2==null){\n" +
                            "    throw new java.lang.NullPointerException(\"Not Static.\");\n" +
                            "}\n"+
                            "long offset=is_static?\n" +
                            "        moe.xinmu.minecraft_agent.Utils.getUnsafe().staticFieldOffset($1):\n" +
                            "        moe.xinmu.minecraft_agent.Utils.getUnsafe().objectFieldOffset($1);\n" +
                            "if(is_static){\n" +
                            "    $2=moe.xinmu.minecraft_agent.Utils.getUnsafe().staticFieldBase($1);\n" +
                            "}"+
                            "moe.xinmu.minecraft_agent.Utils.getUnsafe().putObjectVolatile($2,offset,$3);\n"+
                            "}"
            );
            return cc.toBytecode();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
