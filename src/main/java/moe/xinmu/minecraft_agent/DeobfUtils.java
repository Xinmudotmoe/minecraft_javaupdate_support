package moe.xinmu.minecraft_agent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//TODO
public class DeobfUtils {
	private static Map<String, String> m = new ConcurrentHashMap<>();
	private static Map<String, String> rem = new ConcurrentHashMap<>();
	private static final ClassLoader appclassloader = ClassLoader.getSystemClassLoader();

	static {
		init();
	}

	public static void init() {
		InputStream is = appclassloader.getResourceAsStream("deobfuscation_data-1.12.2.lzma");
		InputStream iis = null;
		try {
			Class<?> lzma = appclassloader.loadClass("LZMA.LzmaInputStream");
			iis = (InputStream) lzma.getConstructor(InputStream.class).newInstance(is);
		} catch (ReflectiveOperationException | NullPointerException e) {
			e.printStackTrace();
		}
		String ssss = "";
		if (iis == null) {
			return;
		}
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			byte[] buff = new byte[1024];
			int rc;
			while ((rc = iis.read(buff, 0, 100)) > 0) {
				os.write(buff, 0, rc);
			}
			iis.close();
			ssss = new String(os.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}

		String[][] s = Arrays.stream(ssss.split("\n"))
				.filter(sss -> sss.startsWith("CL: ")).map(sss -> sss.replace("CL: ", ""))
				.map(sss -> sss.split(" ")).filter(list -> list.length == 2).toArray(String[][]::new);
		for (String[] ss : s) {
			m.put(ss[1], ss[0]);
			rem.put(ss[0], ss[1]);
		}
	}

	public static String checkClassDeobfString(String s) {
		if (m.containsKey(s))
			return m.get(s);
		return s;
	}

	public static String checkClassReDeobfString(String s) {
		if (rem.containsKey(s))
			return rem.get(s);
		return s;
	}
}
