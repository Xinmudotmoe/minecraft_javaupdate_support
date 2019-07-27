package org.mdkt.compiler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

public class DynamicClassLoader extends ClassLoader {

	private Map<String, CompiledCode> customCompiledCode = new HashMap<>();

	public DynamicClassLoader(ClassLoader parent) {
		super(parent);
	}

	public void addCode(CompiledCode cc) {
		customCompiledCode.put(cc.getName(), cc);
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		CompiledCode cc = customCompiledCode.get(Objects.requireNonNull(name));
		if (cc == null)
			throw new ClassNotFoundException();
		byte[] byteCode = cc.getByteCode();
		return defineClass(name, byteCode, 0, byteCode.length);
	}

	@Override
	protected URL findResource(String name) {
		if(Pattern.matches(".*\\.class$",name)){
			String classname=name.substring(0,name.length()-".class".length()).replace("/",".");
			CompiledCode cc=Objects.requireNonNull(customCompiledCode.get(classname));
			return InMemoryJavaCompilerBase64StreamHandler.genURL(name,cc.getByteCode());
		}
		return super.findResource(name);
	}

	public long getAllSize(){
		final AtomicLong size=new AtomicLong(0);
		customCompiledCode.values().forEach(c->size.set(size.get()+c.getSize()));
		return size.get();
	}

	public String[] getCompiledClazzName(){
		return customCompiledCode.keySet().toArray(new String[0]);
	}

	public static class InMemoryJavaCompilerBase64StreamHandler extends URLStreamHandler {

		private static final InMemoryJavaCompilerBase64StreamHandler INSTANCE=new InMemoryJavaCompilerBase64StreamHandler();

		static final String protocol="in-memory-java-compiler-base64";

		@Override
		protected URLConnection openConnection(URL u) {
			return new InMemoryJavaCompilerBase64Connection(u);
		}
		static URL genURL(String name,byte[] data){
			try {
				return new URL(InMemoryJavaCompilerBase64StreamHandler.protocol+"-"+name.hashCode(),
						new String(Base64.getUrlEncoder().encode(data)),
						-1,"/",InMemoryJavaCompilerBase64StreamHandler.INSTANCE);
			} catch (MalformedURLException e) {
				return null;
			}
		}
		private static class InMemoryJavaCompilerBase64Connection extends URLConnection{

			InMemoryJavaCompilerBase64Connection(URL url) {
				super(url);
			}

			@Override
			public void connect() {

			}

			@Override
			public InputStream getInputStream() throws IOException{
				checkName();
				return new ByteArrayInputStream(Base64.getUrlDecoder().decode(getURL().getHost()));
			}

			private void checkName()throws IOException {
				String file=getURL().getFile();
				if(file.length()==1)
					return;
				if(Integer.parseInt(getURL().getProtocol().replace(protocol+"-","")) != URLDecoder.decode(file.substring(1), "UTF-8").hashCode())
					throw new ConnectException("Not Found.");
			}
		}
	}
}
