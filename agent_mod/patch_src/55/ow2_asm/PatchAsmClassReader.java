package moe.xinmu.minecraft.patcher;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import moe.xinmu.minecraft_agent.Utils;
import moe.xinmu.minecraft_agent.annotation.*;

@TargetClass(
        {
                "org.objectweb.asm.ClassReader",
                "org.spongepowered.asm.lib.ClassReader"
        })
public class PatchAsmClassReader implements ClassFileTransformer{
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        try {
            ClassGen clazz=new ClassGen(new ClassParser(new ByteArrayInputStream(classfileBuffer), className).parse());
            Method m=clazz.containsMethod("<init>","([BII)V");
            Code code=m.getCode();
            InstructionList il=new InstructionList(code.getCode());
            boolean change=false;
            for (InstructionHandle i : il){
                Instruction ii=i.getInstruction();
                if(ii.getName().equals("bipush"))
                    if(((BIPUSH)ii).getValue().equals(52)) {
                        i.setInstruction(new BIPUSH((byte)Utils.getJavaVersion()));
                        change=true;
                        break;
                    }
            }
            if(change){
                code.setCode(il.getByteCode());
                return clazz.getJavaClass().getBytes();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
