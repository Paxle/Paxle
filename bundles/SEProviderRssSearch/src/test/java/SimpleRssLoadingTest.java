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
