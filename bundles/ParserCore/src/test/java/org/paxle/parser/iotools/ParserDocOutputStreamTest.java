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
package org.paxle.parser.iotools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.parser.IParserContext;
import org.paxle.parser.ISubParser;
import org.paxle.parser.ParserContext;
import org.paxle.parser.ParserException;
import org.paxle.parser.impl.ParserContextLocal;

public class ParserDocOutputStreamTest extends MockObjectTestCase {
	private static final File TESTFILE = new File("src/test/resources/test.txt");
	
	private File testFile;
	private ITempFileManager tempFileManager;
	private IParserContext pContext;
	private ISubParser parser;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// creating a dummy temp-file manager
		this.tempFileManager = mock(ITempFileManager.class);
		this.testFile = new File("target/test.txt");

		// init a dummy parser
		this.parser = mock(ISubParser.class);
		
		// initialize the parser-context
		this.pContext = mock(IParserContext.class);
		ParserContext.setThreadLocal(new ParserContextLocal(){
			@Override
			public IParserContext get() {
				return pContext;
			}
		});
	}
	
	public void testParserDocStream() throws IOException, ParserException {
		final URI location = URI.create("http://example.abc");
		final String mimeType = "text/plain";
		final IParserDocument pdoc1 = mock(IParserDocument.class);
		
		checking(new Expectations(){{
			// creating a dummy temp-file
			one(tempFileManager).createTempFile();
			will(returnValue(testFile));
			
			// releasing temp-file
			one(tempFileManager).releaseTempFile(testFile);
			
			// fetching the parser
			one(pContext).getParser("text/plain");
			will(returnValue(parser));
			
			// parsing data
			one(parser).parse(location, null, testFile);
			will(returnValue(pdoc1));
			
			ignoring(pdoc1);
		}});
		
		final FileInputStream fin = new FileInputStream(TESTFILE);
		final ParserDocOutputStream out = new ParserDocOutputStream(this.tempFileManager, null);
		
		// copy data into the parser-doc-stream
		IOUtils.copy(fin, out);
		fin.close();
		out.close();
		
		IParserDocument pdoc2 = out.parse(location, mimeType);
		assertNotNull(pdoc2);
		assertSame(pdoc1, pdoc2);
	}
}
