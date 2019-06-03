package moe.xinmu.minecraft_agent;

import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class Transformer  implements ClassFileTransformer {

    private ConcurrentHashMap<String, Set<ClassFileTransformer>> map=new ConcurrentHashMap<>();
    private List<ClassFileTransformer> polling= Collections.synchronizedList(new ArrayList<>());
    public static PrintWriter os;
    Transformer(){
        try {
            os=new PrintWriter(new BufferedOutputStream(new FileOutputStream(new File("G:\\a.log"))));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private boolean flag=false;
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
                            System.out.println("Change "+className+" by "+c.getClass().getName());
                            return by;
                        }
                }catch (IllegalClassFormatException ignored){
                }
        }catch (Throwable t){
            t.printStackTrace(System.err);
        }
        return null;
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
