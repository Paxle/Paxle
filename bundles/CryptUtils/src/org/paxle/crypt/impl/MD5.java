
package org.paxle.crypt.impl;

import java.io.InputStream;
import java.io.OutputStream;

import org.paxle.core.crypt.md5.AMD5InputStream;
import org.paxle.core.crypt.md5.AMD5OutputStream;
import org.paxle.core.crypt.md5.IMD5;

public class MD5 implements IMD5 {
	
	public AMD5InputStream createInputStream(InputStream stream) {
		return new MD5WrapperInstream(stream);
	}
	
	public AMD5OutputStream createOutputStream(OutputStream stream) {
		return new MD5WrapperOutStream(stream);
	}
}
