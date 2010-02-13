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

package org.paxle.filter.robots.impl;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.paxle.core.doc.LinkInfo;
import org.paxle.core.doc.LinkInfo.Status;
import org.paxle.filter.robots.IRobotsTxtManager;

public class RobotsTxtFilterTest extends MockObjectTestCase {
	private IRobotsTxtManager manager;
	private RobotsTxtFilter filter;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.manager = mock(IRobotsTxtManager.class);
		this.filter = new RobotsTxtFilter(){{
			this.robotsTxtManager = manager;
		}};
	}
	
	public void testFilterURI() {
		final URI link1 = URI.create("http://www.test1.at/");
		final URI link2 = URI.create("http://www.test2.at/");
		final URI link3 = URI.create("http://www.test3.at/");
		
		final Map<URI, LinkInfo> uriMap = new HashMap<URI, LinkInfo>();
		uriMap.put(link1, new LinkInfo());
		uriMap.put(link2, new LinkInfo());
		uriMap.put(link3, new LinkInfo());
		
		// define mock conditions
		checking(new Expectations() {{
			one(manager).isDisallowed(with(any(Collection.class))); 
			will(returnValue(Arrays.asList(new URI[]{
				link1,
				link3
			})));
		}});
		
		// check URI by the filter
		this.filter.checkRobotsTxt(uriMap, new RobotsTxtFilter.Counter());
		
		// check if the status was set properly
		assertEquals(LinkInfo.Status.FILTERED, uriMap.get(link1).getStatus());
		assertEquals(LinkInfo.Status.OK, uriMap.get(link2).getStatus());
		assertEquals(LinkInfo.Status.FILTERED, uriMap.get(link3).getStatus());	
	}
	
	public void testSkippNotOKURI() {
		final URI link = URI.create("http://www.test1.at/");
		
		final Map<URI, LinkInfo> uriMap = new HashMap<URI, LinkInfo>();
		uriMap.put(link, new LinkInfo("test", Status.FILTERED, "Testcase"));
		
		// define mock conditions
		checking(new Expectations() {{
			never(manager);
		}});
		
		// check URI by the filter
		this.filter.checkRobotsTxt(uriMap, new RobotsTxtFilter.Counter());
	}
}
