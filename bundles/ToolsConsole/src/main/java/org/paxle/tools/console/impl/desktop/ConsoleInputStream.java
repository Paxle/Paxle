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

/**
 * 
 */
package org.paxle.tools.console.impl.desktop;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Queue;

class ConsoleInputStream extends InputStream {
	/**
	 * 
	 */
	private final Console console;

	/**
	 * @param console
	 */
	public ConsoleInputStream(Console console) {
		this.console = console;
	}

	/**
	 * A buffer holding the commands the user has typed in
	 */
	private Queue<Byte> byteBuffer = new LinkedList<Byte>();
	
	/**
	 * Amount of bytes that are currently available for reading
	 */
	private int byteCounter = 0;

	/**
	 * Notifies a blocking reader about newly available bytes
	 */
	public synchronized void notifyReader() {
		this.notify();
	}

	@Override
	public synchronized int read() throws IOException {
		if (this.byteCounter == 0) {
			this.console.setInputEnabled(true);
			
			while (this.byteBuffer.isEmpty()) {
				// waiting for new bytes
				try {
					this.wait();
				} catch (Exception e) {}
			}
			this.console.setInputEnabled(false);
			
			this.byteCounter = this.byteBuffer.size() + 1;
		}
		this.byteCounter--;
		return byteBuffer.isEmpty() ? -1 : this.byteBuffer.poll();
	}

	public void fill(byte[] b) {
		for (byte i : b) {
			this.byteBuffer.offer(i);
		}
	}
}