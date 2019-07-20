package moe.xinmu.minecraft_agent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.*;
@Deprecated
public final class Log {
    private static Logger log;
    static {
        log=Logger.getLogger("mc_agent");
        log.setLevel(Level.ALL);
        init();
    }
    private static volatile String filename="";
    private static FileHandler fileHandler;
    private static void flush(){
        if(fileHandler!=null)
            fileHandler.flush();
    }
    private static void init(){
        try {
            Properties prop=new Properties();
            prop.load(new FileInputStream(new File(Utils.getAgent_dir_file(),"setting.properties")));
            if(prop.containsKey("logfile")){
                String log=prop.getProperty("logfile");
                init(log);
                i("LogInit",String.format("Find the target log file %s, the initialization is successful.",log));
            }
            else{
                init(Utils.getAgent_dir_file().getAbsolutePath()+"/log.log");
                i("LogInit","Use the default log file");
            }
        } catch (IOException er) {
            e("Init","Initialization exception",er);
        }
    }
    private static void init(String file) throws IOException {
        if(!file.equals(filename)){
            filename=file;
            if(fileHandler!=null)
                log.removeHandler(fileHandler);
            fileHandler=new FileHandler(filename);
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.ALL);
            log.addHandler(fileHandler);
        }
    }

    private static String get(){
        return Arrays.stream(new Throwable().getStackTrace())
                .filter(stackTraceElement -> !stackTraceElement.getClassName().equals(Log.class.getName()))
                .findFirst().get().toString();
    }
    public static void i(String tag, String msg) {
        log.log(Level.INFO,"file: "+get()+"\t"+tag+": "+msg);
        flush();

    }
    public static void i(String tag, String msg,Throwable throwable) {
        log.log(Level.INFO,"file: "+get()+"\t"+tag+": "+msg,throwable);
        flush();
    }
    public static void w(String tag, String msg) {
        log.log(Level.WARNING,"file: "+get()+"\t"+tag+": "+msg);
        flush();
    }
    public static void w(String tag, String msg,Throwable throwable) {
        log.log(Level.WARNING,"file: "+get()+"\t"+tag+": "+msg,throwable);
        flush();
    }
    public static void e(String tag, String msg) {
        log.log(Level.SEVERE,"file: "+get()+"\t"+tag+": "+msg);
        flush();
    }
    public static void e(String tag, String msg,Throwable throwable) {
        log.log(Level.SEVERE,"file: "+get()+"\t"+tag+": "+msg,throwable);
        flush();
    }
    public static void v(String tag, String msg) {
        log.log(Level.FINEST,"file: "+get()+"\t"+tag+": "+msg);
        flush();
    }
    public static void v(String tag, String msg,Throwable throwable) {
        log.log(Level.FINEST,"file: "+get()+"\t"+tag+": "+msg,throwable);
        flush();
    }
    public static void d(String tag, String msg) {
        log.log(Level.FINE,"file: "+get()+"\t"+tag+": "+msg);
        flush();
    }
    public static void d(String tag, String msg,Throwable throwable) {
        log.log(Level.FINE,"file: "+get()+"\t"+tag+": "+msg,throwable);
        flush();
    }
}
