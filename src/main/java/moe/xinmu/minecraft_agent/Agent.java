package moe.xinmu.minecraft_agent;

import java.lang.instrument.Instrumentation;

public final class Agent {
    public static AgentModClassLoader classLoader;
    private static Instrumentation instrumentation; //future
    public static void premain(String args, Instrumentation inst){
        Log.i("Agent","Start Agent processing");
        Utils.init();
        instrumentation=inst;
        Utils.OpenAllModule();
        classLoader=new AgentModClassLoader(instrumentation);
        if(args!=null&&args.length()>2)
            Utils.agent_dir=args;
        inst.addTransformer(classLoader.getTransformer());
        classLoader.init();
        classLoader.main(inst);
    }

    public static void agentmain(String args, Instrumentation inst) {
        premain(args,inst);
    }
}
