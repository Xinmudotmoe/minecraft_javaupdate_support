package moe.xinmu.minecraft_agent.annotation;

import moe.xinmu.minecraft_agent.AgentModClassLoader;

import java.lang.instrument.Instrumentation;

public interface $Main {
	default void main() {
	}

	default void main(@SuppressWarnings("unused") AgentModClassLoader amcl) {
		main();
	}

	default void main(@SuppressWarnings("unused") AgentModClassLoader amcl, @SuppressWarnings("unused") Instrumentation instrumentation) {
		main(amcl);
	}
}
