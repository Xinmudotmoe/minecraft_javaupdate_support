package org.mdkt.compiler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
			try {
				String classname=name.substring(0,name.length()-".class".length()).replace("/",".");
				CompiledCode cc=Objects.requireNonNull(customCompiledCode.get(classname));
				String spec=InMemoryJavaCompilerBase64StreamHandler.protocol+new String(Base64.getUrlEncoder().encode(cc.getByteCode()));
				return new URL(null,spec,InMemoryJavaCompilerBase64StreamHandler.INSTANCE);
			} catch (MalformedURLException | NullPointerException ignored) {
			}
		}
		return super.findResource(name);
	}

	public String[] getCompiledClazzName(){
		return customCompiledCode.keySet().toArray(new String[0]);
	}

	private static class InMemoryJavaCompilerBase64StreamHandler extends URLStreamHandler {

		private static final InMemoryJavaCompilerBase64StreamHandler INSTANCE=new InMemoryJavaCompilerBase64StreamHandler();

		static final String protocol="in-memory-java-compiler-base64://";

		@Override
		protected URLConnection openConnection(URL u) {
			return new InMemoryJavaCompilerBase64Connection(u);
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
				String s=url.toExternalForm();
				if(!s.startsWith(protocol))
					throw new ProtocolException();
				return new ByteArrayInputStream(Base64.getUrlDecoder().decode(s.substring(protocol.length())));
			}
		}
	}
}
