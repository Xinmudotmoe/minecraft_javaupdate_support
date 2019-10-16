package moe.xinmu.minecraft.patcher.patch1122;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class EnumMap<E extends AdoptEnum> extends ConcurrentHashMap<String,E> {
	public EnumMap(E[] es){
		super();
		for(E e:es)
			if(Objects.nonNull(e))
				this.put(e.name,e);
	}
}
