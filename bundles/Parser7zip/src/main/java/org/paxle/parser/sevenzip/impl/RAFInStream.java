/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2010 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.parser.sevenzip.impl;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

import SevenZip.IInStream;

public class RAFInStream extends IInStream {
	
	private final RandomAccessFile _file;
	
	public RAFInStream(File file) throws IOException {
		this._file = new RandomAccessFile(file, "r");
	}
	
	public RAFInStream(String filename) throws IOException {
		_file = new RandomAccessFile(filename, "r");
	}
	
	@Override
	@SuppressWarnings("NM_METHOD_NAMING_CONVENTION")
	public long Seek(long offset, int seekOrigin) throws IOException {
		if (seekOrigin == STREAM_SEEK_SET) {
			_file.seek(offset);
		} else if (seekOrigin == STREAM_SEEK_CUR) {
			_file.seek(offset + _file.getFilePointer());
		}
		return _file.getFilePointer();
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
