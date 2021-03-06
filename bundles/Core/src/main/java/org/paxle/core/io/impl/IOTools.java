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

package org.paxle.core.io.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.CharBuffer;

import org.paxle.core.io.IIOTools;

public class IOTools implements IIOTools {
	
	private static final int DEFAULT_BUFFER_SIZE_BYTES = 1024;
	private static final int DEFAULT_BUFFER_SIZE_CHARS = DEFAULT_BUFFER_SIZE_BYTES / 2;
	
	public long copy(Reader in, Appendable out) throws IOException {
		return copy(in, out, -1);
	}
	
	public long copy(Reader in, Appendable out, long chars) throws IOException {
		final char[] buf = new char[DEFAULT_BUFFER_SIZE_CHARS];                
		int cs = (int)((chars > 0 && chars < (DEFAULT_BUFFER_SIZE_CHARS)) ? chars : (DEFAULT_BUFFER_SIZE_CHARS));
		
		int rn;
		long rt = 0;
		while ((rn = in.read(buf, 0, cs)) > 0) {
			out.append(CharBuffer.wrap(buf, 0, rn));
			rt += rn;
			
			if (chars > 0) {
				cs = (int)Math.min(chars - rt, DEFAULT_BUFFER_SIZE_CHARS);
				if (cs == 0)
					break;
			}
		}
		return rt;
	}
	
	public long copy(InputStream is, OutputStream os) throws IOException {
		return copy(is, os, -1);
	}
	
	public long copy(InputStream is, OutputStream os, long bytes) throws IOException {
		final byte[] buf = new byte[DEFAULT_BUFFER_SIZE_BYTES];
		int cs = (int)((bytes > 0 && bytes < DEFAULT_BUFFER_SIZE_BYTES) ? bytes : DEFAULT_BUFFER_SIZE_BYTES);
		
		int rn;
		long rt = 0;
		while ((rn = is.read(buf, 0, cs)) > 0) {
			os.write(buf, 0, rn);
			rt += rn;
			
			if (bytes > 0) {
				cs = (int)Math.min(bytes - rt, DEFAULT_BUFFER_SIZE_BYTES);
				if (cs == 0)
					break;
			}
		}
		os.flush();
		return rt;
	}
	
	public long copy(final InputStream is, final OutputStream os, final long bytes, final int limitKBps) throws IOException {
		final long delta = (limitKBps == 0) ? 0 : 1000l / (limitKBps * 1024 / DEFAULT_BUFFER_SIZE_BYTES);
		if (delta > 0) {
			return copy(is, os, bytes, delta);
		} else {
			return copy(is, os, bytes);
		}
	}
	
	private long copy(final InputStream is, final OutputStream os, final long bytes, final long delta) throws IOException {
		final byte[] buf = new byte[DEFAULT_BUFFER_SIZE_BYTES];
		int cs = (int)((bytes > 0 && bytes < DEFAULT_BUFFER_SIZE_BYTES) ? bytes : DEFAULT_BUFFER_SIZE_BYTES);
		
		int rn;
		long rt = 0;
		long time = System.currentTimeMillis();
		try {
			while ((rn = is.read(buf, 0, cs)) > 0) {
				os.write(buf, 0, rn);
				rt += rn;
				
				time += delta;
				final long sleep = time - System.currentTimeMillis();
				// System.out.println("SLEEPING " + sleep + " ms");
				if (sleep > 0)
					Thread.sleep(sleep);
				
				if (bytes > 0) {
					cs = (int)Math.min(bytes - rt, DEFAULT_BUFFER_SIZE_BYTES);
					if (cs == 0)
						break;
				}
			}
		} catch (InterruptedException e) { /* ignore, simply quit copying */ }
		os.flush();
		return rt;
	}
}
