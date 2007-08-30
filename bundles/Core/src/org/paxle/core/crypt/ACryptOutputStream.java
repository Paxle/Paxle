
package org.paxle.core.crypt;

import java.io.FilterOutputStream;
import java.io.OutputStream;

public abstract class ACryptOutputStream extends FilterOutputStream implements ICryptStream {
	
	public ACryptOutputStream(OutputStream out) {
		super(out);
	}
	
	public abstract byte[] getHash();
}
