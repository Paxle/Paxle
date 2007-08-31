
package org.paxle.core.io.temp.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;

import org.paxle.core.io.temp.ITempDir;
import org.paxle.core.io.temp.ITempFileManager;

public class TempFileManager implements ITempFileManager {
	
	private static TempFileManager tfm = null;
	
	public static void init(ITempDir defaultDir) {
		tfm = new TempFileManager(defaultDir);
	}
	
	public static void init() {
		tfm = new TempFileManager();
	}
	
	public static TempFileManager getTempFileManager() {
		if (tfm == null) {
			init();
		}
		return tfm;
	}
	
	private final Hashtable<String,ITempDir> classMap = new Hashtable<String,ITempDir>();
	private final Hashtable<File,ITempDir> fileMap = new Hashtable<File,ITempDir>();
	private final ITempDir defaultDir;
	
	public TempFileManager(ITempDir defaultDir) {
		this.defaultDir = defaultDir;
	}
	
	public TempFileManager() {
		this.defaultDir = new DefaultTempDir();
	}
	
	public void removeTempDirFor(String... classNames) {
		for (final String className : classNames)
			this.classMap.remove(className);
	}
	
	public void setTempDirFor(ITempDir dir, String... classNames) {
		for (final String className : classNames)
			this.classMap.put(className, dir);
	}
	
	public File createTempFile() throws IOException {
		final String className = new Exception().getStackTrace()[1].getClassName();
		ITempDir dir = this.classMap.get(className);
		if (dir == null)
			dir = this.defaultDir;
		final File ret = dir.createTempFile(className, "tmp");
		this.fileMap.put(ret, this.defaultDir);
		return ret;
	}
	
	public void releaseTempFile(File file) throws FileNotFoundException, IOException {
		final ITempDir dir = this.fileMap.get(file);
		if (dir == null)
			throw new FileNotFoundException("this manager doesn't handle the file '" + file + "'");
		dir.releaseTempFile(file);
	}
}
