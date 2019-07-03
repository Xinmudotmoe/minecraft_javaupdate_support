package moe.xinmu.minecraft_agent;

import moe.xinmu.minecraft_agent.annotation.$Main;
import moe.xinmu.minecraft_agent.annotation.Main;
import moe.xinmu.minecraft_agent.annotation.TargetClass;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public final class SMain implements $Main {
    public void main(AgentModClassLoader amcl, Instrumentation instrumentation) {
        String[] s=amcl.compile();
        ArrayList<Class<? extends $Main>> mains=new ArrayList<>();
        for (String name:s) {
            try{
                Class<?> c=amcl.loadClass(name);
                TargetClass tc=c.getDeclaredAnnotation(TargetClass.class);
                Main m=c.getDeclaredAnnotation(Main.class);
                if(tc!=null){
                    ClassFileTransformer o = (ClassFileTransformer) c.getConstructor().newInstance();
                    if(tc.value().length==0)
                        instrumentation.addTransformer(o);
                    else
                        for (String key:tc.value())
                            amcl.addClassFileTransformer(key, o);
                }
                if(m!=null)
                    mains.add(c.asSubclass($Main.class));
            }catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException | ClassCastException e){
                e.printStackTrace(System.err);
            }
        }
        for(Class<? extends $Main> main:mains) {
            try {
                main.getConstructor().newInstance().main(amcl, instrumentation);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }
}
