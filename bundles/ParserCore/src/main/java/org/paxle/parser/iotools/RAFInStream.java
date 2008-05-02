package org.paxle.parser.iotools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import org.paxle.parser.iotools.Seekable;

public class RAFInStream extends InputStream implements Seekable {
	
	private final RandomAccessFile _file;
	
	public RAFInStream(File file) throws IOException {
		this._file = new RandomAccessFile(file, "r");
	}
	
	public RAFInStream(String filename) throws IOException {
		_file = new RandomAccessFile(filename, "r");
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
	public int read() throws IOException {
		return _file.read();
	}
	
	@Override
	public int read(byte [] data, int off, int size) throws IOException {
		return _file.read(data,off,size);
	}
	
	public int read(byte [] data, int size) throws IOException {
		return _file.read(data,0,size);
	}
	
	@Override
	public void close() throws IOException {
		_file.close();
	}
}
