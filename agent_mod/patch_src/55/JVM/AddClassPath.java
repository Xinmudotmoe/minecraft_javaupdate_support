package moe.xinmu.minecraft.patcher;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.util.Objects;
import java.util.jar.JarFile;

import moe.xinmu.minecraft_agent.*;
import moe.xinmu.minecraft_agent.annotation.*;

@Main
public class AddClassPath implements $Main {
	public void main(AgentModClassLoader amcl, Instrumentation instrumentation) {
		try {
			File f = new File(Utils.getAgent_dir_file(), "extendLibs");
				if (f.isDirectory()) {
				for (File ff : Objects.requireNonNull(f.listFiles())) {
					if (ff.getName().endsWith(".jar")) {
						Utils.addClassLoaderURLs(ff.toURI().toURL());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
