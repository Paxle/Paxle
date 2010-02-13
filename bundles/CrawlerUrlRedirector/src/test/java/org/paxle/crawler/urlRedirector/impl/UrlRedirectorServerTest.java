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

package org.paxle.crawler.urlRedirector.impl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.nio.BufferOverflowException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.paxle.core.norm.IReferenceNormalizer;
import org.paxle.data.db.ICommandDB;
import org.xsocket.connection.BlockingConnection;
import org.xsocket.connection.IBlockingConnection;

public class UrlRedirectorServerTest extends MockObjectTestCase {
	
	/**
	 * The server port
	 */
	private int port;
	
	/**
	 * The server
	 */
	private UrlRedirectorServer server;
	
	/**
	 * The client
	 */
	private IBlockingConnection client;
	
	/**
	 * The paxle command db
	 */
	private ICommandDB cmdDB;
	
	/**
	 * The paxle reference normalizer 
	 */
	private IReferenceNormalizer refNormalizer;
	
	private static int findFreePort() throws IOException {
		ServerSocket server = new ServerSocket(0);
		int port = server.getLocalPort();
		server.close();
		return port;
	}
	
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// detemine a free port
		this.port = findFreePort();
		
		// configure properties
		final Dictionary<String, Object> props = new Hashtable<String, Object>();
		props.put(UrlRedirectorServer.PORT, Integer.valueOf(this.port));
		
		// mocking external services
		this.refNormalizer = mock(IReferenceNormalizer.class);		
		this.cmdDB = mock(ICommandDB.class);
		
		// create and init server
		this.server = new UrlRedirectorServer(){{
			this.commandDB = cmdDB;
			this.referenceNormalizer = refNormalizer;
			
			// activate component
			this.activate(props);
		}};
		
		// init a client connection
		this.client = new BlockingConnection("localhost",this.port);
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		
		// shutdown client
		this.client.close();
		
		// shutdown server
		this.server.deactivate(null);
	}
	
	public void testUrlRedirector() throws BufferOverflowException, IOException, InterruptedException {
		final String testURLStr = "http://www.paxle.de";
		final URI testURL = URI.create(testURLStr);
		final Semaphore s = new Semaphore(0);
		
		checking(new Expectations(){{
			// URI must be normalized
			one(refNormalizer).normalizeReference(testURLStr); will(returnValue(testURL));
			
			// URI must be enqueued
			one(cmdDB).enqueue(testURL, -1, 0); will(new Action(){
				public void describeTo(Description arg0) {}

				public Object invoke(Invocation invocation) throws Throwable {
					s.release();
					return Boolean.TRUE;
				}				
			});
		}});		
		
		// the message to send
		String line = testURL + " 192.168.0.1/- - GET\r\n";

		// sending message to server
		int bytesWritten = this.client.write(line);
		assertEquals(line.getBytes().length, bytesWritten);
		
		// reading the response
		String res = this.client.readStringByDelimiter("\r\n");
		assertNotNull(res);
		assertEquals(testURLStr, res);
		
		// ensure that the URL was enqueued
		assertTrue(s.tryAcquire(10, TimeUnit.SECONDS));
	}
}
