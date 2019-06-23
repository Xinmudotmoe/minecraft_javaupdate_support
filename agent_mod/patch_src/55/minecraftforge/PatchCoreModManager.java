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

@TargetClass("net/minecraftforge/fml/relauncher/CoreModManager")
public class PatchCoreModManager implements ClassFileTransformer{
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        try{
            ClassPool cp=new ClassPool();
            cp.insertClassPath(new ClassClassPath(loader.loadClass("java.io.File")));
            cp.insertClassPath(new ClassClassPath(loader.loadClass("java.net.URL")));
            cp.insertClassPath(new ClassClassPath(loader.loadClass("java.lang.Exception")));
            cp.insertClassPath(new ClassClassPath(loader.loadClass("moe.xinmu.minecraft_agent.Utils")));
            cp.insertClassPath(new ClassClassPath(loader.loadClass("net.minecraft.launchwrapper.LaunchClassLoader")));
            CtClass c=cp.makeClass(new ByteArrayInputStream(classfileBuffer));
            CtMethod cm=c.getMethod("handleCascadingTweak",
                                    "(Ljava/io/File;Ljava/util/jar/JarFile;Ljava/lang/String;Lnet/minecraft/launchwrapper/LaunchClassLoader;Ljava/lang/Integer;)V");
            cm.setBody(
                    "{\n"+
                    "    java.net.URL url=$1.toURI().toURL();\n" +
                    "    moe.xinmu.minecraft_agent.Utils.addClassLoaderURLs(url);\n" +
                    "    $4.addURL(url);\n" +
                    "    tweaker.injectCascadingTweak($3);\n" +
                    "    tweakSorting.put($3,$5);"+
                    "}");
            cm.addCatch(
                    "{\n" +
                        "$e.printStackTrace();\n"+
                        "return;\n"+
                    "}\n", cp.get("java.lang.Exception"));
            return c.toBytecode();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
