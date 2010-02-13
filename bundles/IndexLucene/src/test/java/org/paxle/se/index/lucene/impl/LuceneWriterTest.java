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

package org.paxle.se.index.lucene.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.commons.io.FileUtils;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.paxle.core.data.IDataSource;
import org.paxle.core.doc.ICommand;
import org.paxle.core.doc.ICommandTracker;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.core.doc.impl.BasicCommand;
import org.paxle.se.index.IIndexWriter;

public class LuceneWriterTest extends ALuceneTest {
	
	private ICommandTracker cmdTracker;	
	private ArrayBlockingQueue<ICommand> queue = null;
	private IDataSource<ICommand> dataSource = null;
	
	private LuceneWriter writer = null;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// a dummy indexer-input-queue
		this.queue = new ArrayBlockingQueue<ICommand>(5);
		this.dataSource = new DummySource();
		
		// create a dummy command tracker
		this.cmdTracker = mock(ICommandTracker.class);		
		
		// create an index writer
		this.writer = new LuceneWriter() {{
			this.manager = lmanager;
			this.stopwordsManager = LuceneWriterTest.this.stopwordsManager;
			this.commandTracker = LuceneWriterTest.this.cmdTracker;
			this.activate(null);
		}};
		this.writer.setDataSource(this.dataSource);
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		
		// stopping lucene-writer and -manager
		this.lmanager.deactivate();
		this.writer.deactivate();
		
		// delete files
		FileUtils.deleteDirectory(new File(LuceneWriterTest.DATA_PATH));
	}	
	
	/**
	 * A dummy {@link IDataSource} to feed the {@link IIndexWriter} with data
	 */
	class DummySource implements IDataSource<ICommand> {
		public ICommand getData() throws Exception {
			return queue.take();
		}		
	}
	
	/**
	 * A helper jmock-action to wait for the {@link IIndexWriter} to finish processing of the
	 * test-command.
	 */
	static class WaitForIndexer implements Action {
		private boolean returned = false;

		public synchronized void waitForIndexer() throws InterruptedException {
			if (!returned) this.wait(5000);
			if (!returned) throw new IllegalStateException("Indexer never returned!");
		}
		
		public void describeTo(Description arg0) {}

		public synchronized Object invoke(Invocation invocation) throws Throwable {
			this.returned = true;
			this.notifyAll();
			return null;
		}
	}	
	
	/**
	 * Function to create a {@link ICommand dummy-command}
	 * @return
	 * @throws IOException 
	 */
	private ICommand createTestCommand() throws IOException {
		File testData = new File("src/test/resources/test.txt");
		assertTrue(testData.exists());
		
		ICommand cmd = new BasicCommand();
		cmd.setLocation(URI.create("http://www.paxle.net"));
		cmd.setOID((int)System.currentTimeMillis());
		cmd.setResult(ICommand.Result.Passed);
		
		IIndexerDocument idxDoc = this.docFactory.createDocument(IIndexerDocument.class);
		idxDoc.setOID((int)System.currentTimeMillis());
		idxDoc.setStatus(IIndexerDocument.Status.OK);
		
		idxDoc.set(IIndexerDocument.LOCATION, "http://www.paxle.net");
		idxDoc.set(IIndexerDocument.TITLE, "Test-Document");
		idxDoc.set(IIndexerDocument.TEXT, testData);
		idxDoc.set(IIndexerDocument.SIZE, testData.length());
		idxDoc.set(IIndexerDocument.LANGUAGES, new String[]{"en"});
		idxDoc.set(IIndexerDocument.LAST_CRAWLED, new Date());
		idxDoc.set(IIndexerDocument.LAST_MODIFIED, new Date());
		idxDoc.set(IIndexerDocument.MIME_TYPE, "text/plain");
		idxDoc.set(IIndexerDocument.AUTHOR, "Paxle");
	
		cmd.addIndexerDocument(idxDoc);
		return cmd;
	}
	
	public void testIndexDocument() throws InterruptedException, IOException {
		assertEquals(0, this.lmanager.getDocCount());
		
		// creating a test command
		final ICommand testCmd = this.createTestCommand();
		
		final WaitForIndexer waitforIndexer = new WaitForIndexer();
		checking(new Expectations(){{
			// indexer must use command-tracking
			one(cmdTracker).commandDestroyed(LuceneWriter.class.getName(), testCmd); 
			will(waitforIndexer);
		}});
		
		// enqueue the cmd
		this.queue.add(testCmd);
		
		// waitforReturnToPool until indexer has finisehd
		waitforIndexer.waitForIndexer();		
		
		// testing document count
		assertEquals(1,this.lmanager.getDocCount());
		
		// testing indexed words
		this.lmanager.flush();
		final Iterator<String> words = this.lmanager.wordIterator();
		assertNotNull(words);
		while (words.hasNext()) {
			System.out.println("* " + words.next());
		}
	}
}
