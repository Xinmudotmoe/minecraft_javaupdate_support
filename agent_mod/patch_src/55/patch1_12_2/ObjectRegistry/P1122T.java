package moe.xinmu.minecraft.patcher.patch1122;

import moe.xinmu.minecraft_agent.DeobfUtils;
import moe.xinmu.minecraft_agent.Utils;
import moe.xinmu.minecraft_agent.annotation.TargetClass;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.*;

@TargetClass(value = P1122T.target, register = false)
public class P1122T implements ClassFileTransformer {
	public static final String target = "net.minecraftforge.registries.ObjectHolderRegistry";

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
		ClassReader cr = new ClassReader(classfileBuffer);
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		cr.accept(new ClassVisitor(Opcodes.ASM5, cw) {
			@Override
			public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
				super.visit(Utils.getJavaVersion(), access, name, signature, superName, interfaces);
			}

			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {

				final MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				if (name.equals("findObjectHolders")) {
					try {
						ClassReader cr = new ClassReader(Objects.requireNonNull(P1122T.class.getClassLoader()
								.getResourceAsStream("moe.xinmu.minecraft.patcher.patch1122.P1122T$B".replace(".", "/").concat(".class"))));
						cr.accept(new ClassVisitor(Opcodes.ASM5, null) {
							@Override
							public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
								if (name.equals("b"))
									return new MethodVisitor(Opcodes.ASM5, mv) {
										@Override
										public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
											switch (owner) {
												case "moe/xinmu/minecraft/patcher/patch1122/P1122T$B":
													owner = "net/minecraftforge/registries/ObjectHolderRegistry";
													if ("c".equals(name)) {
														name = "addHolderReference";
														desc = "(Lnet/minecraftforge/registries/ObjectHolderRef;)V";
													}
													break;
												case "moe/xinmu/minecraft/patcher/patch1122/P1122T$B$a":
													owner = "net/minecraftforge/fml/common/discovery/ASMDataTable";
													name = "getAll";
													break;
												case "moe/xinmu/minecraft/patcher/patch1122/P1122T$B$b":
													owner = "net/minecraftforge/fml/common/discovery" +
															"/ASMDataTable$ASMData";
													switch (name) {
														case "a":
															name = "getAnnotationInfo";
															break;
														case "b":
															name = "getClassName";
															break;
													}
													break;
												case "moe/xinmu/minecraft/patcher/patch1122/P1122T$B$c":
													owner = "net/minecraftforge/fml/common/registry/GameRegistry$ObjectHolder";
													break;
												case "moe/xinmu/minecraft/patcher/patch1122/P1122T$B$e":
													owner = "net/minecraftforge/registries/ObjectHolderRef";
													desc = "(Ljava/lang/reflect/Field;Lnet/minecraft/util/ResourceLocation;Z)V";
													//net/minecraft/util/ResourceLocation
													break;
												case "moe/xinmu/minecraft/patcher/patch1122/P1122T$B$f":
													owner = "net/minecraft/util/ResourceLocation";
													break;

											}
											super.visitMethodInsn(opcode, owner, name, desc, itf);
										}

										@Override
										public void visitTypeInsn(int opcode, String type) {
											switch (type) {
												case "moe/xinmu/minecraft/patcher/patch1122/P1122T$B":
													type = "net/minecraftforge/registries/ObjectHolderRegistry";
													break;
												case "moe/xinmu/minecraft/patcher/patch1122/P1122T$B$a":
													type = "net/minecraftforge/fml/common/discovery/ASMDataTable";
													break;
												case "moe/xinmu/minecraft/patcher/patch1122/P1122T$B$b":
													type = "net/minecraftforge/fml/common/discovery" +
															"/ASMDataTable$ASMData";
													break;
												case "moe/xinmu/minecraft/patcher/patch1122/P1122T$B$e":
													type = "net/minecraftforge/registries/ObjectHolderRef";
													break;
												case "moe/xinmu/minecraft/patcher/patch1122/P1122T$B$c":
													type = "net/minecraftforge/fml/common/registry/GameRegistry$ObjectHolder";
													break;
												case "moe/xinmu/minecraft/patcher/patch1122/P1122T$B$f":
													type = "net/minecraft/util/ResourceLocation";
													break;
											}
											super.visitTypeInsn(opcode, type);
										}

										@Override
										public void visitLdcInsn(Object cst) {
											if (cst instanceof Type) {
												String classname = ((Type) cst).getClassName().replace(".", "/");
												switch (classname) {
													case "moe/xinmu/minecraft/patcher/patch1122/P1122T$B":
														classname = "net/minecraftforge/registries/ObjectHolderRegistry";
														break;
													case "moe/xinmu/minecraft/patcher/patch1122/P1122T$B$a":
														classname = "net/minecraftforge/fml/common/discovery/ASMDataTable";
														break;
													case "moe/xinmu/minecraft/patcher/patch1122/P1122T$B$b":
														classname = "net/minecraftforge/fml/common/discovery" +
																"/ASMDataTable$ASMData";
														break;
													case "moe/xinmu/minecraft/patcher/patch1122/P1122T$B$c":
														classname = "net/minecraftforge/fml/common/registry/GameRegistry$ObjectHolder";
														break;
													case "moe/xinmu/minecraft/patcher/patch1122/P1122T$B$d":
														classname = "net/minecraftforge/fml/common/Mod";
														break;
													case "moe/xinmu/minecraft/patcher/patch1122/P1122T$B$e":
														classname = "net/minecraftforge/registries/ObjectHolderRef";
														break;
													case "moe/xinmu/minecraft/patcher/patch1122/P1122T$B$f":
														classname = "net/minecraft/util/ResourceLocation";
														break;
												}
												cst = Type.getType("L" + classname + ";");
											}
											super.visitLdcInsn(cst);
										}

										@Override
										public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String desc, boolean visible) {
											return null;
										}

										@Override
										public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
											//return;
										}

										@Override
										public void visitLineNumber(int line, Label start) {
											//return;
										}
									};
								return null;
							}
						}, ClassReader.EXPAND_FRAMES);
					} catch (IOException e) {
						e.printStackTrace();
					}
					return null;
				}
				if (name.equals("scanClassForFields"))
					return new MethodVisitor(Opcodes.ASM5, new GeneratorAdapter(mv, access, name, desc)) {
						@Override
						public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
							super.visitMethodInsn(opcode, owner, name, desc, itf);
							if (name.equals("isFinal")) {
								GeneratorAdapter ga = (GeneratorAdapter) this.mv;
								ga.pop();
								ga.push(true);
							}
						}
					};
				return mv;
			}
		}, ClassReader.EXPAND_FRAMES);
		return cw.toByteArray();
	}

	static class B {
		static class a {
			Set<b> a(String s) {
				return null;
			}
		}

		static class b {
			Map<String, String> a() {
				return null;
			}

			String b() {
				return null;
			}
		}

		@interface c {
			String value();
		}

		static class d {
		}

		static class e {
			public e(Field a, f b, boolean c) {
			}
		}

		static class f {
			public f(String a) {
			}

			public f(String a, String b) {
			}
		}

		public void c(e a) {
		}

		public void b(a table) {

			Set<b> allObjectHolders = table.a(c.class.getName());
			Map<String, String> classModIds = new HashMap<>();
			Iterator iterator = table.a(d.class.getName()).iterator();
			ClassLoader cl = this.getClass().getClassLoader();

			while (iterator.hasNext()) {
				b data = (b) iterator.next();
				String className = (String) data.a().get("modid");
				classModIds.put(data.b(), className);
			}

			iterator = allObjectHolders.iterator();
			while (iterator.hasNext()) {
				try {
					Class.forName(((b) iterator.next()).b(), true, cl);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
			iterator = P1122ObjectRegistry.INSTANCE.modifys.iterator();
			while (iterator.hasNext()) {
				String name = (String) iterator.next();
				Class c = null;
				try {
					c = Class.forName(name.replace("/", "."), true, cl);
				} catch (ClassNotFoundException e) {
					//e.printStackTrace();
					continue;
				}
				Iterator iterator2 = Arrays.asList(c.getDeclaredFields()).iterator();
				while (iterator2.hasNext()) {
					Field field = (Field) iterator2.next();
					int mod = field.getModifiers();
					if (Modifier.isPublic(mod) && Modifier.isStatic(mod))
						this.c(new e(field, new f("minecraft"), true));
				}
			}
			Map map = moe.xinmu.minecraft.patcher.patch1122.P1122ObjectRegistry.INSTANCE.maper;
			iterator = map.keySet().iterator();

			while (iterator.hasNext()) {
				String name = (String) iterator.next();
				Class clazz = null;
				try {
					clazz = Class.forName(name, true, cl);
				} catch (ClassNotFoundException e) {
					String name1 = DeobfUtils.checkClassReDeobfString(name);
					if (!name.equals(name1)) {
						try {
							clazz = Class.forName(name1, true, cl);
						} catch (ClassNotFoundException e1) {
							e1.printStackTrace();
							continue;
						}
					} else {
						e.printStackTrace();
						continue;
					}
				}
				Set s = (Set) map.get(name);
				Iterator iterator2 = s.iterator();
				while (iterator2.hasNext()) {
					String fieldname = (String) iterator2.next();
					try {
						Field field = clazz.getDeclaredField(fieldname);
						String modid = Objects.requireNonNullElse(classModIds.get(name), "minecraft");
						c cc = (c) clazz.getAnnotation(c.class);
						if (cc != null)
							modid = cc.value();
						String se = null;
						cc = field.getAnnotation(c.class);
						if (cc != null)
							se = cc.value();
						if (Objects.nonNull(se) && se.contains(":")) {
							modid = se.split(":")[0];
							se = se.split(":")[1];
						}
						this.c(new e(field, new f(modid, se), false));
					} catch (NoSuchFieldException e) {
						e.printStackTrace();
					}
				}
			}
			P1122ObjectRegistry.INSTANCE.stop();
		}
	}
}
