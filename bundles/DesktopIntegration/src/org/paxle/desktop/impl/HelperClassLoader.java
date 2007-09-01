package org.paxle.desktop.impl;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Hashtable;

public class HelperClassLoader extends URLClassLoader {
	private ClassLoader bundleClassloader = null;
	private Hashtable<String,Class> classCache = new Hashtable<String,Class>();	
	
	public HelperClassLoader(URL[] urls, ClassLoader bundleClassloader) {
		super(urls, null);
		this.bundleClassloader = bundleClassloader;
	}

	@Override
	public Class loadClass(String className) throws ClassNotFoundException {
		Class cls = null;
		byte[] classByte = null;
		if (className.startsWith("org.jdesktop.jdic") || className.equals("org.paxle.desktop.impl.DesktopInit")) {	
			cls = (Class)this.classCache.get(className);
			if(cls != null) return cls;
			
			classByte = loadClassData(this,className);
			if (classByte == null) {
				classByte = loadClassData(bundleClassloader, className);
			}

			cls = defineClass(null, classByte, 0, classByte.length);
			resolveClass(cls);
			this.classCache.put(className,cls);
		} else {
			cls = this.bundleClassloader.loadClass(className);
		}
		return cls;
	}

	private byte[] loadClassData(ClassLoader cl, String className) throws ClassNotFoundException {
		byte[] data = new byte[0];
		try {
			InputStream in = cl.getResourceAsStream(className.replace('.','/') + ".class");
			if (in == null) return null;
			
			byte[] buf = new byte[100];
			int i;
			while((i = in.read(buf)) != -1) {
				byte[] tmp = new byte[data.length + i];
				System.arraycopy(data,0,tmp,0,data.length);
				System.arraycopy(buf,0,tmp,data.length,i);
				data = tmp;
			}
		} catch(Exception e) {
			throw new ClassNotFoundException(className);
		}
		return data;
	} 	
}
