package org.paxle.core.crypt.md5;

import java.io.InputStream;
import java.io.OutputStream;

public interface IMD5 {
	
	public abstract AMD5InputStream createInputStream(InputStream stream);
	public abstract AMD5OutputStream createOutputStream(OutputStream stream);
}
