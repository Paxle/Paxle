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
package org.paxle.data.db.impl;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.paxle.core.doc.Command;
import org.paxle.core.doc.CommandProfile;
import org.paxle.core.doc.ICommand;
import org.paxle.core.doc.ICommandProfile;
import org.paxle.core.doc.ICommandProfileManager;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.doc.LinkInfo;
import org.paxle.core.doc.LinkInfo.Status;

public class CommandProfileFilterTest extends MockObjectTestCase {
	private CommandProfileFilter filter;
	private ICommandProfileManager manager;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.manager = mock(ICommandProfileManager.class);
		this.filter = new CommandProfileFilter(this.manager);
	}
	
	public void testCheckLinks() {
		final Map<URI, LinkInfo> uriMap = new HashMap<URI, LinkInfo>();
		for (int i=0; i< 3; i++) uriMap.put(URI.create("http://test.at/" + i), new LinkInfo());

		final IParserDocument pDoc = mock(IParserDocument.class);
		checking(new Expectations() {{
			exactly(2).of(pDoc).getLinks(); will(returnValue(uriMap));
			ignoring(pDoc);
		}});
		
		// check URIs: maxdepth > current-depth
		final ICommandProfile profile = new CommandProfile();
		profile.setMaxDepth(2);
		
		final ICommand command1 = new Command();
		command1.setDepth(1);
		
		this.filter.checkLinks(profile, command1, pDoc, new CommandProfileFilter.Counter());
		
		// no URI must be marked as filter now
		for (LinkInfo meta: uriMap.values()) {
			assertTrue(meta.hasStatus(Status.OK));
		}
		
		// check URIs: maxdepth > current-depth
		final ICommand command2 = new Command();
		command2.setDepth(2);		
		this.filter.checkLinks(profile, command2, pDoc, new CommandProfileFilter.Counter());
		
		// all URI must be marked as filter now
		for (LinkInfo meta: uriMap.values()) {
			assertTrue(meta.hasStatus(Status.FILTERED));
		}
	}
}
