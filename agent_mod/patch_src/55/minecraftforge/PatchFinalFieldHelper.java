package moe.xinmu.minecraft.patcher;
import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Arrays;

import org.apache.bcel.Const;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import moe.xinmu.minecraft_agent.*;
import moe.xinmu.minecraft_agent.annotation.*;
import javassist.*;

@TargetClass("net.minecraftforge.registries.ObjectHolderRef$FinalFieldHelper")
public class PatchFinalFieldHelper implements ClassFileTransformer{
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        try{
            JavaClass jc=new ClassParser(new ByteArrayInputStream(classfileBuffer),className).parse();
            ClassGen cg=new ClassGen(jc);
            ConstantPoolGen cpg=cg.getConstantPool();
//          int outter=cpg.addFieldref("java/lang/System","out","Ljava/io/PrintStream;");
//          int outrun=cpg.addMethodref("java/io/PrintStream","println","(Ljava/lang/Object;)V");
            for (Field f:cg.getFields()) 
                cg.removeField(f);
            Method m=cg.getMethodAt(1);
            Code c=m.getCode();
            InstructionList il=new InstructionList();
//            il.append(new GETSTATIC(outter));
//            il.append(new ALOAD(0));
//            il.append(new INVOKEVIRTUAL(outrun));
            il.append(new ALOAD(0));
            il.append(new ARETURN());
            c.setMaxStack(2);
            c.setAttributes(new Attribute[0]);
            c.setCode(il.getByteCode());
            ClassPool classPool = new ClassPool();
            classPool.insertClassPath(new ClassClassPath(java.lang.reflect.Field.class));
            classPool.insertClassPath(new ClassClassPath(moe.xinmu.minecraft_agent.Utils.class));
            CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(cg.getJavaClass().getBytes()));
            CtMethod cm=ctClass.getMethod("setField","(Ljava/lang/reflect/Field;Ljava/lang/Object;Ljava/lang/Object;)V");
            cm.setBody(
                        "{\n"+
                            "boolean is_static=java.lang.reflect.Modifier.isStatic($1.getModifiers());\n" +
                            "if(!is_static&&$2==null){\n" +
                            "    throw new java.lang.NullPointerException(\"Not Static.\");\n" +
                            "}"+
                            "if(!$1.getType().isInstance($3)){\n" +
                            "    throw new java.lang.ClassCastException(\"Unsupported modify. Please check type.\");\n" +
                            "}"+
                            "long offset=is_static?\n" +
                            "        moe.xinmu.minecraft_agent.Utils.getUnsafe().staticFieldOffset($1):\n" +
                            "        moe.xinmu.minecraft_agent.Utils.getUnsafe().objectFieldOffset($1);\n" +
                            "if(is_static){\n" +
                            "    $2=moe.xinmu.minecraft_agent.Utils.getUnsafe().staticFieldBase($1);\n" +
                            "}"+
                            "moe.xinmu.minecraft_agent.Utils.getUnsafe().putObjectVolatile($2,offset,$3);\n"+
                            "moe.xinmu.minecraft_agent.Utils.setAccessible($1,true);"+
                            "$1.get($2);"+
                        "}"
            );
            return ctClass.toBytecode();
        }   catch (Throwable e){
            e.printStackTrace();
        }
        return null;
    }
}
