
package org.paxle.core.io.temp.impl;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.io.temp.ITempDir;

public class FSTempDir extends ATempDir implements ITempDir {
	
	private final File directory;
	private final Log logger = LogFactory.getLog(FSTempDir.class);
	
	public FSTempDir(File dir, String prefix) throws IOException {
		super(prefix);
		this.directory = dir;
		if (!dir.exists())
			if (!dir.mkdirs())
				throw new IOException("Couldn't create directory for temporay files: " + dir);
	}
	
	public FSTempDir(File dir) throws IOException {
		this(dir, null);
	}
	
	public File createTempFile(String prefix, String suffix) throws IOException {
		File r;
		String name;
		int i=0;
		do {
			switch (i++) {
				case 10: logger.warn("No free temp-file-name found up to now"); break;
				case 100: throw new IOException("No free temp-file-name found in 100 tries");
				default: break;
			}
			name = generateNewName(prefix, suffix);
			r = new File(this.directory, name);
		} while (r.exists());
		if (!r.createNewFile())
			throw new IOException("file '" + name + "' in directory '" + this.directory + "' already exists, this is an internal error and must be fixed");
		return r;
	}
}
