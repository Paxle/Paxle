package org.paxle.core.crypt;

import java.io.InputStream;
import java.io.OutputStream;

public interface ICrypt {
	
	public static final String CRYPT_NAME_PROP = "crypt.name";
	
	public abstract ACryptInputStream createInputStream(InputStream stream);
	public abstract ACryptOutputStream createOutputStream(OutputStream stream);
}
