package moe.xinmu.minecraft.patcher;
import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import moe.xinmu.minecraft_agent.annotation.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;

@TargetClass("net.minecraftforge.fml.common.registry.ItemStackHolderRef")
public class PatchItemStackHolderRef implements ClassFileTransformer{
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        try{
            ClassReader cr=new ClassReader(new ByteArrayInputStream(classfileBuffer));
            ClassWriter cw=new ClassWriter(cr,1);
            cr.accept(new ClassVisitor(Opcodes.ASM5,cw) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                    if(name.equals("makeWritable")) {
                        GeneratorAdapter ga=new GeneratorAdapter(super.visitMethod(access, name, desc, signature, exceptions),access,name,desc);
                        ga.returnValue();
                        ga.endMethod();
                        return null;
                    }
                    return super.visitMethod(access, name, desc, signature, exceptions);
                }
            },0);
            return cw.toByteArray();
        }   catch (Throwable e){
            e.printStackTrace();
        }
        return null;
    }
}
