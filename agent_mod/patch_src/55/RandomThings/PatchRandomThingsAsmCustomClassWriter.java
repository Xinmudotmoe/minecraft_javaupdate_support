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

@TargetClass("lumien.randomthings.asm.CustomClassWriter")
public class PatchRandomThingsAsmCustomClassWriter implements ClassFileTransformer{
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        try{
            ClassGen cg=new ClassGen(new ClassParser(new ByteArrayInputStream(classfileBuffer),className).parse());
            ConstantPoolGen cpg=cg.getConstantPool();
            Method method=cg.containsMethod("<clinit>","()V");
            InstructionList i2=new InstructionList();
            i2.append(new INVOKESTATIC(cpg.addMethodref(className,"_init$","()V")));
            i2.append(new RETURN());
            method.getCode().setCode(i2.getByteCode());
            Method mm=new Method(java.lang.reflect.Modifier.STATIC|java.lang.reflect.Modifier.PRIVATE,cpg.addUtf8("_init$"),cpg.addUtf8("()V"),new Attribute[]{new Code(method.getCode())},cpg.getConstantPool());
            cg.addMethod(mm);
            InstructionList i1=new InstructionList();
            i1.append(new RETURN());
            mm.getCode().setCode(i1.getByteCode());
            ClassPool cp=ClassPool.getDefault();
            CtClass c=cp.makeClass(new ByteArrayInputStream(cg.getJavaClass().getBytes()));
            CtMethod cm=c.getMethod("_init$","()V");
            cm.setBody("{" +
                    "customClassLoader=new java.net.URLClassLoader(moe.xinmu.minecraft_agent.Utils.getClassLoaderURLs());"+
                    "return;"+
                    "}");
            return c.toBytecode();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}