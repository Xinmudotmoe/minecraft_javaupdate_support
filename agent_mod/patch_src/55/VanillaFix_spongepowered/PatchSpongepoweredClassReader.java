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

@TargetClass("org.spongepowered.asm.lib.ClassReader")
public class PatchSpongepoweredClassReader implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if(classfileBuffer[0x2f21]==52){
            classfileBuffer[0x2f21]= (byte) (Utils.getJavaVersion());
            return classfileBuffer;
        }
        return null;
    }
}
