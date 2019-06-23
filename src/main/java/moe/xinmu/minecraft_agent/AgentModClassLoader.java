package moe.xinmu.minecraft_agent;

import moe.xinmu.minecraft_agent.annotation.$Main;
import org.mdkt.compiler.CompiledCode;
import org.mdkt.compiler.InMemoryJavaCompiler;

import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationHandler;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AgentModClassLoader {
    public static AgentModClassLoader INSTANCE;
    AgentModClassLoader() {
        INSTANCE = this;
        transformer = new Transformer();
    }
    private SecondaryClassLoader cl;
    private static void findFile(File f,List<File>ff){
        if(f==null||!f.isDirectory())
            return;
        File[] fs=Utils.requireNonNullElse(f.listFiles(),new File[0]);
        Arrays.stream(fs).filter(File::isDirectory).forEach(fz->findFile(fz,ff));
        Arrays.stream(fs).filter(File::isFile).forEach(ff::add);
    }
    @SuppressWarnings("unchecked")
    public void init(Instrumentation instrumentation) {
        List<File> ls=new ArrayList<>();
        findFile(new File("agent_mod"),ls);
        ArrayList<File> urlss=new ArrayList<>();
        ls.stream()
                .filter(a->a.getName().endsWith(".jar"))
                .forEach(urlss::add);

        urlss.add(new File(Agent.class.getProtectionDomain().getCodeSource().getLocation().getFile()));
        File[]a=urlss.stream().map(File::getAbsoluteFile).toArray(File[]::new);
        urlss.clear();
        urlss.addAll(Arrays.asList(a));
        System.out.println(urlss);
        cl=new SecondaryClassLoader(urlss.toArray(new File[0]));
        try {
            ((Class<$Main>)cl.loadClass(SMain.class.getName())).getConstructor().newInstance().main(this,instrumentation);
        }catch(Exception e){
            throw new Error(e);
        }
    }
    private Transformer transformer;
    public ClassFileTransformer getTransformer() {
        return transformer;
    }
    public void addClassFileTransformer(String classname, ClassFileTransformer classFileTransformer) {
        transformer.addClassFileTransformer(classname, classFileTransformer);
    }
    public String[] compile(){
        cl.compile();
        return cl.getCompiledClazzName();
    }
    public void addClassFilePolling(ClassFileTransformer classFileTransformer){
        transformer.addClassFilePolling(classFileTransformer);
    }
    public static final class SecondaryClassLoader extends ZipsClassLoader{
        private Map<String, CompiledCode> customCompiledCode = new ConcurrentHashMap<>();
        private static List<String> checkraw=Arrays.asList(
                // Allow most jdk classes to be accessible
                "java.",
                "javax.",
                "jdk.",
                "sun.",
                // Allow self own class library to run
                "moe.xinmu.minecraft_agent.",
                // Allow in-memory compilation library to run
                "org.mdkt.compiler"
                // Most classes related to Minecraft are forbidden because they are modified by the proxy mechanism when the class is loaded.
                // Loading in advance will result in no modification.
        );
        private static List<String> blocklist=Arrays.asList(
                "net.minecraft.",
                "net.minecraftforge.",
                Instrumentation.class.getName(),
                "sun.instrument.",
                "org.objectweb.asm."
        );

        private static List<String> reversecheck=Arrays.asList(
                //By classifying the classloader, all classes are loaded in the following order.
                //          BootClassLoader
                // SecondaryClassLoader(InMemoryCompiler)
                // SecondaryClassLoader(ZipsClassLoader)
                //        PlatformClassLoader
                //          AppClassLoader
                //Allow the following classes to take precedence over the JVM class loading mechanism
                //Objects constructed by different class loaders cannot be converted
                // like this
                //          BootClassLoader
                //  SecondaryClassLoader(InMemoryCompiler)
                //        PlatformClassLoader
                //          AppClassLoader
                //  SecondaryClassLoader(ZipsClassLoader)
                "java.",
                "javax.",
                "jdk.",
                AgentModClassLoader.class.getName(),
                Agent.class.getName(),
                Transformer.class.getName(),
                ClassFileTransformer.class.getName(),
                Utils.class.getName(),
                Log.class.getName(),
                "moe.xinmu.minecraft_agent.annotation."

        );
        InMemoryJavaCompiler imjc;
        SecondaryClassLoader(File[] urls) {
            super(urls,null);
            imjc=new InMemoryJavaCompiler(this);
            settingcompileclasspath(urls);

        }
        void settingcompileclasspath(File[] urls){
            StringJoiner sj=new StringJoiner(System.getProperty("path.separator"));
            Arrays.stream(urls).map(File::getPath).forEach(sj::add);
            imjc.useOptions("-classpath",sj.toString());
        }
        void compile(){
            ArrayList<File> al=new ArrayList<>();
            for (int i = 0; i <= Utils.getJavaVersion(); i++) {
                File f=new File("agent_mod/patch_src/"+i);
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
            imjc.ignoreWarnings();
            try {
                imjc.compileAll();
            } catch (Exception e) {
                throw new Error(e);
            }
        }
        private boolean checkClassName(String name){
            if(blocklist.stream().anyMatch(name::startsWith))
                return false;
            return checkraw.stream().anyMatch(name::startsWith);
        }
        private boolean reversePriority(String name){
            return reversecheck.stream().anyMatch(name::startsWith);
        }

        @Override
        public Class<?> findClass(String name) throws ClassNotFoundException {
            if(name==null)
                throw new ClassNotFoundException("It`s null.");
            Class<?>c=null;
            CompiledCode cc=customCompiledCode.get(name);
            if(cc!=null)
                c=defineClass(name,cc.getByteCode());
            try{
                if(c==null)
                    if (reversePriority(name))
                        c = this.getClass().getClassLoader().loadClass(name);
            }catch (ClassNotFoundException ignored){

            }
            try{
                if(c==null)
                    c=super.findClass(name);
            }catch (ClassNotFoundException ignored){
            }
            try{
                if(c==null)
                    if(checkClassName(name))
                        c = this.getClass().getClassLoader().loadClass(name);
            }catch (ClassNotFoundException ignored){
            }
            if(c!=null)
                return c;
            throw new ClassNotFoundException("Intercept  Block or NotFound"+ name);
        }

        @Override
        public URL getResource(String resName) {
            return super.getResource(resName);//TODO
        }
        private Class<?> defineClass(String name, byte[] b) throws ClassFormatError {
            return defineClass(name, b, 0, b.length);
        }

        public void addCode(CompiledCode cc) {
            customCompiledCode.put(cc.getName(), cc);
        }
        String[] getCompiledClazzName(){
            return customCompiledCode.keySet().toArray(new String[0]);
        }
    }
}
