package moe.xinmu.minecraft_agent;

import java.io.*;
import java.lang.instrument.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class Transformer implements ClassFileTransformer {

    private ConcurrentHashMap<String, Set<ClassFileTransformer>> map=new ConcurrentHashMap<>();
    private List<ClassFileTransformer> polling= Collections.synchronizedList(new ArrayList<>());
    private Instrumentation instrumentation;
    private Method findLoadedClass;
    private PrintStream err=System.err;
    Transformer(Instrumentation instrumentation){
        this.instrumentation=instrumentation;
    }
    private boolean loadFindLoadedClass(){
        if(findLoadedClass==null)
            try{
                findLoadedClass=ClassLoader.class.getDeclaredMethod("findLoadedClass",String.class);
                    Utils.setAccessible(findLoadedClass,true);
            }catch (NoSuchMethodException e){
                return false;
            }
        return true;
    }
    private Class<?> findLoadedClass(ClassLoader cl,String s){
        try {
            if(findLoadedClass!=null)
                return (Class<?>)findLoadedClass.invoke(cl,s);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return Transformer.class;
    }
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain
    protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        try{
            if(className==null)
                return null;
            ArrayList<ClassFileTransformer>al=new ArrayList<>();
            if(map.containsKey(className))
                al.addAll(Utils.requireNonNullElse(map.get(className),new HashSet<>()));
            al.addAll(polling);
            byte[] a=classfileBuffer.clone();
            for (ClassFileTransformer c:al)
                try{
                    byte[] by=c.transform(loader,className,classBeingRedefined,protectionDomain,a);
                    if(!(by==null||by.length<10))
                        if(!Utils.equalsLByte(classfileBuffer,by)){
                            String clazzName=className.replace("/",".");
                            Class target=findLoadedClass(loader,clazzName);
                            if(loadFindLoadedClass()&&(target==null||Transformer.class.equals(target))){
                                err.printf("Change `%s` by `%s`.\n",clazzName,c.getClass().getName());
                                return by;
                            }else{
                                err.printf("Warning, `%s` has been loaded before the transition and will attempt to redefine this class. The converter is `%s`.\n",clazzName,c.getClass().getName());
                                redefine(target,by);
                                return null;
                            }
                }
                }catch (IllegalClassFormatException ignored){
                }
        }catch (Throwable t){
            t.printStackTrace(err);
        }
        return null;
    }
    private void redefine(Class c, byte[] data){
        try {
            instrumentation.redefineClasses(new ClassDefinition(c,data));
        } catch (ClassNotFoundException | UnmodifiableClassException e) {
            e.printStackTrace(err);
        }
    }
    void addClassFilePolling(ClassFileTransformer classFileTransformer){
        polling.add(classFileTransformer);
    }
    void addClassFileTransformer(String classname,ClassFileTransformer classFileTransformer){
        classname=classname.replace(".","/");
        if(!map.containsKey(classname))
            map.put(classname,new HashSet<>());
        map.get(classname).add(classFileTransformer);
    }
}
