package org.paxle.parser.sevenzip.impl;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import SevenZip.IInStream;

/**
 * @see SevenZip.MyRandomAccessFile
 */
public class RAFInStream extends IInStream {
	
	RandomAccessFile _file;
	
	public RAFInStream(File file) throws IOException {
		this._file = new RandomAccessFile(file, "r");
	}
	
	public RAFInStream(String filename) throws IOException {
		_file = new RandomAccessFile(filename, "r");
	}
	
	public long Seek(long offset, int seekOrigin) throws IOException {
		if (seekOrigin == STREAM_SEEK_SET) {
			_file.seek(offset);
		} else if (seekOrigin == STREAM_SEEK_CUR) {
			_file.seek(offset + _file.getFilePointer());
		}
		return _file.getFilePointer();
	}
	
	public int read() throws IOException {
		return _file.read();
	}
	
	public int read(byte [] data, int off, int size) throws IOException {
		return _file.read(data,off,size);
	}
	
	public int read(byte [] data, int size) throws IOException {
		return _file.read(data,0,size);
	}
	
	public void close() throws IOException {
		_file.close();
		_file = null;
	}
}
