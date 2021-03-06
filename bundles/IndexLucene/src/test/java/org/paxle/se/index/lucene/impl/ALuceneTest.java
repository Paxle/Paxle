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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jmock.integration.junit3.MockObjectTestCase;
import org.paxle.core.doc.IDocumentFactory;
import org.paxle.core.doc.impl.BasicDocumentFactory;
import org.paxle.se.index.IFieldManager;
import org.paxle.se.index.impl.FieldManager;

public abstract class ALuceneTest extends MockObjectTestCase {
	/**
	 * Path were the test-db should be stored or loaded from
	 */
	public static final String DATA_PATH = "target/writerTest";
	
	protected StopwordsManager stopwordsManager = null;
	
	protected IDocumentFactory docFactory = null;
	
	protected AFlushableLuceneManager lmanager = null;
	
	protected IFieldManager fieldManager = null;
	
	protected void initDataDirectory() throws IOException {
		// nothing todo here. overwrite this if necessary 
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// some required system-properties
		System.setProperty("paxle.data", DATA_PATH);
		this.initDataDirectory();
		
		// init the doc-factory
		this.docFactory = new BasicDocumentFactory();
		
		// init stopwordsmanager
		this.stopwordsManager = new StopwordsManager(){{
			initStopWords(StopwordsManagerTest.getStopwordsFiles());
		}};		
		
		// init field-manager
		this.fieldManager = new FieldManager();
		
		// init lucene manager
		final Map<String, Object> props = new HashMap<String, Object>();
		props.put("dataPath", "lucene-db");
		this.lmanager = new AFlushableLuceneManager() {{
			this.docFactory = ALuceneTest.this.docFactory;
			this.stopWordsManager = ALuceneTest.this.stopwordsManager;
			this.fieldManager = ALuceneTest.this.fieldManager;
			this.activate(props);
		}};
	}
}
