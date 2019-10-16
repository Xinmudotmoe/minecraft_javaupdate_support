package moe.xinmu.minecraft.patcher.patch1122;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Objects;

public abstract class AdoptEnum<E extends AdoptEnum<E>> implements Comparable<E>{
	protected final String name;
	protected final int ordinal;

	protected AdoptEnum(String name, int ordinal) {
		this.name = Objects.requireNonNull(name);
		this.ordinal = ordinal;
	}
	public final String name() {
		return name;
	}
	public final int ordinal() {
		return ordinal;
	}

	@Override
	public String toString() {
		return name;
	}
	@Override
	public final boolean equals(Object other) {
		return this == other;
	}
	@Override
	public final int hashCode() {
		return ordinal + (name == null ? 0 : name.hashCode());
	}
	@Override
	protected final Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	public final int compareTo(E o) {
		return ordinal - o.ordinal;
	}

	@SuppressWarnings("unchecked")
	public static <T extends AdoptEnum<T>> T valueOf(Class<T> enumType, String name) {
		throw new RuntimeException("TODO");
		
	}
}
