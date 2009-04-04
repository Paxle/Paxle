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
package org.paxle.filter.webgraph.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LRUMap;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;

public class PrefuseServletTest extends MockObjectTestCase {
	private static final String TESTDIR_NAME = "target/testDir";
	
	File testDir;
	LRUMap dataMap;
	HttpServletResponse resp;
	HttpServletRequest req;
	PrefuseServlet servlet;
	
	@SuppressWarnings("serial")
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// creating dummy data
		dataMap = new LRUMap();
		dataMap.put("navi.paxle.net", new HashSet<String>(Arrays.asList(new String[]{
				"www2.paxle.net",
				"svn.paxle.net",
				"wiki.paxle.net",
				"forum.paxle.info",
				"paste.paxle.de",
				"bugs.pxl.li"
		})));
		dataMap.put("wiki.paxle.net", new HashSet<String>(Arrays.asList(new String[]{
				"svn.paxle.net",
				"forum.paxle.info"
		})));		
		dataMap.put("bugs.pxl.li", new HashSet<String>(Arrays.asList(new String[]{
				"paxle.net"
		})));
		
		// creating test dir
		this.testDir = new File(TESTDIR_NAME);
		this.testDir.mkdir();		
		
		// creating a dummy response
		resp = mock(HttpServletResponse.class);
		req = mock(HttpServletRequest.class);
		
		// creating servlet
		servlet = new PrefuseServlet(){
			@Override
			protected LRUMap getRelations() {
				return dataMap;
			}
		};
	}

	public void testGenerateGraphImage() throws IOException, ServletException {
		final File testFile = new File(testDir,"test.png");
		if (testFile.exists()) assertTrue(testFile.delete());

		assertTrue(testFile.createNewFile());
		final FileOutputStream fileOut = new FileOutputStream(testFile);		
		checking(new Expectations(){{
			allowing(resp).setContentType("image/png");
			one(resp).getOutputStream(); will(returnValue(new ServletOutputStream(){
				@Override
				public void write(int b) throws IOException {
					fileOut.write(b);					
				}				
			}));
			
			allowing(req).getParameter("view"); will(returnValue("graph"));
		}});
		
		this.servlet.doGet(this.req, this.resp);
		
		fileOut.flush();
		fileOut.close();
	}
	
	public void testGenerateGraphML() throws IOException, ServletException {
		final File testFile = new File(testDir,"test.xml");
		if (testFile.exists()) assertTrue(testFile.delete());

		assertTrue(testFile.createNewFile());
		final FileOutputStream fileOut = new FileOutputStream(testFile);		
		checking(new Expectations(){{
			allowing(resp).setContentType("text/xml");
			one(resp).getOutputStream(); will(returnValue(new ServletOutputStream(){
				@Override
				public void write(int b) throws IOException {
					fileOut.write(b);					
				}				
			}));
			
			allowing(req).getParameter("view"); will(returnValue("graphML"));
		}});
		
		this.servlet.doGet(this.req, this.resp);
		
		fileOut.flush();
		fileOut.close();
	}	
}
