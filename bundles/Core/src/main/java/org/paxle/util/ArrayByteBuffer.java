
package org.paxle.util;

import java.io.OutputStream;

public class ArrayByteBuffer extends OutputStream implements Cloneable {
	
	private static final int INC_BUF_BYTES = 8;
	
	protected byte[] buf;
	protected int len;
	
	public ArrayByteBuffer() {
		this(INC_BUF_BYTES);
	}
	
	public ArrayByteBuffer(final int len) {
		buf = new byte[len];
	}
	
	public ArrayByteBuffer(final byte[] buf) {
		this(buf, 0, buf.length);
	}
	
	public ArrayByteBuffer(final byte[] buf, final int off, final int len) {
		this.buf = new byte[len];
		System.arraycopy(buf, off, this.buf, 0, len);
		this.len = len;
	}
	
	public ArrayByteBuffer(final byte[] buf, final int len) {
		if (len > buf.length)
			throw new IndexOutOfBoundsException("len > buf.length: " + len);
		this.buf = buf;
		this.len = len;
	}
	
	@Override
	public ArrayByteBuffer clone() {
		try {
			final ArrayByteBuffer bb = (ArrayByteBuffer)super.clone();
			bb.buf = new byte[buf.length];
			System.arraycopy(buf, 0, bb.buf, 0, buf.length);
			bb.len = len;
			return bb;
		} catch (CloneNotSupportedException e) {
			throw new InternalError("clone not supported: " + e.getMessage());
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ArrayByteBuffer))
			return super.equals(obj);
		final ArrayByteBuffer bb = (ArrayByteBuffer)obj;
		final int len = this.len;
		if (len != bb.len)
			return false;
		final byte[] buf = this.buf, bbbuf = bb.buf;
		for (int i=0; i<len; i++)
			if (buf[i] != bbbuf[i])
				return false;
		return true;
	}
	
	protected void checkLength(final int additional) {
		final int add = len + additional - buf.length;
		if (add > 0) {
			final byte[] n = new byte[buf.length + Math.max(add, INC_BUF_BYTES)];
			System.arraycopy(buf, 0, n, 0, len);
			buf = n;
		}
	}
	
	/* -------------------------------------------------------------------- */
	
	public byte[] toByteArray() {
		final byte[] r = new byte[len];
		System.arraycopy(buf, 0, r, 0, len);
		return r;
	}
	
	public byte[] getBuffer() {
		return buf;
	}
	
	public int size() {
		return len;
	}
	
	public int capacity() {
		return buf.length;
	}
	
	public void clear() {
		len = 0;
	}
	
	/* -------------------------------------------------------------------- */
	
	@Override
	public void write(byte[] b, int off, int len) {
		append(b, off, len);
	}
	
	@Override
	public void write(byte[] b) {
		append(b, 0, b.length);
	}
	
	@Override
	public void write(int b) {
		append((byte)(b & 0xFF));
	}
	
	@Override
	public void close() {
	}
	
	@Override
	public void flush() {
	}
	
	/* -------------------------------------------------------------------- */
	
	public ArrayByteBuffer append(final byte b) {
		checkLength(1);
		buf[len++] = b;
		return this;
	}
	
	public ArrayByteBuffer append(final byte[] b) {
		return append(b, 0, b.length);
	}
	
	public ArrayByteBuffer append(final byte[] b, final int off, final int len) {
		checkLength(len);
		if (off < 0 || off + len > b.length)
			throw new ArrayIndexOutOfBoundsException("off: " + off + ", len: " + len);
		System.arraycopy(b, off, buf, this.len, len);
		this.len += len;
		return this;
	}
	
	/* -------------------------------------------------------------------- */
	
	public ArrayByteBuffer removeBytesAt(final int idx, final int num) {
		if (idx < 0 || idx > len)
			throw new IndexOutOfBoundsException("invalid index: " + idx + " (len: " + len + ")");
		final int loff = idx + num;
		if (num < 0 || loff > len)
			throw new IndexOutOfBoundsException("invalid num: " + num + ", index: " + idx + " (len: " + len + ")");
		
		if (loff == len - 1) {
			return removeLastBytes(num);
		} else {
			System.arraycopy(buf, idx + num, buf, idx, num);
			len -= num;
			return this;
		}
	}
	
	public ArrayByteBuffer removeLastBytes(final int num) {
		if (len < num)
			throw new IndexOutOfBoundsException("len < num, len: " + len + ", num: " + num);
		len -= num;
		return this;
	}
	
	public ArrayByteBuffer removeByteAt(final int idx) {
		if (idx < 0 || idx > len)
			throw new IndexOutOfBoundsException("invalid index: " + idx + " (len: " + len + ")");
		
		if (idx == len - 1) {
			return removeLast();
		} else {
			System.arraycopy(buf, idx + 1, buf, idx, len - idx);
			len--;
			return this;
		}
	}
	
	public ArrayByteBuffer removeLast() {
		if (len == 0)
			throw new IndexOutOfBoundsException("len == 0");
		len--;
		return this;
	}
}
