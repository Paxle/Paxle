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

package org.paxle.parser.impl;

import java.net.URI;

import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.paxle.core.doc.ICommand;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.doc.ICommand.Result;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.parser.ISubParserManager;

public class MimeTypeFilterTest extends MockObjectTestCase {
	private IFilter<ICommand> mimeTypeFilter;
	private ISubParserManager parserManager;
	private ICommand cmd;
	private ICrawlerDocument cdoc;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.parserManager = mock(ISubParserManager.class);
		this.cmd = mock(ICommand.class);
		this.cdoc = mock(ICrawlerDocument.class);
		
		this.mimeTypeFilter = new MimeTypeFilter() {{
			this.subParserManager = parserManager;
		}};
		
		checking(new Expectations(){{
			allowing(cmd).getLocation(); will(returnValue(URI.create("http://example.abc")));
		}});
	}
	
	public void testFilterKnownMimeType() {
		final String mimeType = "text/html";
		checking(new Expectations(){{
			atLeast(1).of(cmd).getCrawlerDocument(); will(returnValue(cdoc));
			one(cdoc).getMimeType(); will(returnValue(mimeType));			
			
			// the given mime-type is supported
			one(parserManager).isSupported(mimeType); will(returnValue(Boolean.TRUE));
		}});
		
		this.mimeTypeFilter.filter(this.cmd, mock(IFilterContext.class));
	}
	
	public void testFilterUnknownMimeType() {
		final String mimeType = "text/unknown";
		checking(new Expectations(){{
			atLeast(1).of(cmd).getCrawlerDocument(); will(returnValue(cdoc));
			one(cdoc).getMimeType(); will(returnValue(mimeType));			
			
			// the given mimetype is not supported
			one(parserManager).isSupported(mimeType); will(returnValue(Boolean.FALSE));
			
			// the command needs to be rejected
			one(cmd).setResult(with(equal(Result.Rejected)),with(any(String.class)));
		}});
		
		this.mimeTypeFilter.filter(this.cmd, mock(IFilterContext.class));
	}	
}
