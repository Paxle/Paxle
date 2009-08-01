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
package org.paxle.crawler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import junitx.framework.FileAssert;

import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.io.impl.IOTools;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.crawler.impl.CrawlerTools;

public class CrawlerToolsTest extends MockObjectTestCase {
	private static final File TESTFILE = new File("src/test/resources/paxle.txt");
	
	private File outFile = new File("target/test.txt");
	private ITempFileManager tempFileManager; 
	private ICrawlerContext context; 
	private ICrawlerTools crawlerTools;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// clean old files if required
		if (this.outFile != null && this.outFile.exists()) 
			assertTrue(this.outFile.delete());
		
		// init dummy temp-file-manager
		this.tempFileManager = mock(ITempFileManager.class);
		checking(new Expectations(){{
			one(tempFileManager).createTempFile(); will(returnValue(outFile));
		}});		
		
		// initialize crawler context
		this.context = mock(ICrawlerContext.class);
		this.crawlerTools = new CrawlerTools(){{
			this.tfm = tempFileManager;
			this.ioTools = new IOTools();
		}};	
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		
		// cleanup
		if (this.outFile != null && this.outFile.exists()) 
			assertTrue(this.outFile.delete());
	}
	
	public void testSaveInto() throws IOException {
		final InputStream fileIn = new BufferedInputStream(new FileInputStream(TESTFILE));
		final ICrawlerDocument cdoc = mock(ICrawlerDocument.class);
		
		checking(new Expectations(){{
			// some doc properties
			allowing(cdoc).getCharset(); will(returnValue("UTF-8"));
			allowing(cdoc).getMimeType(); will(returnValue("text/plain"));
			one(cdoc).setContent(outFile);	
		}});
		long copied = this.crawlerTools.saveInto(cdoc, fileIn);
		assertEquals(copied, TESTFILE.length());
		assertTrue(this.outFile.exists());
		FileAssert.assertBinaryEquals(TESTFILE, this.outFile);
	}
	
	public void _testSaveIntoDummyData() throws IOException {
		final long max = 20*1024*1024;
		final InputStream fileIn = new InputStream() {
			private long count = 0;			
			private Random rand = new Random();			
			public int read() throws IOException {
				if (++count > max) return -1;				
				return rand.nextInt(Byte.MAX_VALUE);
			};
		};
		final ICrawlerDocument cdoc = mock(ICrawlerDocument.class);
		
		checking(new Expectations(){{
			// some doc properties
			allowing(cdoc).getCharset(); will(returnValue("UTF-8"));
			allowing(cdoc).getMimeType(); will(returnValue("text/plain"));
			one(cdoc).setContent(outFile);		
		}});
		long copied = this.crawlerTools.saveInto(cdoc, fileIn);
		assertTrue(this.outFile.exists());
		assertEquals(max, copied);
	}
}
