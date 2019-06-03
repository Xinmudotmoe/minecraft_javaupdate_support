package moe.xinmu.minecraft_agent;

import java.io.*;
import java.security.SecureClassLoader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

//Equivalent to re-creating a URLClassLoader
public class ZipsClassLoader extends SecureClassLoader {
    private Map<String, InputStream> m=new ConcurrentHashMap<>();
    public ZipsClassLoader(File[] files, ClassLoader parent){
        super(parent);
        for (File f:files)
            try {
                ZipFile zf=new ZipFile(f);
                for (ZipEntry z:zf.stream().toArray(ZipEntry[]::new))
                    m.put(z.getName(),zf.getInputStream(z));
            } catch (IOException e) {
                e.printStackTrace();
            }

    }
    private Map<String,Class<?>> sc=new ConcurrentHashMap<>();
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if(sc.containsKey(name))
            return sc.get(name);
        String newname=name.replace(".","/")+".class";
        if(!m.containsKey(newname))
            throw new ClassNotFoundException("Not Found."+name);
        try {
            Class<?> c=defineClass(name,readAllBytes(m.get(newname)));
            sc.put(name,c);
            return c;
        } catch (IOException e) {
            throw new ClassNotFoundException("Can't load",e);
        }
    }

    private Class<?> defineClass(String name, byte[] b) {
        return defineClass(name, b, 0, b.length);
    }
    private static byte[] readAllBytes(InputStream is)throws IOException{
        ByteArrayOutputStream ous = null;
        try {
            byte[] buffer = new byte[4096];
            ous = new ByteArrayOutputStream();
            int read;
            while ((read = is.read(buffer)) != -1) {
                ous.write(buffer, 0, read);
            }
        }finally {
            try {
                if (ous != null)
                    ous.close();
            } catch (IOException e) {
            }

            try {
                is.close();
            } catch (IOException e) {
            }
        }
        return ous.toByteArray();
    }
}
