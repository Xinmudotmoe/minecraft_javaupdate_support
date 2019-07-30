package moe.xinmu.minecraft.patcher;

import moe.xinmu.minecraft_agent.Utils;
import moe.xinmu.minecraft_agent.annotation.Main;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public class ModuleOpenHelper {
	private static Method implAddOpensToAllUnnamed;
	private static ModuleLayer moduleLayer = ModuleLayer.boot();
	private static PrintStream err=System.err;
	public static void OpenModule(String module, String packageName) {
		if (implAddOpensToAllUnnamed == null) {
			try {
				implAddOpensToAllUnnamed = Objects.requireNonNull(Module.class.getDeclaredMethod(
						"implAddOpensToAllUnnamed",
						String.class));
				Utils.setAccessible(implAddOpensToAllUnnamed, true);
			} catch (NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
		}

		Module modules;
		{
			var a = moduleLayer.findModule(module);
			if (!a.isPresent())
				throw new RuntimeException(String.format("Not Found Module `%s`", module));
			modules = a.get();
		}
		try {
			implAddOpensToAllUnnamed.invoke(modules, packageName);
			err.printf("Module `%s` Package `%s` is Open In `%s` .\n",module,packageName,
					new Error().getStackTrace()[1].getClassName());
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException();
		}
	}
}
