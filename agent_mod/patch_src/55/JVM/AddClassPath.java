package moe.xinmu.minecraft.patcher;
import java.io.File;
import java.net.MalformedURLException;

import moe.xinmu.minecraft_agent.*;
import moe.xinmu.minecraft_agent.annotation.*;
@Main
public class AddClassPath implements $Main{
    public void main(AgentModClassLoader amcl){
        File f=new File(Utils.getAgent_dir()+"/extendLibs");
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
