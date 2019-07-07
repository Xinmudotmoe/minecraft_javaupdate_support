package moe.xinmu.minecraft_agent;

import sun.misc.Unsafe;

import java.lang.reflect.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@SuppressWarnings("deprecation")//Block warnings when compiling in Java8. However, it did not take effect.
public final class Utils {
    private static Unsafe unsafe;
    public static Unsafe getUnsafe() {
        if(unsafe!=null)
            return unsafe;
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            //Should not be Utils.setAccessible .This can cause recursion.
            unsafe = (sun.misc.Unsafe) f.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        if(unsafe == null)
            throw new NullPointerException();
        return unsafe;
    }
    private static boolean isnewjdk=false;
    private static boolean isnewclassloader=false;
    private static boolean init=false;
    static {
        init();
    }
    public static boolean isNewJDK(){
        return isnewjdk;
    }
    public static void init() {
        if(!init) {
            Log.i("Utils", "Init Start.");
            try {
                Class.forName("java.lang.Module");
                isnewjdk = true;
                Log.i("Utils", "NewJDK is True.");
            } catch (ClassNotFoundException ignored) {
                Log.i("Utils", "NewJDK is False.");
            }
            ClassLoader cl = ClassLoader.getSystemClassLoader();
            if (isnewclassloader = !(cl instanceof URLClassLoader))
                Log.i("Utils", "NewClassLoader is True.");
            else
                Log.i("Utils", "NewClassLoader is False.");
            Log.i("Utils", "Init End.");
            init=true;
        }
    }
    //In new jdk. if you not add "--add-opens java.base/jdk.internal.loader=ALL-UNNAMED"
    //Then you need to be able to set accessible.
    public static void setAccessible(AccessibleObject ao,boolean flag) {
        try{
            ao.setAccessible(flag);
        }catch (/*java.lang.reflect.InaccessibleObjectException*/RuntimeException e){
            try {
                Field field = AccessibleObject.class.getDeclaredField("override");
                getUnsafe().putBoolean(ao, getUnsafe().objectFieldOffset(field), flag);
            }catch (NoSuchFieldException|SecurityException ei){
                setAccessibleDependentOnUnsafe(ao,flag);
            }
        }
    }
    private static void setAccessibleDependentOnUnsafe(AccessibleObject ao,boolean flag){
        getUnsafe().putBoolean(ao, is64Bit() ? 12 : 8, flag);
        Log.w("Utils setAccessibleDependentOnUnsafe",ao.toString());
    }
    public static boolean is64Bit(){
        return System.getProperty("os.arch").contains("64");
    }

    public static URL[] getClassLoaderURLs(){
        if(isnewclassloader){
            return Latest.getClassLoaderURLsLatest();
        }else{
            return Legacy.getClassLoaderURLsLegacy();
        }
    }
    public static void addClassLoaderURLs(URL url){
        if(isnewclassloader) {
            Latest.addClassLoaderURLsLatest(url);
        }else{
            Legacy.addClassLoaderURLsLegacy(url);
        }
    }
    // Equivalent to Objects.requireNonNullElse
    public static <T> T requireNonNullElse(T source, T elze) {
        return source == null ? Objects.requireNonNull(elze) : source;
    }
    public static boolean equalsLByte(byte[] a,byte[]b){
        if(a.length!=b.length)
            return false;
        if(a.length<21){
            for (int i = 0; i < a.length; i++)
                if(a[i]!=b[i])
                    return false;
            return true;
        }
        return equalsLByte(sha1(a),sha1(b));
    }

