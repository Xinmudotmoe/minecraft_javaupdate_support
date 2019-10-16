package moe.xinmu.minecraft.patcher;

import moe.xinmu.minecraft_agent.annotation.TargetClass;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

@TargetClass("pl.asie.foamfix.client.Deduplicator")
public class Foamfix_OpenModule implements ClassFileTransformer {
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
		unlock_module();
		return null;
	}
	public void unlock_module(){
		ModuleOpenHelper.OpenModule("java.base","jdk.internal.ref");
	}
}
