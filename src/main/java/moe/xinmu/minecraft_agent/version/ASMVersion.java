package moe.xinmu.minecraft_agent.version;

public enum ASMVersion {
	V4_0,
	V4_1,
	V4_2,
	V5_0,
	V5_1,
	V5_2,
	V6_0,
	Unknow;
	public static ASMVersion ASM_Version = Unknow;

	public static boolean checkVersion(ASMVersion a) {
		return a == ASM_Version;
	}
}
