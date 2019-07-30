package moe.xinmu.minecraft_agent;

import java.io.File;
import java.lang.instrument.Instrumentation;

public final class Agent {
	public static void premain(String args, Instrumentation inst) {
		if (args != null && args.length() > 2)
			Utils.setAgent_dir_file(new File(args));
		Log.i("Agent", "Start Agent processing");
		Utils.init();
		AgentModClassLoader classLoader = new AgentModClassLoader(inst);
		inst.addTransformer(classLoader.getTransformer());
		classLoader.init();
		classLoader.main(inst);
	}

	public static void agentmain(String args, Instrumentation inst) {
		premain(args, inst);
	}
}
