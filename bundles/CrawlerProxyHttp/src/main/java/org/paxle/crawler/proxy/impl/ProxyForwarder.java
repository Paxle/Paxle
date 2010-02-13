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

package org.paxle.crawler.proxy.impl;

import java.io.IOException;
import java.io.PipedOutputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xsocket.Execution;
import org.xsocket.connection.http.BodyDataSink;
import org.xsocket.connection.http.HttpResponse;
import org.xsocket.connection.http.IBodyDataHandler;
import org.xsocket.connection.http.NonBlockingBodyDataSource;

/**
 * This class forwards the body of a {@link HttpResponse}-message
 * to the client and furthermore pipes the {@link HttpResponse}-message
 * into a {@link PipedOutputStream} 
 */
public class ProxyForwarder implements IBodyDataHandler {
	final Log logger = LogFactory.getLog(this.getClass());
	final PipedOutputStream fileDataSink;
	final BodyDataSink clientDataSink;
	
	public ProxyForwarder(PipedOutputStream responseBodyPipe, BodyDataSink clientOut) {
		this.fileDataSink = responseBodyPipe;
		this.clientDataSink = clientOut;
	}
	
	@Execution(Execution.MULTITHREADED)
	public boolean onData(NonBlockingBodyDataSource bodyDataSource) throws BufferUnderflowException {
		try {
			int available = bodyDataSource.available();
			if (available > 0) {
				ByteBuffer[] data = bodyDataSource.readByteBufferByLength(available);
				if (this.fileDataSink != null) {
					for (ByteBuffer buf : data) {
						ByteBuffer copy = buf.duplicate();
						while(copy.hasRemaining()) {
							this.fileDataSink.write(copy.get());
						}

//						Charset charset = Charset.forName("UTF-8");
//						System.out.print(charset.decode(buf.duplicate()).toString());

					}				
				}
				this.clientDataSink.write(data);
			} else if (available == -1) {
				this.clientDataSink.close();
				if (this.fileDataSink != null) this.fileDataSink.close();
			}
		} catch (Throwable ioe) {
			if (this.fileDataSink != null) try {this.fileDataSink.close(); } catch (IOException e) {/* ignore this */ }
			this.clientDataSink.destroy();
			this.logger.error(String.format("Unexpected '%s': %s", ioe.getClass().getName(), ioe.getMessage()),ioe);
		}

		return true;
	}
}
