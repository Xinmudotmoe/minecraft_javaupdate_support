package moe.xinmu.minecraft;

import moe.xinmu.minecraft_agent.Utils;
import moe.xinmu.minecraft_agent.annotation.$Main;
import moe.xinmu.minecraft_agent.annotation.Main;
import moe.xinmu.minecraft_agent.annotation.PreMain;
import moe.xinmu.minecraft_agent.version.ASMVersion;
import moe.xinmu.minecraft_agent.version.MinecraftVersion;
import org.objectweb.asm.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

@Main
public class VersionInitialize implements $Main {
	@Override
	public void main() {
		init_minecraft_version();
		init_asm_version();
	}

	public static void init_minecraft_version() {
		URLClassLoader classLoader = new URLClassLoader(Utils.getClassLoaderURLs(), null);
		URL u = classLoader.findResource("net/minecraft/server/MinecraftServer.class");
		if (u == null)
			return;
		try {
			ClassReader cr = new ClassReader(u.openStream());
			cr.accept(new ClassVisitor(Opcodes.ASM5, null) {
				@Override
				public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
					if (Type.getReturnType(desc).equals(Type.getType(String.class)))
						return new MethodVisitor(Opcodes.ASM5) {
							Pattern p = Pattern.compile("1\\.[0-9]{1,2}(\\.[0-9]{1,2})?$");

							@Override
							public void visitLdcInsn(Object cst) {
								if (cst instanceof String) {
									String c = (String) cst;
									if (p.matcher(c).matches())
										try {
											MinecraftVersion.MINECRAFT_VERSION = MinecraftVersion.valueOf(
													"V".concat(c.replace(".", "_")));
										} catch (IllegalArgumentException ignored) {
										}
								}
							}
						};
					return null;
				}
			}, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
		} catch (Exception ignored) {
		}
	}

	public static void init_asm_version() {
		URL url = new URLClassLoader(Utils.getClassLoaderURLs(), null)
				.findResource("org/objectweb/asm/ClassWriter.class");
		if (url.getProtocol().equals("jar")) {
			try {
				Attributes a = new JarFile(new File(new URI(url.getFile().split("!/")[0])))
						.getManifest().getMainAttributes();
				ASMVersion.ASM_Version =
						ASMVersion.valueOf("V".concat(a.get(new Attributes.Name("Bundle-Version")).toString().replace(".","_")));
			} catch (Exception ignored) {
			}
		}
	}
}
