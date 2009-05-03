/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package org.paxle.core.queue.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.HashMap;

import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.osgi.service.event.EventAdmin;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.filter.IFilterQueue;
import org.paxle.core.queue.ICommand;

public class CommandFilteringContextTest extends MockObjectTestCase {
	private CommandFilteringContext<ICommand> cmdCtx;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.cmdCtx = new CommandFilteringContext<ICommand>(mock(EventAdmin.class),mock(IFilterQueue.class)){{}};
	}
	
	@SuppressWarnings("serial")
	public void testCmdWrapper() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		// some dummy classes for testing
		final ICommand cmd1 = mock(ICommand.class);
		final ICrawlerDocument cdoc1 = mock(ICrawlerDocument.class);
		
		// command methods a caller is allowed to use and the expected test results
		final ExpectedResultsAction expectedCmdResults =  new ExpectedResultsAction(new HashMap<String, Object>(){{
			// a list of: methodNames + dummy-return values
			put("getProfileOID", Integer.valueOf(-1));
			put("getDepth", Integer.valueOf(-1));
			put("getLocation", URI.create("http://example.abc"));
			put("getCrawlerDocument", cdoc1);
		}});
		
		// crawler-doc methods a caller is allowed to use and the expected test results
		final ExpectedResultsAction expectedCdocResults = new ExpectedResultsAction(new HashMap<String, Object>(){{
			// a list of: methodNames + dummy-return values
			put("getMimeType", "text/plain");
			put("getSize", Long.valueOf(500));
		}});
		
		// creating a wrapper for the command
		final ICommand cmd2 = this.cmdCtx.createCmdWrapper(cmd1);
		assertNotNull(cmd2);
		assertNotSame(cmd1, cmd2);
		assertTrue(Proxy.isProxyClass(cmd2.getClass()));
		
		// testing allowed methods		
		checking(new Expectations(){{
			exactly(2).of(cmd1).getProfileOID(); will(expectedCmdResults);
			exactly(2).of(cmd1).getDepth(); will(expectedCmdResults);
			exactly(2).of(cmd1).getLocation(); will(expectedCmdResults);
			exactly(1).of(cmd1).getCrawlerDocument(); will(expectedCmdResults);
			
			exactly(2).of(cdoc1).getMimeType(); will(expectedCdocResults);
			exactly(2).of(cdoc1).getSize(); will(expectedCdocResults);
		}});
		
		// calling all allowed functions on the command
		assertEquals(cmd1.getProfileOID(), cmd2.getProfileOID());
		assertEquals(cmd1.getDepth(), cmd2.getDepth());
		assertEquals(cmd1.getLocation(), cmd2.getLocation());
		
		// calling all allowed functions on the cdoc
		final ICrawlerDocument cdoc2 = cmd2.getCrawlerDocument();
		assertNotNull(cdoc2);
		assertTrue(Proxy.isProxyClass(cdoc2.getClass()));
		assertEquals(cdoc1.getMimeType(), cdoc2.getMimeType());
		assertEquals(cdoc1.getSize(), cdoc2.getSize());
	}
	
	/**
	 * A jmock action just returning the expected results 
	 * contained in the {@link HashMap} passed to this class
	 */
	private static class ExpectedResultsAction implements Action {
		private final HashMap<String, Object> expectedCmdResults;
		
		public ExpectedResultsAction(HashMap<String, Object> expectedCmdResults) {
			this.expectedCmdResults = expectedCmdResults;
		}
		
		public void describeTo(Description arg0) {}
		public Object invoke(Invocation invocation) throws Throwable {
			final Method m = invocation.getInvokedMethod();
			return this.expectedCmdResults.get(m.getName());
		}				
	}	
}
