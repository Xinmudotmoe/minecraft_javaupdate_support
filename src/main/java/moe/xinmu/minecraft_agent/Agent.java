package moe.xinmu.minecraft_agent;

import java.lang.instrument.Instrumentation;

public class Agent {
    public static AgentModClassLoader classLoader;
    private static Instrumentation instrumentation; //future
    public static void premain(String args, Instrumentation inst){
        Log.i("Agent","Start Agent processing");
        Utils.init();
        instrumentation=inst;
        Utils.OpenAllModule();
        classLoader=new AgentModClassLoader();
        inst.addTransformer(classLoader.getTransformer());
        classLoader.init(inst);

        }
}
