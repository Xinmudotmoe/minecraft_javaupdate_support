package moe.xinmu.minecraft_agent;

import java.lang.annotation.Inherited;
import java.net.URL;
import java.net.URLClassLoader;

//Not supported yet

@Deprecated

//TODO
public class SystemClassLoader extends URLClassLoader {
    public SystemClassLoader(ClassLoader parent){
        //TODO
        this(new URL[0], parent);
    }
    public SystemClassLoader(URL[] urls, ClassLoader parent) {
        //TODO
        super(urls, parent);
    }
}
