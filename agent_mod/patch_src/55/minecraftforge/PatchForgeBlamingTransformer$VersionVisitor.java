package moe.xinmu.minecraft.patcher;

import moe.xinmu.minecraft_agent.annotation.TargetClass;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

@TargetClass("net.minecraftforge.fml.common.asm.transformers.BlamingTransformer$VersionVisitor")
public class PatchForgeBlamingTransformer$VersionVisitor implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        ClassReader cr=new ClassReader(classfileBuffer);
        ClassWriter cw=new ClassWriter(cr,ClassWriter.COMPUTE_MAXS);
        cr.accept(new ClassVisitor(Opcodes.ASM5,cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                if(name.equals("visit")){
                    GeneratorAdapter ga=new GeneratorAdapter(super.visitMethod(access, name, desc, signature, exceptions),access,name,desc);
                    ga.returnValue();
                    ga.endMethod();
                    return null;
                }
                return super.visitMethod(access, name, desc, signature, exceptions);
            }
        },0);
        return cw.toByteArray();
    }
}
