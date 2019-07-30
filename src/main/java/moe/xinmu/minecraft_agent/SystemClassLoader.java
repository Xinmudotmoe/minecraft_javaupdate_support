package moe.xinmu.minecraft_agent;

import java.net.URL;
import java.net.URLClassLoader;

/*
* Not Found In JDK8
import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
*/

//Not supported yet

@Deprecated
//TODO
public final class SystemClassLoader extends URLClassLoader {
	public SystemClassLoader(ClassLoader parent) {
		this(new URL[0], parent);
	}

	public SystemClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
/*        try {
            VirtualMachine vm=VirtualMachine.attach(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
            vm.loadAgent(this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile());
            vm.detach();
        } catch (AttachNotSupportedException | IOException | AgentLoadException | AgentInitializationException e) {
            e.printStackTrace();
        }*/
	}
}
