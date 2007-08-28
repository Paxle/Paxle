package org.paxle.core.crypt.md5;

import java.io.FilterOutputStream;
import java.io.OutputStream;

public abstract class AMD5OutputStream extends FilterOutputStream implements IMD5Stream {
	
	protected AMD5OutputStream(OutputStream stream) {
		super(stream);
	}
	
	public abstract byte[] getHash();
}
