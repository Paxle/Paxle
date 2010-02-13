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

package org.paxle.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.io.impl.IOTools;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.parser.impl.ParserContextLocal;

public class ASubParserTest extends MockObjectTestCase {
	private final File TESTFILE = new File("src/test/resources/test.txt");
	
	private IParserContext pContext;
	private ITempFileManager tempFileManager;
	private File testFile;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// creating a dummy temp-file-manager
		this.tempFileManager = mock(ITempFileManager.class);
		this.testFile = new File("target/test.txt");
		
		// init the parser-context
		this.pContext = mock(IParserContext.class);
		ParserContext.setThreadLocal(new ParserContextLocal(){
			@Override
			public IParserContext get() {
				return pContext;
			}
		});
		
		checking(new Expectations(){{
			// allowing to get the temp-file-manager
			allowing(pContext).getTempFileManager();
			will(returnValue(tempFileManager));
			
			// allowing to get the iotools
			allowing(pContext).getIoTools();
			will(returnValue(new IOTools()));
		}});
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		if (testFile != null && testFile.exists()) 
			assertTrue(testFile.delete());
	}
	
	public void testParseFromStream() throws IOException, ParserException {
		// a dummy parser-doc
		final IParserDocument pdoc1 = mock(IParserDocument.class);
		
		// a dummy parser
		final ISubParser dummyParser = new ASubParser() {
			@Override
			public IParserDocument parse(URI location, String charset, File content) throws ParserException, UnsupportedEncodingException, IOException {
				return pdoc1;
			}
		};
		
		checking(new Expectations(){{
			// creating a dummy temp-file
			one(tempFileManager).createTempFile();
			will(returnValue(testFile));
			
			// temp file must be released
			one(tempFileManager).isKnown(testFile); will(returnValue(true));
			one(tempFileManager).releaseTempFile(testFile);
		}});
		
		FileInputStream fin = new FileInputStream(TESTFILE);
		final IParserDocument pdoc2 = dummyParser.parse(URI.create("http://example.abc"), null, fin);
		assertNotNull(pdoc2);
		assertSame(pdoc1,pdoc2);		
	}
}
