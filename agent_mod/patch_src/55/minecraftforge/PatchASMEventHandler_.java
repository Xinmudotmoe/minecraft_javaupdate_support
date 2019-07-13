package moe.xinmu.minecraft.patcher;

import moe.xinmu.minecraft_agent.annotation.TargetClass;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashMap;


@TargetClass
public class PatchASMEventHandler_ implements ClassFileTransformer {
    static PrintStream err;
    static {
        err=System.err;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if(!loader.getClass().getName().equals("net.minecraftforge.fml.common.eventhandler.ASMEventHandler$ASMClassLoader"))
            return null;

        try {
            ClassGen cg=new ClassGen(new ClassParser(new ByteArrayInputStream(classfileBuffer),className).parse());
            final ConstantPoolGen cpg=cg.getConstantPool();
            final ConstantPool cp=cpg.getConstantPool();
            HashMap<String,Boolean>hm=new HashMap<>();
            hm.put(cg.getClassName(),cg.isInterface());
            hm.put("java/lang/Object",false);
            cp.accept(new DescendingVisitor(cg.getJavaClass(),new EmptyVisitor(){}) {
                public void visitConstantMethodref(ConstantMethodref obj){
                    String class_name=obj.getClass(cp);
                    /*temporary*/ ConstantNameAndType cnat= (ConstantNameAndType) cp.getConstant(obj.getNameAndTypeIndex());
                    String method_name=cnat.getName(cp);
                    String signature=cnat.getSignature(cp);
                    boolean isInterface=false;
                    if(!hm.containsKey(class_name))
                            try {
                                isInterface=loader.loadClass(class_name.replace("/",".")).isInterface();
                                hm.put(class_name,isInterface);
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                    else
                        isInterface=hm.get(class_name);
                    if(isInterface){
                        cpg.setConstant(cpg.lookupMethodref(class_name,method_name,signature), new ConstantInterfaceMethodref(obj.getClassIndex(),obj.getNameAndTypeIndex()));
                        System.out.println(class_name);
                    }
                }
            });
            cg.setMajor(52);
            return cg.getJavaClass().getBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return classfileBuffer;
    }
}
