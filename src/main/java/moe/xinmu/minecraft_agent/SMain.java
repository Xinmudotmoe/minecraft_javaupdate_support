package moe.xinmu.minecraft_agent;

import moe.xinmu.minecraft_agent.annotation.$Main;
import moe.xinmu.minecraft_agent.annotation.Main;
import moe.xinmu.minecraft_agent.annotation.TargetClass;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;

public class SMain implements $Main {
    public void main(AgentModClassLoader amcl, Instrumentation instrumentation) {
        String[] s=amcl.compile();
        ClassLoader cl=getClass().getClassLoader();
        for (String name:s) {
            try{
                Class<?> c=cl.loadClass(name);
                TargetClass tc=c.getDeclaredAnnotation(TargetClass.class);
                Main m=c.getDeclaredAnnotation(Main.class);
                Object o=null;
                if(tc!=null||m!=null)
                    o=c.getConstructor().newInstance();
                if(tc!=null){
                    if(tc.value().length==0)
                        amcl.addClassFilePolling((ClassFileTransformer) o);
                    else
                        for (String key:tc.value())
                            amcl.addClassFileTransformer(key,(ClassFileTransformer) o);
                }
                if(m!=null)
                    (($Main)o).main(amcl,instrumentation);
            }catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException | ClassCastException e){
                e.printStackTrace(System.err);
            }
        }
    }
}
