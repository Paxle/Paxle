
package org.paxle.core.crypt;

import java.io.FilterInputStream;
import java.io.InputStream;

public abstract class ACryptInputStream extends FilterInputStream implements ICryptStream {
	
	public ACryptInputStream(InputStream in) {
		super(in);
	}
	
	public abstract byte[] getHash();
}
