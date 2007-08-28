package org.paxle.crypt.impl;

import java.io.IOException;
import java.io.OutputStream;

import org.paxle.core.crypt.md5.AMD5OutputStream;
import org.paxle.crypt.md5.MD5OutputStream;

public class MD5WrapperOutStream extends AMD5OutputStream {
	
	private final MD5OutputStream md5str;
	
	public MD5WrapperOutStream(OutputStream stream) {
		super(null);
		this.md5str = new MD5OutputStream(stream);
	}
	
	@Override
	public byte[] getHash() {
		return this.md5str.hash();
	}
	
	@Override
	public void close() throws IOException {
		this.md5str.close();
	}
	
	@Override
	public boolean equals(Object obj) {
		return this.md5str.equals(obj);
	}
	
	@Override
	public void flush() throws IOException {
		this.md5str.flush();
	}
	
	@Override
	public int hashCode() {
		return this.md5str.hashCode();
	}
	
	@Override
	public String toString() {
		return this.md5str.toString();
	}
	
	@Override
	public void write(byte[] b) throws IOException {
		this.md5str.write(b);
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		this.md5str.write(b, off, len);
	}
	
	@Override
	public void write(int b) throws IOException {
		this.md5str.write(b);
	}
}
