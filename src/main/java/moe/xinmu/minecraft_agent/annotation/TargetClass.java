package moe.xinmu.minecraft_agent.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/** TargetClass can be used to identify the class of ClassFileTransformer.
 * When there is a value, it will be added to the Transformer class by SMain, and this object will be called only when the class name to be converted is exactly equal to the value.
 * When no value exists, it is placed in the polling conversion list, and the priority is lower than the priority when there is value.
 * Warning:
 * Executing loadClass in ClassFileTransformer is a more dangerous operation. If you find that you can't modify the class object, you should try to implement an EnvironmentSecurityClassLoader.
 * But note that this is also a dangerous behavior. In fact, you don't need the class itself when modifying the class, just get the class bytecode.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)

public @interface TargetClass {
    String[] value() default {};
}
