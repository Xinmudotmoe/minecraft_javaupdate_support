package moe.xinmu.minecraft_agent;

import org.mdkt.compiler.DynamicClassLoader;
import org.mdkt.compiler.InMemoryJavaCompiler;

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.regex.Pattern;

import moe.xinmu.minecraft_agent.annotation.*;

public final class AgentModClassLoader {
    public static AgentModClassLoader INSTANCE;
    private Transformer transformer;
    private DynamicClassLoader cl;
    private InMemoryJavaCompiler imjc;
    private static List<String> transfer=Arrays.asList(
            // like this
            //            BootClassLoader   <------------\
            //                                          PlatformClassLoader
            //          LimitAppClassLoader --transfer--> AppClassLoader
            //            URLClassLoader
            //         SecondaryClassLoader(InMemoryCompiler)
            AgentModClassLoader.class.getName(),
            Agent.class.getName(),
            Transformer.class.getName(),
            Utils.class.getName(),
            Log.class.getName(),

            $Main.class.getName(),
            Main.class.getName(),
            TargetClass.class.getName()
    );

    AgentModClassLoader(Instrumentation instrumentation) {
        INSTANCE = this;
        transformer = new Transformer(instrumentation);
        imjc = InMemoryJavaCompiler.newInstance();
        imjc.ignoreWarnings();
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return cl.loadClass(name);
    }

    private static void findFile(File f,List<File>ff){
        if(f==null||!f.isDirectory())
            return;
        File[] fs=Utils.requireNonNullElse(f.listFiles(),new File[0]);
        Arrays.stream(fs).filter(File::isDirectory).forEach(fz->findFile(fz,ff));
        Arrays.stream(fs).filter(File::isFile).forEach(ff::add);
    }

    public void init() {
        List<File> ls=new ArrayList<>();
        findFile(new File(Utils.getAgent_dir()),ls);
        ls.add(new File(Agent.class.getProtectionDomain().getCodeSource().getLocation().getFile()));
        settingcompileclasspath(ls.stream().filter(a->a.getName().endsWith(".jar")).toArray(File[]::new));
    }

    @SuppressWarnings("unchecked")
    public void main(Instrumentation instrumentation){
        try {
            ((Class<$Main>)cl.loadClass(SMain.class.getName())).getConstructor().newInstance().main(this,instrumentation);
        }catch(Exception e){
            throw new Error(e);
        }
    }

    public ClassFileTransformer getTransformer() {
        return transformer;
    }

    public void addClassFileTransformer(String classname, ClassFileTransformer classFileTransformer) {
        transformer.addClassFileTransformer(classname, classFileTransformer);
    }

    public String[] compile(){
        ArrayList<File> al=new ArrayList<>();
        String src=Utils.agent_dir +"/patch_src/";
        for (int i = 0; i <= Utils.getJavaVersion(); i++) {
            File f=new File(src+i);
            if(f.isDirectory())
                findFile(f,al);
        }
        File[] a=al.stream().filter(f->f.getName().endsWith(".java")).toArray(File[]::new);
        for (File f:a) {
            try {
                String ff=f.getCanonicalPath()
                        .replace(".java","")
                        .replace(":","")
                        .replace("\\","/");
                imjc.addSource(ff,String.join("\n",new BufferedReader(new InputStreamReader(new FileInputStream(f))).lines().toArray(String[]::new)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            imjc.compileAll();
        } catch (Exception e) {
            throw new Error(e);
        }
        return cl.getCompiledClazzName();
    }

    private void settingcompileclasspath(File[] urls){
        StringJoiner sj = new StringJoiner(System.getProperty("path.separator"));
        List<File> af = new ArrayList<>(Arrays.asList(urls));
        af.stream().map(File::getPath).forEach(sj::add);
        imjc.useOptions("-nowarn","-g","-classpath",sj.toString());
        cl = imjc.useParentClassLoader(genSubClassLoader(af.toArray(new File[0]))).getClassloader();
    }

    private static URLClassLoader genSubClassLoader(File[] files){
        LimitAppClassLoader lacl=new LimitAppClassLoader(String.class.getClassLoader()/*This is null*/);
        return new URLClassLoader(
                Arrays.stream(files)
                        .map(File::toURI)
                        .map(AgentModClassLoader::toURL)
                        .filter(Objects::nonNull)
                        .toArray(URL[]::new),lacl);
    }

    private static URL toURL(URI i){
        try {
            return i.toURL();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private static class LimitAppClassLoader extends ClassLoader{
        ClassLoader cl;

        LimitAppClassLoader(ClassLoader parent) {
            super(parent);
            cl=this.getClass().getClassLoader();
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            if (transfer.contains(name))
                return cl.loadClass(name);
            return super.findClass(name);
        }

        @Override
        protected URL findResource(String name) {
            if(Pattern.matches(".*\\.class$",name))
                if (transfer.contains(name.substring(0,name.length()-".class".length()).replace("/",".")))
                    return cl.getResource(name);
            return null;
        }
    }
}
