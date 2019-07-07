package moe.xinmu.minecraft_agent;

import moe.xinmu.minecraft_agent.annotation.$Main;
import org.mdkt.compiler.CompiledCode;
import org.mdkt.compiler.InMemoryJavaCompiler;

import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationHandler;
import java.net.*;
import java.security.ProtectionDomain;
import java.security.SecureClassLoader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class AgentModClassLoader {
    public static AgentModClassLoader INSTANCE;
    AgentModClassLoader(Instrumentation instrumentation) {
        INSTANCE = this;
        transformer = new Transformer(instrumentation);
    }
    private SecondaryClassLoader cl;
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

    @SuppressWarnings("unchecked")
    public void init(Instrumentation instrumentation) {
        List<File> ls=new ArrayList<>();
        findFile(new File(Utils.getAgent_dir()),ls);
        ls.add(new File(Agent.class.getProtectionDomain().getCodeSource().getLocation().getFile()));
        cl=SecondaryClassLoader.genSecondaryClassLoader(ls.stream().filter(a->a.getName().endsWith(".jar")).toArray(File[]::new));
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
    public static final class SecondaryClassLoader extends SecureClassLoader {
        private Map<String, CompiledCode> customCompiledCode = new ConcurrentHashMap<>();

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
                "moe.xinmu.minecraft_agent.annotation."
        );
        InMemoryJavaCompiler imjc;
        private static URL toURL(URI i){
            try {
                return i.toURL();
            } catch (MalformedURLException e) {
                return null;
            }
        }
        public static SecondaryClassLoader genSecondaryClassLoader(File[] files){
            LimitAppClassLoader lacl=new LimitAppClassLoader(String.class.getClassLoader()/*This is null()*/);
            URLClassLoader ucl=new URLClassLoader(
                    Arrays.stream(files)
                            .map(File::toURI)
                            .map(SecondaryClassLoader::toURL)
                            .filter(Objects::nonNull)
                            .toArray(URL[]::new),lacl);
            return new SecondaryClassLoader(files,ucl);
        }
        private SecondaryClassLoader(File[] urls,ClassLoader parent) {
            super(parent);
            imjc=new InMemoryJavaCompiler(this);
            imjc.ignoreWarnings();
            settingcompileclasspath(urls);

        }
        void settingcompileclasspath(File[] urls){
            StringJoiner sj=new StringJoiner(System.getProperty("path.separator"));
            List<File> af=new ArrayList<>(Arrays.asList(urls));
            af.stream().map(File::getPath).forEach(sj::add);
            imjc.useOptions("-nowarn","-g","-classpath",sj.toString());
        }
        void compile(){
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
        }

        @Override
        public Class<?> findClass(String name) throws ClassNotFoundException {
            if(name==null)
                throw new ClassNotFoundException("It`s null.");
            Class<?>c=null;
            CompiledCode cc=customCompiledCode.get(name);
            if(cc!=null)
                c=defineClass(name,cc.getByteCode());
            if(c!=null)
                return c;
            throw new ClassNotFoundException(name);
        }

        @Override
        public URL findResource(String name) {
            if(Pattern.matches(".*\\.class$",name)){
                try {
                    String nname=name.substring(0,name.length()-".class".length()).replace("/",".");
                    CompiledCode cc=Objects.requireNonNull(customCompiledCode.get(nname));
                    String spec=XinmuBase64StreamHandler.protocol+new String(Base64.getUrlEncoder().encode(cc.getByteCode()));
                    return new URL(null,spec,XinmuBase64StreamHandler.INSTANCE);
                } catch (MalformedURLException|NullPointerException e) {
                }
            }
            return null;
        }

        @Override
        public InputStream getResourceAsStream(String name) {
            InputStream is = super.getResourceAsStream(name);
            if(is!=null)
               return is;
            try {
                if(Pattern.matches(".*\\.class$",name))
                    return customCompiledCode.get(name.substring(0,name.length()-".class".length()).replace("/",".")).openInputStream();
            } catch (IOException ignored) { }
            return null;
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

    private static class LimitAppClassLoader extends ClassLoader{
        ClassLoader cl;

        LimitAppClassLoader(ClassLoader parent) {
            super(parent);
            cl=this.getClass().getClassLoader();
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            for (String i:SecondaryClassLoader.transfer)
                if (name.contains(i))
                    return cl.loadClass(name);
            return super.findClass(name);
        }
    }

    private static class XinmuBase64StreamHandler extends URLStreamHandler{
        private static final XinmuBase64StreamHandler INSTANCE=new XinmuBase64StreamHandler();
        static final String protocol="xinmu-agent-base64://";

        @Override
        protected URLConnection openConnection(URL u) throws IOException {
            return new XinmuBase64Connection(u);
        }

        static class XinmuBase64Connection extends URLConnection{
            XinmuBase64Connection(URL url) {
                super(url);
            }

            @Override
            public void connect() throws IOException {

            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(Base64.getUrlDecoder().decode(url.toExternalForm().replace(protocol,"")));
            }
        }
    }
}
