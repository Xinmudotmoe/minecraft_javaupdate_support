package moe.xinmu.minecraft.patcher;

import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.*;

import org.apache.bcel.Const;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import moe.xinmu.minecraft_agent.*;
import moe.xinmu.minecraft_agent.annotation.*;

@TargetClass("net.minecraft.launchwrapper.Launch")
public class PatchLaunch implements ClassFileTransformer{
    byte[]cache;
    public final static List<String> link=Collections.synchronizedList(new ArrayList<>());
    static {
        link.add("moe.xinmu.minecraft_agent.Utils");
    }
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        try {
            if(cache!=null)
                return cache.clone();
            if(!Utils.equalsLByte(Utils.sha1(classfileBuffer),new byte[]{-70, 13, 49, -92, -88, -49, -70, 31, 92, 12, -112, -62, 0, -112, -51, -71, 80, -85, 14, -122}))
                return null;
            JavaClass jc=new ClassParser(new ByteArrayInputStream(classfileBuffer),className).parse();
            ClassGen cg=new ClassGen(jc);
            ConstantPoolGen cpg=cg.getConstantPool();
            int li2=cpg.addMethodref("moe/xinmu/minecraft_agent/Utils","getClassLoaderURLs","()[Ljava/net/URL;");
            int li3=cpg.addFieldref("net/minecraft/launchwrapper/Launch","classLoader","Lnet/minecraft/launchwrapper/LaunchClassLoader;");
            int li4=cpg.addMethodref("net/minecraft/launchwrapper/LaunchClassLoader","addClassLoaderExclusion","(Ljava/lang/String;)V");
            int li5=cpg.addFieldref("net/minecraft/launchwrapper/Launch","classLoader","Lnet/minecraft/launchwrapper/LaunchClassLoader;");
            Method m=cg.containsMethod("<init>","()V");
            if(m.isPrivate()){
                m.isPrivate(false);
                m.isPublic(true);
            }
            Code c=m.getCode();
            InstructionList  il=new InstructionList();
            il.append(new ALOAD(0));
            il.append(new INVOKESPECIAL(cpg.addMethodref("java/lang/Object","<init>","()V")));
            il.append(new ALOAD(0));

            il.append(new NEW(cpg.addClass("net/minecraft/launchwrapper/LaunchClassLoader")));
            il.append(new DUP());
            il.append(new INVOKESTATIC(li2));

            il.append(new INVOKESPECIAL(cpg.addMethodref("net/minecraft/launchwrapper/LaunchClassLoader","<init>","([Ljava/net/URL;)V")));
            il.append(new PUTSTATIC(li5));
            il.append(new NEW(cpg.addClass("java/util/HashMap")));
            il.append(new DUP());
            il.append(new INVOKESPECIAL(cpg.addMethodref("java/util/HashMap","<init>","()V")));
            il.append(new PUTSTATIC(cpg.addFieldref("net/minecraft/launchwrapper/Launch","blackboard","Ljava/util/Map;")));
            il.append(new INVOKESTATIC(cpg.addMethodref("java/lang/Thread","currentThread","()Ljava/lang/Thread;")));
            il.append(new GETSTATIC(li5));
            il.append(new INVOKEVIRTUAL(cpg.addMethodref("java/lang/Thread","setContextClassLoader","(Ljava/lang/ClassLoader;)V")));
            for (String s:link) {
                il.append(new GETSTATIC(li3));
                il.append(new LDC(cpg.addString(s)));
                il.append(new INVOKEVIRTUAL(li4));
            }
            il.append(new RETURN());
            c.setCode(il.getByteCode());
            c.setMaxLocals(2);
            c.setMaxStack(4);
            c.setAttributes(Arrays.stream(c.getAttributes())
                    .filter(a->a.getTag()!=Const.ATTR_LINE_NUMBER_TABLE)
                    .filter(a->a.getTag()!=Const.ATTR_LOCAL_VARIABLE_TABLE)
                    .toArray(Attribute[]::new));
            cache=cg.getJavaClass().getBytes();
            return cache;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
