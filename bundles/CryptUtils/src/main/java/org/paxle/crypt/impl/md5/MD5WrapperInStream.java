
package org.paxle.crypt.impl.md5;

import java.io.InputStream;

import org.paxle.core.crypt.ACryptInputStream;

public class MD5WrapperInStream extends ACryptInputStream {
	
	public MD5WrapperInStream(InputStream in) {
		super(new MD5InputStream(in));
	}
	
	@Override
	public byte[] getHash() {
		return ((MD5InputStream)super.in).hash();
	}
}
