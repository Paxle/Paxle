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

import java.io.File;
import java.io.IOException;

import de.nava.informa.core.ChannelFormat;
import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ParseException;
import de.nava.informa.impl.basic.ChannelBuilder;
import de.nava.informa.parsers.FeedParser;
import junit.framework.TestCase;


public class SimpleRssLoadingTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testLoadRssFromFile() throws IOException, ParseException {
		File testFile = new File("src/test/resources/test.rss");
		assertTrue(testFile.exists());
		assertTrue(testFile.canRead());
		
        // creating a channel-builder
        ChannelBuilder builder = new ChannelBuilder();   
        
        // parsing the rss/atom feed
        ChannelIF channel = FeedParser.parse(builder, testFile);
        assertNotNull(channel);
        
        assertNotNull(channel.getFormat());
        assertEquals(ChannelFormat.RSS_2_0, channel.getFormat());
        
        assertNotNull(channel.getTitle());
        assertEquals("SourceForge.net: SF.net Project News: News Aggregation Library for Java", channel.getTitle());
        
        assertNotNull(channel.getItems());
        assertEquals(10, channel.getItems().size());
	}
}
