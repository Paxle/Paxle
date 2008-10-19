/*
This file is part of the Paxle project.
Visit http://www.paxle.net for more information.
Copyright 2007-2008 the original author or authors.

Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
or in the file LICENSE.txt in the root directory of the Paxle distribution.

Unless required by applicable law or agreed to in writing, this software is distributed
on an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*/

package org.paxle.desktop.impl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.io.IOTools;

public class HelperClassLoader extends URLClassLoader {
	private ClassLoader bundleClassloader = null;
	private Hashtable<String,Class<?>> classCache = new Hashtable<String,Class<?>>();
	
	private Log logger = LogFactory.getLog(this.getClass());
	
	public HelperClassLoader(URL[] urls, ClassLoader bundleClassloader) {
		super(urls, null);
		this.bundleClassloader = bundleClassloader;
	}

	@Override
	public Class<?> loadClass(String className) throws ClassNotFoundException {
		Class<?> cls = null;
		byte[] classByte = null;
		
		if ((className.startsWith("org.jdesktop.jdic") && !className.equals("org.jdesktop.jdic.init.JdicManager")) ||
				className.startsWith("org.paxle.desktop.backend.impl")) {
			
			cls = this.classCache.get(className);
			if (cls != null)
				return cls;
			
			try {
				classByte = loadClassData(this,className);
			} catch (ClassNotFoundException e) {
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
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			final String classFile = className.replace('.', '/' /*File.separatorChar*/) + ".class";
			this.logger.debug("Load: " + classFile + " using " + ((cl == this) ? "this" : "bundle") + " class-loader");
			final InputStream in = cl.getResourceAsStream(classFile);
			if (in == null)
				throw new ClassNotFoundException(classFile);
			IOTools.copy(in, baos);
		} catch(Exception e) {
			throw new ClassNotFoundException(className);
		}
		return baos.toByteArray();
	} 	
}
