package moe.xinmu.minecraft_agent.annotation;

import java.lang.instrument.Instrumentation;
import moe.xinmu.minecraft_agent.AgentModClassLoader;

public interface $Main{
    default void main(AgentModClassLoader amcl){}
    default void main(AgentModClassLoader amcl, Instrumentation instrumentation){main(amcl);}
}
