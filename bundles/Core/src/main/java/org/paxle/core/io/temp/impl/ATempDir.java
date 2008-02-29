package org.paxle.core.io.temp.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.paxle.core.io.temp.ITempDir;

public abstract class ATempDir implements ITempDir {
	
	protected static AtomicInteger num = new AtomicInteger(0);
	protected static int numLength = 3;
	
	protected final String prefix;
	
	public ATempDir(String prefix) {
		this.prefix = prefix;
	}
	
	public void releaseTempFile(File file) throws FileNotFoundException, IOException {
		file.delete();
	}
	
	protected String generateNewName(String prefix, String suffix) {
		final StringBuilder sb = new StringBuilder();
		if (this.prefix != null)
			sb.append(this.prefix).append('_');
		if (prefix != null)
			sb.append(prefix).append('_');
		sb.append(formatUID(generateNewUID()));
		if (suffix != null)
			sb.append('.').append(suffix);
		return sb.toString();
	}
	
	protected String formatUID(int uid) {
		return String.format("%" + numLength + "d", Integer.valueOf(uid));
	}
	
	protected synchronized int generateNewUID() {
		return num.getAndIncrement();
	}
}
