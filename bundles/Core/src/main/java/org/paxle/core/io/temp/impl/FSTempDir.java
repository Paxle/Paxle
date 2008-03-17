
package org.paxle.core.io.temp.impl;

import java.io.File;
import java.io.IOException;

import org.paxle.core.io.temp.ITempDir;

public class FSTempDir extends ATempDir implements ITempDir {
	
	private final File directory;
	private final boolean deleteOnExit;
	
	public FSTempDir(File dir, String prefix, boolean deleteOnExit) throws IOException {
		super(prefix);
		this.directory = dir;
		this.deleteOnExit = deleteOnExit;
		if (!dir.exists())
			if (!dir.mkdirs())
				throw new IOException("Couldn't create directory for temporay files: " + dir);
	}
	
	public FSTempDir(File dir, boolean deleteOnExit) throws IOException {
		this(dir, null, deleteOnExit);
	}
	
	public File createTempFile(String prefix, String suffix) throws IOException {
		final String name = generateNewName(prefix, suffix);
		final File r = new File(this.directory, name);
		if (!r.createNewFile())
			throw new Error("file '" + name + "' in directory '" + this.directory + "' already exists, this is an internal error and must be fixed");
		if (this.deleteOnExit)
			r.deleteOnExit();
		return r;
	}
}
