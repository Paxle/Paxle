package org.paxle.parser.iotools;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import org.paxle.parser.iotools.Seekable;

public class RAFOutStream extends OutputStream implements Seekable {
	
	private final RandomAccessFile _file;
	
	public RAFOutStream(File file) throws IOException {
		this._file = new RandomAccessFile(file, "rws");
	}
	
	public RAFOutStream(String filename) throws IOException {
		_file = new RandomAccessFile(filename, "rws");
	}
	
	public long seekAbsolute(long pos) throws IOException {
		this._file.seek(pos);
		return this._file.getFilePointer();
	}
	
	public long seekRelative(long pos) throws IOException {
		this._file.seek(this._file.getFilePointer() + pos);
		return this._file.getFilePointer();
	}
	
	@Override
	public void write(int b) throws IOException {
		this._file.write(b);
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		this._file.write(b, off, len);
	}
	
	@Override
	public void close() throws IOException {
		_file.close();
	}
}
