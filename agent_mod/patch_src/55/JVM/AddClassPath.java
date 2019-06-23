package moe.xinmu.minecraft.patcher;
import java.io.File;
import java.net.MalformedURLException;

import moe.xinmu.minecraft_agent.Utils;
import moe.xinmu.minecraft_agent.annotation.Main;
import moe.xinmu.minecraft_agent.annotation.$Main;

@Main
public class AddClassPath implements $Main{
    public void main(AgentModClassLoader amcl){
        File f=new File("agent_mod/extendLibs");
        if(f.isDirectory()){
            for (File ff:f.listFiles()) {
                if(ff.getName().endsWith(".jar"))
                    try{
                        Utils.addClassLoaderURLs(ff.toURI().toURL());
                    }catch (MalformedURLException e){

                    }
            }
        }
    }
}
