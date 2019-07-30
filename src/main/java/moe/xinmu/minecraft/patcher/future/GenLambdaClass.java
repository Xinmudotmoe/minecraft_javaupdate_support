package moe.xinmu.minecraft.patcher.future;

import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;

public class GenLambdaClass {
	public GenLambdaClass() {
		this(GenLambdaClass.class);
	}

	public GenLambdaClass(Class<?> mirror) {
		lookup = MethodHandles.lookup().in(mirror);
	}

	public MethodHandles.Lookup lookup;

	public <T> T genLambda(Class<T> tClass, Method tmethod) {
		if (!Modifier.isStatic(tmethod.getModifiers()))
			throw new NullPointerException();
		return genLambda(tClass, tmethod, null);
	}

	public <T> T genLambda(Class<T> tClass, Method tmethod, Object o) {
		try {
			Method method = null;
			for (Method m : tClass.getDeclaredMethods())
				if (Modifier.isAbstract(m.getModifiers()))
					method = m;
			Objects.requireNonNull(method);
			Class<?> clazz = tmethod.getDeclaringClass();
			boolean isStatic = Modifier.isStatic(tmethod.getModifiers());
			MethodType invokedType;
			if (isStatic)
				invokedType = MethodType.methodType(tClass);
			else
				invokedType = MethodType.methodType(tClass, clazz);
			MethodType methodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
			MethodType methodType1 = MethodType.methodType(tmethod.getReturnType(), tmethod.getParameterTypes());
			CallSite cs = LambdaMetafactory.metafactory(lookup,
					method.getName(),
					invokedType,
					methodType,
					lookup.findStatic(clazz, tmethod.getName(), methodType1),
					methodType1);
			MethodHandle mh = cs.dynamicInvoker();
			Object out;
			if (isStatic)
				out = mh.invoke();
			else
				out = mh.invoke(o);
			return tClass.cast(out);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
}
