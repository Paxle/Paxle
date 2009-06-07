import it.unimi.dsi.mg4j.tool.IndexBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.apache.commons.configuration.ConfigurationException;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.core.queue.ICommand;
import org.paxle.se.index.IFieldManager;
import org.paxle.se.index.mg4j.impl.PaxleDocumentFactory;
import org.paxle.se.index.mg4j.impl.PaxleDocumentSequence;


public class IndexBuilderTest extends MockObjectTestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	
	public void testBuildIndex() throws ConfigurationException, SecurityException, IOException, URISyntaxException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		final ICommand command = mock(ICommand.class);
		final IIndexerDocument indexerDoc = mock(IIndexerDocument.class);
		checking(new Expectations() {{
			atLeast(1).of(command).getIndexerDocuments(); will(returnValue(new IIndexerDocument[]{indexerDoc}));
			atLeast(1).of(indexerDoc).get(IIndexerDocument.TEXT); will(returnValue(new File("src/test/resources/Content.txt")));
			atLeast(1).of(indexerDoc).get(IIndexerDocument.AUTHOR); will(returnValue("ich"));
		}});
		
		IFieldManager filedManager = mock(IFieldManager.class);
		PaxleDocumentFactory docFactory = new PaxleDocumentFactory(filedManager);
		PaxleDocumentSequence documentSequence = new PaxleDocumentSequence(docFactory,Arrays.asList(new ICommand[]{command}));

		String fileName = "target/test" + System.currentTimeMillis();				
		IndexBuilder indexBuilder = new IndexBuilder(fileName, documentSequence)
		.skipBufferSize(32 * 1024);		
		indexBuilder.run();
	}
}
