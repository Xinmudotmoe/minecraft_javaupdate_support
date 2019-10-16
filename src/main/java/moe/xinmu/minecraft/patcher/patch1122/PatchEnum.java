package moe.xinmu.minecraft.patcher.patch1122;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class PatchEnum implements ClassFileTransformer {
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		return new byte[0];
	}
	public byte[] transform(byte[] classfileBuffer) {
		ClassReader cr=new ClassReader(classfileBuffer);
		if(!cr.getSuperName().equals("java/lang/Enum")){
			System.err.println(cr.getSuperName()+"!=Enum");
			return classfileBuffer;
		}
		ClassWriter cw=new ClassWriter(cr,ClassWriter.COMPUTE_FRAMES);
		cw.visitField(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC|Opcodes.ACC_FINAL,"ae$VALUES",
				"Lmoe/xinmu/minecraft/patcher/patch1122/EnumMap;",null,null);
		cr.accept(new ClassVisitor(Opcodes.ASM5,cw) {
			String name;
			@Override
			public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
				this.name=name;
				super.visit(version, access, name, signature, "moe/xinmu/minecraft/patcher/patch1122/AdoptEnum", interfaces);
			}

			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				MethodVisitor mv=super.visitMethod(access, name, desc, signature, exceptions);
				switch (name){
					case "<clinit>":
						return new CLInitPatch(new GeneratorAdapter(mv,access,name,desc));
					case "values":
						return new ValuesPatch(new GeneratorAdapter(mv,access,name,desc));
					case "valueOf":
						return new ValueOfPatch(new GeneratorAdapter(mv,access,name,desc));

				}
				return mv;
			}
		},ClassReader.EXPAND_FRAMES);
		return cw.toByteArray();
	}
	static class CLInitPatch extends  MethodVisitor{
		GeneratorAdapter ga;
		public CLInitPatch(MethodVisitor mv) {
			super(Opcodes.ASM5, mv);
			ga = (GeneratorAdapter) mv;
		}

		@Override
		public void visitFieldInsn(int opcode, String owner, String name, String desc) {
			if(opcode==Opcodes.PUTSTATIC&&name.equals("$VALUES"))
			{
			}
			super.visitFieldInsn(opcode, owner, name, desc);
		}
	}
	class ValuesPatch extends MethodVisitor{

		public ValuesPatch(MethodVisitor mv) {
			super(Opcodes.ASM5, mv);
		}
	}
	class ValueOfPatch extends MethodVisitor{

		public ValueOfPatch(MethodVisitor mv) {
			super(Opcodes.ASM5, mv);
		}
	}

}
