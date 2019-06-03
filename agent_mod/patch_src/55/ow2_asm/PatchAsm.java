package moe.xinmu.minecraft.patcher;

import moe.xinmu.minecraft_agent.*;
import moe.xinmu.minecraft_agent.annotation.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Arrays;

@TargetClass("org.objectweb.asm.ClassReader")
public class PatchAsm implements ClassFileTransformer{
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (classfileBuffer[0x22d2] == 52) {
            classfileBuffer[0x22d2] = 64;
            return classfileBuffer;
        }
        return null;
    }
}