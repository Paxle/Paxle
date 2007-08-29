
package org.paxle.core.io.temp.impl;

import java.io.File;
import java.io.IOException;

import org.paxle.core.io.temp.ITempDir;

public class DefaultTempDir extends ATempDir implements ITempDir {
	
	public DefaultTempDir() {
		super(null);
	}
	
	public File createTempFile(String prefix, String suffix) throws IOException {
		return File.createTempFile(prefix, suffix);
	}
}
