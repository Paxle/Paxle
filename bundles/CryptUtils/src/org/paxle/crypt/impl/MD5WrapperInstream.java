package org.paxle.crypt.impl;

import java.io.IOException;
import java.io.InputStream;

import org.paxle.core.crypt.md5.AMD5InputStream;
import org.paxle.crypt.md5.MD5InputStream;

public class MD5WrapperInstream extends AMD5InputStream {
	
	private final MD5InputStream md5str;
	
	public MD5WrapperInstream(InputStream stream) {
		super(null);
		this.md5str = new MD5InputStream(stream);
	}
	
	@Override
	public int available() throws IOException {
		return this.md5str.available();
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
	public int hashCode() {
		return this.md5str.hashCode();
	}
	
	@Override
	public synchronized void mark(int readlimit) {
		this.md5str.mark(readlimit);
	}
	
	@Override
	public boolean markSupported() {
		return this.md5str.markSupported();
	}
	
	@Override
	public int read() throws IOException {
		return this.md5str.read();
	}
	
	@Override
	public int read(byte[] b) throws IOException {
		return this.md5str.read(b);
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return this.md5str.read(b, off, len);
	}
	
	@Override
	public synchronized void reset() throws IOException {
		this.md5str.reset();
	}
	
	@Override
	public long skip(long n) throws IOException {
		return this.md5str.skip(n);
	}
	
	@Override
	public byte[] getHash() {
		return this.md5str.hash();
	}
	
	@Override
	public String toString() {
		return this.md5str.toString();
	}
}
