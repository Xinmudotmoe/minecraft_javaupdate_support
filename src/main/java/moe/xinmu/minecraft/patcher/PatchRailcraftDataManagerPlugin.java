package moe.xinmu.minecraft.patcher;

import javassist.ClassPool;
import moe.xinmu.minecraft_agent.annotation.TargetClass;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

@TargetClass("mods.railcraft.common.plugins.forge.DataManagerPlugin")
public class PatchRailcraftDataManagerPlugin implements ClassFileTransformer {
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		//TODO
		ClassPool c;
		return null;
	}
}
