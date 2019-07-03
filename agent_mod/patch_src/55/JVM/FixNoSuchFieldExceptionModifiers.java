package moe.xinmu.minecraft.patcher;

import java.lang.instrument.*;
import java.util.*;
import moe.xinmu.minecraft_agent.*;
import moe.xinmu.minecraft_agent.annotation.*;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;

@Main
public class FixNoSuchFieldExceptionModifiers implements $Main {
    public void main(AgentModClassLoader amcl, Instrumentation instrumentation){
        try{
            java.lang.reflect.Field.class.getDeclaredField("modifiers");
        }catch (NoSuchFieldException e){
            try {
                plan_asm(instrumentation);
            }
            catch (Exception e21){
                e21.printStackTrace();
            }
        }
    }
    private void plan_asm(Instrumentation instrumentation) throws Exception{
        ClassReader cr=new ClassReader(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("jdk/internal/reflect/Reflection.class")));
        ClassWriter cw=new ClassWriter(cr,ClassWriter.COMPUTE_MAXS);
        cr.accept(new ClassVisitor(Opcodes.ASM5,cw){
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                if(name.equals("filterFields")||name.equals("filterMethods")) {
                    GeneratorAdapter ga=new GeneratorAdapter(super.visitMethod(access, name, desc, signature, exceptions),access,name,desc);
                    ga.visitVarInsn(Opcodes.ALOAD,1);
                    ga.returnValue();
                    ga.endMethod();
                    return null;
                }
                return super.visitMethod(access, name, desc, signature, exceptions);
            }
        },0);
        cw.visitEnd();
        byte[] b=cw.toByteArray();
        instrumentation.redefineClasses(new ClassDefinition(Class.forName("jdk.internal.reflect.Reflection"), b));
    }
}