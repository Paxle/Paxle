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

package org.paxle.console.impl.desktop;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;

class ConsoleOutputStream extends OutputStream {
	/**
	 * 
	 */
	private final Console console;

	/**
	 * A byte buffer holding bytes that should be displayed to the user
	 */
	private Queue<Byte> byteBuffer = new LinkedList<Byte>();
	
	/**
	 * Indicates that the stream was already closed
	 */
	private boolean closed;

	public ConsoleOutputStream(Console console) {
		this.console = console;
		this.closed = false;
	}

	@Override
	public void write(int b) throws IOException {
		if (this.closed)  throw new IOException("Stream closed");

		synchronized (this.byteBuffer) {
			this.byteBuffer.offer((byte) b);
		}
	}

	@Override
	public void flush() {
		synchronized (this.byteBuffer) {
			byte[] b = new byte[byteBuffer.size()];
			int cnt = 0;
			while (!this.byteBuffer.isEmpty()) {
				b[cnt++] = this.byteBuffer.poll();
			}
			this.console.print(b, this);
		}
	}

	@Override
	public void close() {
		this.closed = true;
	}
}