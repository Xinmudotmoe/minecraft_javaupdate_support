# minecraft_javaupdate_support

[English](README.md)

这是一个Minecraft模块，用于在新的Java版本中运行旧版本的MinecraftForge和Mods。 其灵感来自于 [AmadeusSystems/Alchemy](https://github.com/AmadeusSystems/Alchemy)。

该程序在很大程度上依赖于 [Unsafe](https://github.com/unofficial-openjdk/openjdk/blob/jdk/jdk/src/jdk.unsupported/share/classes/sun/misc/Unsafe.java) 库。值得一提的是，OpenJDK内部邮件已经表明它将删除Unsafe，那么我可能在未来需要做出大的改动。（谁知道。许多高效的库均依赖Unsafe。这些库的作者显然比我忙得多。）

## 如何安装 Agent

1. 下载编译的二进制文件。
2. 复制到.minecraft文件夹。
3. 在启动MinecraftForge的命令行中添加 "-javaagent:agent.jar"命令。
4. 下载代理程序模组(agent mod)并将它们放在.minecraft文件夹中.
5. 运行你的Minecraft。

## 警告

1. 该程序将大大降低Java的安全性，请不要在高权限下使用该程序。
2. 您应仔细检查所有文件，以验证所有文件是否可能包含病毒等。
3. 请确保您是通过https协议下载的所有文件，并确保源是值得信赖的。
4. 仔细配置agent_mod目录以确保下面的所有源代码或文件均正确且安全。

## 免责声明

我（作者）对因使用本程序而导致的任何损害不承担任何责任。 但我会尝试确保仓库内代码的安全性。

## 关于许可证

这个项目直接依赖于 [InMemoryJavaCompiler](https://github.com/trung/InMemoryJavaCompiler), 所以使用 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) 作为目标协议.

## 如何构建项目

您可以使用配置gradle进行编译。
或导入到任何Ide亦或是使用Javac命令直接编译。

agent本体不依赖于JVM以外的库。

代理程序模组（在.minecraft/agent_mod文件夹中）是运行时动态编译的。

## 支持的版本

### OS Support（如果除了Java版本差异之外还存在严重的OS差异）

| Version          | Supported          |
| ---------------- | ------------------ |
| Windows 10       | :white_check_mark: |
| Windows 8        | 计划支持            |
| Windows 7        | 计划支持            |
| <= Windows Vista | :x:                |
| Linux            | 计划支持（高优先度） |
| MacOS            | 计划支持            |

### Minecraft Support

| Version  | Supported               |
| -------- | ----------------------- |
| >= 1.13  | 也许已被支持             |
| 1.12.2   | :white_check_mark:      |
| 1.10.2   | 计划支持                 |
| 1.7.10   | 计划支持                 |
| else     | :x:                     |

### Java Version

| Version  | Supported                    |
| -------- | ---------------------------- |
| 1.13     | 计划支持（低优先度）           |
| 1.12     | :w:                          |
| 1.11     | :white_check_mark:           |
| <= 1.10  | :x:                          |

### JVM

| Name     | Supported                     |
| -------- | ----------------------------- |
| OpenJDK  | :white_check_mark:            |
| OpenJ9   | 计划支持                       |
| ZingVM   | 计划支持 （没有环境）           |

### Mod Support

计划中不支持所有反作弊相关模组。

其他模组会尽力支持他们，但我不能保证他们的稳定性。 如果遇到稳定性和差异，请发布issus。

### Cancel support

如果您不希望我以某种方式修改您的模组，请发布issus。 我将删除相应的代码。

如果您不想在新环境中运行mod，可以将以下代码放在mod中并确保它将运行（如调用）。

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
