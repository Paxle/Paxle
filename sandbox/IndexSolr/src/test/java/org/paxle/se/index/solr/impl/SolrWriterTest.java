package org.paxle.se.index.solr.impl;

import java.net.URL;

import org.jmock.integration.junit3.MockObjectTestCase;
import org.paxle.core.data.IDataSource;
import org.paxle.core.doc.ICommand;

import junit.framework.TestCase;

public class SolrWriterTest extends MockObjectTestCase {
	private SolrWriter writer;
	private IDataSource<ICommand> dataSource;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		this.writer = new SolrWriter(null, new URL("http://localhost:8983/solr"));
		this.writer.start();
	}
	
	public void test() {
		
	}
}
