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
package org.paxle.crawler.impl;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.paxle.core.doc.LinkInfo;
import org.paxle.core.doc.LinkInfo.Status;
import org.paxle.crawler.ISubCrawlerManager;

public class ProtocolFilterTest extends MockObjectTestCase {
	private ISubCrawlerManager crawlerManager;
	private ProtocolFilter filter;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.crawlerManager = mock(ISubCrawlerManager.class);
		this.filter = new ProtocolFilter(this.crawlerManager);
	}
	
	public void testProtocolIsSupported() throws ProtocolFilterException {
		// define test URI
		URI testUri = URI.create("http://www.test.at/");
		
		// define mock conditions
		checking(new Expectations() {{
			one(crawlerManager).isSupported("http"); will(returnValue(Boolean.TRUE));
		}});
		
		// check protocol, should be ok
		this.filter.checkProtocol(testUri);
	}
	
	public void testProtocolIsNotSupported() throws ProtocolFilterException {
		// define test URI
		URI testUri = URI.create("xxx://www.test.at/");
		
		// define mock conditions
		checking(new Expectations() {{
			one(crawlerManager).isSupported("xxx"); will(returnValue(Boolean.FALSE));
		}});
		
		try {
			this.filter.checkProtocol(testUri);
			fail("An ProtocolFilterException was expected");
		} catch (ProtocolFilterException e) {
			
		}
	}
	
	public void testProtocolMap() throws ProtocolFilterException {
		URI link1 = URI.create("http://www.test.at/");
		URI link2 = URI.create("xxx://www.test.at/");
		
		Map<URI, LinkInfo> uriMap = new HashMap<URI, LinkInfo>();
		uriMap.put(link1, new LinkInfo());
		uriMap.put(link2, new LinkInfo());
		
		// define mock conditions
		checking(new Expectations() {{			
			one(crawlerManager).isSupported("http"); will(returnValue(Boolean.TRUE));
			one(crawlerManager).isSupported("xxx"); will(returnValue(Boolean.FALSE));
		}});
		
		// check protocol
		this.filter.checkProtocol(uriMap);
		
		assertEquals(LinkInfo.Status.OK, uriMap.get(link1).getStatus());
		assertEquals(LinkInfo.Status.FILTERED, uriMap.get(link2).getStatus());		
	}
	
	public void testMustSkipNotOKUri() {
		Map<URI, LinkInfo> uriMap = new HashMap<URI, LinkInfo>();
		uriMap.put(URI.create("http://www.test1.at/"), new LinkInfo("test1", Status.FILTERED, "Testcase"));
		uriMap.put(URI.create("http://www.test2.at/"), new LinkInfo("test1", Status.FILTERED, "Testcase"));
		
		checking(new Expectations() {{
			never(crawlerManager);
		}});
		
		// check protocol
		this.filter.checkProtocol(uriMap);
	}
}
