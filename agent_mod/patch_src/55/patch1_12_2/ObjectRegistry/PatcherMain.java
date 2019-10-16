package moe.xinmu.minecraft.patcher.patch1122;

import moe.xinmu.minecraft.patcher.PatchLaunch;
import moe.xinmu.minecraft_agent.AgentModClassLoader;
import moe.xinmu.minecraft_agent.SMain;
import moe.xinmu.minecraft_agent.Utils;
import moe.xinmu.minecraft_agent.annotation.$Main;
import moe.xinmu.minecraft_agent.annotation.Main;
import moe.xinmu.minecraft_agent.version.MinecraftVersion;


import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.Objects;
//issus: Opening will cause TOP Addons error
@Main
public class PatcherMain implements $Main {
	@Override
	public void main(AgentModClassLoader amcl, Instrumentation instrumentation) {
		if(!MinecraftVersion.chechVersion(MinecraftVersion.V1_12_2))
			return;
		for (String s:SMain.INSTANCE.class_names) {
			if(s.contains("moe.xinmu.minecraft.patcher.patch1122.P1122ObjectRegistry")){
				String name=s.replace(".","/").concat(".class");
				try {
					Utils.TempJar.INSTANCE.addFile(name, Objects.requireNonNull(amcl.getClassLoader().getResourceAsStream(name)));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		String classname=Patch1122Util.GenIClassName(moe.xinmu.minecraft.patcher.patch1122.P1122ObjectRegistry.class);
		Utils.TempJar.INSTANCE.addFile(classname.replace(".","/").concat(".class"), Patch1122Util.GenIClassTransformer(moe.xinmu.minecraft.patcher.patch1122.P1122ObjectRegistry.class));
		SMain.INSTANCE.registerLimitTransformer(P1122T.target,new P1122T());
		PatchLaunch.addClassLoaderExclusion.add("moe.xinmu.minecraft.patcher.patch1122.P1122ObjectRegistry");
		PatchLaunch.registerTransformer.add(classname);
	}
}
