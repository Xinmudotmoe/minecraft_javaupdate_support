package moe.xinmu.minecraft.patcher;

import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.lang.instrument.*;
import java.util.*;
import javassist.*;
import org.apache.bcel.Const;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import moe.xinmu.minecraft_agent.*;
import moe.xinmu.minecraft_agent.annotation.*;

@Main
public class FixNoSuchFieldExceptionModifiers implements $Main {
    public void main(AgentModClassLoader amcl, Instrumentation instrumentation){
        try{
            Field.class.getDeclaredField("modifiers");
        }catch (NoSuchFieldException e){
            try{
                ClassGen cg=new ClassGen(org.apache.bcel.Repository.getRepository().loadClass(Class.forName("jdk.internal.reflect.Reflection")));
                Method m=cg.containsMethod("filterFields","(Ljava/lang/Class;[Ljava/lang/reflect/Field;)[Ljava/lang/reflect/Field;");
                InstructionList il=new InstructionList();
                il.append(new ALOAD(1));
                il.append(new RETURN());
                m.getCode().setCode(il.getByteCode());
                m.setAttributes(new Attribute[0]);
                instrumentation.redefineClasses(new ClassDefinition(Class.forName("jdk.internal.reflect.Reflection"),cg.getJavaClass().getBytes()));
                Utils.OpenAllModule();
            }catch (Exception e1){
            }
        }
    }
}