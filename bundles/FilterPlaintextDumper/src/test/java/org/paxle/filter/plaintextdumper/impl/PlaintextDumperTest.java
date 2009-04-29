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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;

import junit.framework.TestCase;

import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.doc.ParserDocument;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.core.io.temp.impl.TempFileManager;
import org.paxle.filter.plaintextdumper.impl.PlaintextDumperFilter;
import org.paxle.parser.CachedParserDocument;

public class PlaintextDumperTest extends MockObjectTestCase {

	IParserDocument pdoc = null;
	File tmpdir = new File("target/test-tmp");
	File tmpFile = new File(tmpdir,"test.txt");
	
	PlaintextDumperFilter dumper;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// creating the temp directory 
		this.tmpdir.mkdir();
		System.out.println("Using data dir: " + tmpdir.getAbsolutePath());
		System.setProperty("paxle.data", tmpdir.getAbsolutePath());
		
		// creating a dummy parser-doc
		this.pdoc = new ParserDocument();
		this.pdoc.setTextFile(tmpFile);
		this.pdoc.setStatus(IParserDocument.Status.OK);
		
		// creating the dumper filter
		final Hashtable<String, String> props = new Hashtable<String, String>();
		props.put("dataPath", "plaintext-dumper");		
		final ComponentContext ctx = mock(ComponentContext.class);
		checking(new Expectations(){{
			allowing(ctx).getProperties(); will(returnValue(props));
		}});
		
		dumper = new PlaintextDumperFilter(){{
			this.activate(ctx);
		}};
	}

	private String loadFile(File file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		StringBuffer sb = new StringBuffer(2048);
		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		br.close();
		return sb.toString();
	}
	
	public void testTextExtraction() throws IOException {
		String test = "a test text";
		this.pdoc.addText(test);
		this.pdoc.close();
		
		File target = dumper.store(this.pdoc);
		
		assertTrue(test.equals(loadFile(target)));
		target.delete();
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		this.tmpdir.delete();
	}
}
