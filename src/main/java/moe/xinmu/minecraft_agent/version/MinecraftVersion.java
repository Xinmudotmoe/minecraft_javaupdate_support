package moe.xinmu.minecraft_agent.version;

public enum MinecraftVersion {
	V1_12_2,
	Unknow;
	public static MinecraftVersion MINECRAFT_VERSION = Unknow;
	public static boolean chechVersion(MinecraftVersion a) {
		return a == MINECRAFT_VERSION;
	}
}
