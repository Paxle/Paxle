package org.paxle.crypt.impl;

import java.io.InputStream;
import java.io.OutputStream;

import org.paxle.core.crypt.ACryptInputStream;
import org.paxle.core.crypt.ACryptOutputStream;
import org.paxle.core.crypt.ICrypt;
import org.paxle.crypt.impl.md5.MD5WrapperInstream;
import org.paxle.crypt.impl.md5.MD5WrapperOutStream;

public enum Impls implements ICrypt {
	MD5("md5") {
		public ACryptInputStream createInputStream(InputStream stream) {
			return new MD5WrapperInstream(stream);
		}
		
		public ACryptOutputStream createOutputStream(OutputStream stream) {
			return new MD5WrapperOutStream(stream);
		}
	}
	
	;
	
	public final String name;
	
	private Impls(String name) {
		this.name = name;
	}
}
