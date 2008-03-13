
package org.paxle.crypt.impl.md5;

import java.io.OutputStream;

import org.paxle.core.crypt.ACryptOutputStream;

public class MD5WrapperOutStream extends ACryptOutputStream {
	
	public MD5WrapperOutStream(OutputStream out) {
		super(new MD5OutputStream(out));
	}
	
	@Override
	public byte[] getHash() {
		return ((MD5OutputStream)super.out).hash();
	}
}
