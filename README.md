# minecraft_javaupdate_support
[中文](README-zh_cn.md)

This is a minecraft module dedicated to running the old versions of MinecraftForge and Mods in new Java. Part of the inspiration comes from [AmadeusSystems/Alchemy](https://github.com/AmadeusSystems/Alchemy).

This program relies heavily on the [Unsafe](https://github.com/unofficial-openjdk/openjdk/blob/jdk/jdk/src/jdk.unsupported/share/classes/sun/misc/Unsafe.java) package. It is worth mentioning that OpenJDK internal mail has indicated that it will remove Unsafe, then I may need to make a major change. (Who knows. A lot of efficient libraries rely on Unsafe. The authors of those libraries are obviously much more busy than me.)
## How To Install the agent.
1. Download the compiled binary.
2. Copy to the .minecraft folder
3. Added "-javaagent:agent.jar" the command to start MinecraftForge
4. Download the agent mods and place them in the .minecraft folder.
5. Run your Minecraft.
## Warning
1. This program will greatly reduce the security of Java, please do not use this program under high authority anyway.
2. You should review all the files carefully to verify that all files are likely to contain viruses and so on. 
3. Make sure to download all files via the https protocol and make sure the source is trustworthy.
4. Carefully configure the agent_mod directory to ensure that all source code or files below are ok.
## Disclaimer
I (the author) assumes no responsibility for any damages resulting from the use of this program. But I will try to ensure the security inside the repository.
## About License
This project relies directly on [InMemoryJavaCompiler](https://github.com/trung/InMemoryJavaCompiler), so use the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) open source protocol.
## How To Build this.
You can compile with the configuration gradle.
Or import to any Ide or directly compile with Javac commands.

The agent ontology does not depend on libraries other than the JVM.

The agent mods(In the .minecraft/agent_mod folder) is dynamically compiled for runtime.
## Supported Versions
### Operating System Support (If there are serious operating system differences in addition to Java differences)
| Version          | Supported                    |
| ---------------- | ---------------------------- |
| Windows 10       | :white_check_mark:           |
| Windows 8        | Plan Support                 |
| Windows 7        | Plan Support                 |
| <= Windows Vista | :x:                          |
| Linux            | Plan Support (high priority) |
| MacOS            | Plan Support                 |

### Minecraft Support
| Version  | Supported               |
| -------- | ----------------------- |
| >= 1.13  | Maybe already supported |
| 1.12.2   | :white_check_mark:      |
| 1.10.2   | Plan Support            |
| 1.7.10   | Plan Support            |
| else     | :x:                     |

### Java Version
| Version  | Supported                    |
| -------- | ---------------------------- |
| 1.13     | Plan Support (low priority)  |
| 1.12     | :w:                          |
| 1.11     | :white_check_mark:           |
| <= 1.10  | :x:                          |

### JVM 
| Name     | Supported                     |
| -------- | ----------------------------- |
| OpenJDK  | :white_check_mark:            |
| OpenJ9   | Plan Support                  |
| ZingVM   | Plan Support (No environment) |

### Mod Support
All anti-cheating mods are not supported in the plan.

Other mods will try their best to support them, but I can't guarantee their stability.  If you encounter stability and differences, please post the issue.

### Cancel support
If you don't want me to modify your mod in some way, please submit a issue. I will delete the corresponding code.

If you don't want to run your mod in a new environment, you can place the following code in your mod and make sure it will be run (like calling).

```java
//If jvm supports the highest major version other than 52 (java8), throw an error.
//Problems can arise when you continue to use the original warehouse to develop mods for new versions of the game.
void checkPreventPlan1(){
    if(((Double)Double.parseDouble(System.getProperty("java.class.version"))).intValue()!=52)
        throw new Error();
}
//Try to find the Utils class for this project, and if it finds it, throw an error. 
//This method is recommended.
void checkPreventPlan2(){
    try{
        Class.forName("moe.xinmu.minecraft_agent.Utils");
        throw new Error();
    } catch (ClassNotFoundException e){

    }
}
```