    public static byte[] sha1(byte[] a){
        MessageDigest mdTemp;
        try {
            mdTemp = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            // Can't find? Let it crash.
            throw new Error(e);
        }
        return mdTemp.digest(a);
    }
    public static void OpenAllModule(){
        if(Utils.isnewjdk){
            try {
                //Module
                Class<?> module_class=Objects.requireNonNull(Class.forName("java.lang.Module"));

                //public Set<String> Module.getPackages()
                final Method module_getpackages=Objects.requireNonNull(module_class.getMethod("getPackages"));

                //package-private void java.lang.Module.implAddOpensToAllUnnamed(String)
                final Method module_impladdopenstoallunnamed=Objects.requireNonNull(module_class.getDeclaredMethod("implAddOpensToAllUnnamed",String.class));
                // package-private to public
                Utils.setAccessible(module_impladdopenstoallunnamed,true);
                //ModuleLayer
                Class<?> module_layer_class=Class.forName("java.lang.ModuleLayer");

                //public static ModuleLayer ModuleLayer.boot()
                Object o=module_layer_class.getMethod("boot").invoke(null);

                //public Set<Module> ModuleLayer.modules()
                Set modules= (Set) module_layer_class.getMethod("modules").invoke(o);

                Arrays.stream(modules.toArray()).forEach(m->{
                    // Get Module
                    final Object mm=m;
                    try {
                        Set packages= (Set) module_getpackages.invoke(mm);
                        Arrays.stream(packages.toArray()).forEach(p->{
                            //p is String
                            try {
                                module_impladdopenstoallunnamed.invoke(mm,p);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        });
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                });
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | NullPointerException e) {
                Log.i("OpenAllModule","Exception(failure)",e);
            }
        }
        else{
            Log.i("OpenAllModule","Not NewJDK");
        }
    }

    public static int getJavaVersion(){
        return ((Double)(Double.parseDouble(System.getProperty("java.class.version")))).intValue();
    }

    static String agent_dir="agent_mod";

    public static String getAgent_dir() {
        return agent_dir;
    }

    //Old-style processing method.
    private static final class Legacy{
        private static URL[] getClassLoaderURLsLegacy(){
            final URLClassLoader ucl = (URLClassLoader) ClassLoader.getSystemClassLoader();
            return ucl.getURLs();
        }
        private static void addClassLoaderURLsLegacy(URL url){
            try{
                final URLClassLoader ucl = (URLClassLoader) ClassLoader.getSystemClassLoader();
                Method method=URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                setAccessible(method,true);
                method.invoke(ucl,url);
            }catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
                throw new Error(e);
            }
        }
    }
    private static final class Latest{
        private static Object latestClassPath;
        private static Method latestGetURLsMethod;
        private static Method latestAddURLsMethod;
        private static void initLatest(){
            try {
                //jdk.internal.loader.ClassLoaders.AppClassLoader
                ClassLoader cl=ClassLoader.getSystemClassLoader();
                Class<?> classLoader=cl.getClass();
                //jdk.internal.loader.ClassLoaders.AppClassLoader.ucp type:jdk.internal.loader.URLClassPath
                Field field=classLoader.getDeclaredFields()[0];
                setAccessible(field,true);//relieve internal and private
                //type:jdk.internal.loader.URLClassPath
                latestClassPath = field.get(cl);
                latestGetURLsMethod=latestClassPath.getClass().getMethod("getURLs");
                latestAddURLsMethod=latestClassPath.getClass().getMethod("addURL",URL.class);
                setAccessible(latestGetURLsMethod,true);
                setAccessible(latestAddURLsMethod,true);
            }catch (IllegalAccessException|NoSuchMethodException e) {
                throw new Error(e);
            }
        }
        private static void addClassLoaderURLsLatest(URL url) {
            if(latestClassPath==null)
                initLatest();
            try {
                latestAddURLsMethod.invoke(latestClassPath,url);
            } catch (IllegalAccessException|InvocationTargetException e) {
                throw new Error(e);
            }
        }
        private static URL[] getClassLoaderURLsLatest(){
            if(latestClassPath==null)
                initLatest();
            try {
                return (URL[])(latestGetURLsMethod.invoke(latestClassPath));
            } catch (IllegalAccessException|InvocationTargetException e) {
                throw new Error(e);
            }
        }
    }
}
