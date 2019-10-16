package moe.xinmu.minecraft_agent;

import sun.misc.Unsafe;

import java.io.*;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.zip.*;

@SuppressWarnings({"deprecation", "unused"})//Block warnings when compiling in Java8. However, it did not take effect.
public final class Utils {
	private static Unsafe unsafe;

	@SuppressWarnings({"unused", "WeakerAccess"})
	public static Unsafe getUnsafe() {
		if (unsafe != null)
			return unsafe;
		try {
			Field f = Unsafe.class.getDeclaredField("theUnsafe");
			f.setAccessible(true);
			//Should not be Utils.setAccessible .This can cause recursion.
			unsafe = (sun.misc.Unsafe) f.get(null);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
		if (unsafe == null)
			throw new NullPointerException();
		return unsafe;
	}

	private static boolean isnewjdk = false;
	private static boolean isnewclassloader = false;
	private static boolean init = false;
	private static File agent_dir_file = new File("agent_mod");

	@SuppressWarnings({"unused"})
	public static boolean isNewJDK() {
		return isnewjdk;
	}

	@SuppressWarnings({"unused", "WeakerAccess"})
	public static void init() {
		if (!init) {
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
				Log.i("Utils", "ClassLoader is New.");
			else
				Log.i("Utils", "ClassLoader is Legacy.");
			Log.i("Utils", "Init End.");
			init = true;
		}
	}

	//In new jdk. if you not add "--add-opens java.base/jdk.internal.loader=ALL-UNNAMED"
	//Then you need to be able to set accessible.
	@SuppressWarnings({"unused", "WeakerAccess"})
	public static void setAccessible(AccessibleObject ao, boolean flag) {
		try {
			ao.setAccessible(flag);
		} catch (/*java.lang.reflect.InaccessibleObjectException*/RuntimeException e) {
			try {
				Field field = AccessibleObject.class.getDeclaredField("override");
				getUnsafe().putBoolean(ao, getUnsafe().objectFieldOffset(field), flag);
			} catch (NoSuchFieldException | SecurityException ei) {
				setAccessibleDependentOnUnsafe(ao, flag);
				ei.printStackTrace();
			}
		}
	}

	private static void setAccessibleDependentOnUnsafe(AccessibleObject ao, boolean flag) {
		getUnsafe().putBoolean(ao, is64Bit() ? 12 : 8, flag);
		Log.w("Utils setAccessibleDependentOnUnsafe", ao.toString());
	}

	@SuppressWarnings({"unused", "WeakerAccess"})
	public static boolean is64Bit() {
		return System.getProperty("os.arch").contains("64");
	}

	@SuppressWarnings({"unused"})
	public static URL[] getClassLoaderURLs() {
		if (isnewclassloader) {
			return Latest.getClassLoaderURLsLatest();
		} else {
			return Legacy.getClassLoaderURLsLegacy();
		}
	}

	@SuppressWarnings({"unused"})
	public static void addClassLoaderURLs(URL url) {
		if (isnewclassloader) {
			Latest.addClassLoaderURLsLatest(url);
		} else {
			Legacy.addClassLoaderURLsLegacy(url);
		}
	}

	@SuppressWarnings({"unused", "WeakerAccess"})
	// Equivalent to Objects.requireNonNullElse
	public static <T> T requireNonNullElse(T source, T elze) {
		return source == null ? Objects.requireNonNull(elze) : source;
	}

	@SuppressWarnings({"unused", "WeakerAccess"})
	public static int getJavaVersion() {
		return ((Double) (Double.parseDouble(System.getProperty("java.class.version")))).intValue();
	}

	@SuppressWarnings({"unused", "WeakerAccess"})
	public static void setAgent_dir_file(File file) {
		if (file == null)
			throw new NullPointerException();
		if (!file.isDirectory())
			throw new RuntimeException(file + " Not Directory");
		agent_dir_file = file;
	}

	@SuppressWarnings({"unused", "WeakerAccess"})
	public static File getAgent_dir_file() {
		return agent_dir_file;
	}

	private static ClassLoader cl = ClassLoader.getSystemClassLoader();
	//Old-style processing method.
	private static final class Legacy {
		private static URL[] getClassLoaderURLsLegacy() {
			final URLClassLoader ucl = (URLClassLoader) cl;
			return ucl.getURLs();
		}

		private static void addClassLoaderURLsLegacy(URL url) {
			try {
				final URLClassLoader ucl = (URLClassLoader) cl;
				Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
				setAccessible(method, true);
				method.invoke(ucl, url);
			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static final class Latest {
		private static Object latestClassPath;
		private static Method latestGetURLsMethod;
		private static Method latestAddURLsMethod;
		private static void initLatest() {
			try {
				//jdk.internal.loader.ClassLoaders.AppClassLoader
				Class<?> classLoader = cl.getClass();
				//jdk.internal.loader.ClassLoaders.AppClassLoader.ucp type:jdk.internal.loader.URLClassPath
				Field field = classLoader.getDeclaredFields()[0];
				setAccessible(field, true);//relieve internal and private
				//type:jdk.internal.loader.URLClassPath
				latestClassPath = field.get(cl);
				latestGetURLsMethod = latestClassPath.getClass().getMethod("getURLs");
				latestAddURLsMethod = latestClassPath.getClass().getMethod("addURL", URL.class);
				setAccessible(latestGetURLsMethod, true);
				setAccessible(latestAddURLsMethod, true);
			} catch (IllegalAccessException | NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
		}

		private static void addClassLoaderURLsLatest(URL url) {
			if (latestClassPath == null)
				initLatest();
			try {
				latestAddURLsMethod.invoke(latestClassPath, url);
			} catch (IllegalAccessException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}

		private static URL[] getClassLoaderURLsLatest() {
			if (latestClassPath == null)
				initLatest();
			try {
				return (URL[]) (latestGetURLsMethod.invoke(latestClassPath));
			} catch (IllegalAccessException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
	}
	public static final class TempJar{
		public static final TempJar INSTANCE=new TempJar();
		Map<String,byte[]> map=Collections.synchronizedMap(new HashMap<>());
		private TempJar(){}
		public void addFile(String path,InputStream data)throws IOException{
			map.put(path,data.readAllBytes());
		}
		public void addFile(String path,byte[]data){
			map.put(path,data.clone());
		}

		public File genFile() throws IOException {
			File f=File.createTempFile("mod",".jar");
			OutputStream cos=new CheckedOutputStream(new FileOutputStream(f), new CRC32());
			ZipOutputStream zos=new ZipOutputStream(cos);
			for (String key:map.keySet()){
				ZipEntry entry = new ZipEntry(key);
				zos.putNextEntry(entry);
				zos.write(map.get(key));
				zos.closeEntry();
			}
			zos.close();
			cos.close();
			map.clear();
			return f;
		}
	}
}
