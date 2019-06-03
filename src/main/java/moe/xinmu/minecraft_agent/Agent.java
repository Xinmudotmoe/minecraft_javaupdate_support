package moe.xinmu.minecraft_agent;

import java.lang.instrument.Instrumentation;
import java.util.Arrays;

public class Agent {
    public static AgentModClassLoader classLoader;
    public static void premain(String args, Instrumentation inst){
        Log.i("Agent","Start Agent processing");
        Utils.init();
        Utils.OpenAllModule();
        classLoader=new AgentModClassLoader();
        inst.addTransformer(classLoader.getTransformer());
        classLoader.init();
        inst.addTransformer(classLoader.getTransformer());
        System.out.println(inst);
    }
}
