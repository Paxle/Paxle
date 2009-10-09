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
package org.paxle.filter.plaintextdumper.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.doc.impl.BasicParserDocument;
import org.paxle.core.io.impl.IOTools;
import org.paxle.core.io.temp.impl.TempFileManager;

public class PlaintextDumperTest extends MockObjectTestCase {

	IParserDocument pdoc = null;
	File tmpdir = new File("target/test-tmp");
	File tmpFile = new File(tmpdir,"test.txt");
	
	PlaintextDumperFilter dumper;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (tmpFile.exists()) tmpFile.delete();
		
		// creating the temp directory 
		this.tmpdir.mkdir();
		System.out.println("Using data dir: " + tmpdir.getAbsolutePath());
		System.setProperty("paxle.data", tmpdir.getAbsolutePath());
		
		// creating a dummy parser-doc
		this.pdoc = new BasicParserDocument(new TempFileManager());
		this.pdoc.setTextFile(tmpFile);
		this.pdoc.setStatus(IParserDocument.Status.OK);
		
		// creating the dumper filter
		final Map<String, Object> props = new HashMap<String, Object>();
		props.put("dataPath", "plaintext-dumper");		
		
		dumper = new PlaintextDumperFilter(){{
			this.ioTools = new IOTools();
			this.activate(props);
		}};
	}
	
	public void testTextExtraction() throws IOException {
		String test = "a test text";
		this.pdoc.append(test);
		this.pdoc.close();
		
		InputStream input = null;
		File target = null;
		try {
			target = dumper.store(this.pdoc);		
			String targetString = IOUtils.toString(input = new FileInputStream(target), "UTF-8");
			assertEquals(test, targetString);
		} finally {
			if (input != null) input.close();
			if (target != null) target.delete();
		}
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		this.tmpdir.delete();
	}
}
