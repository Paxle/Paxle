
package org.paxle.core.crypt.md5;

import java.io.FilterInputStream;
import java.io.InputStream;

public abstract class AMD5InputStream extends FilterInputStream implements IMD5Stream {
	
	protected AMD5InputStream(InputStream out) {
		super(out);
	}
	
	public abstract byte[] getHash();
}
