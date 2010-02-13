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

package org.paxle.crawler.http.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.paxle.core.io.IIOTools;

public class DummyServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public static final String ATTR_FILE_NAME = "testfileName"; 
	public static final String ATTR_FILE_MIMETYPE = "testfileMimeType";
	public static final String ATTR_FILE_CHARSET = "testfileCharser"; 
	public static final String ATTR_FILE_SIZE = "testfileSize";
	public static final String ATTR_STATUS_CODE = "serverStatus";
	
	/**
	 * Directory containing test resources
	 */
	private final File resourcesDir = new File("src/test/resources");
	
	private Integer testFileSize = null;
	private String testFileName = null;
	private String testFileMimeType = null;
	private String testFileCharset = null;	
	private Integer serverStatusCode = null;
	
	private IIOTools ioTools = new org.paxle.core.io.impl.IOTools();
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		ServletContext ctx = config.getServletContext();
		this.testFileSize = (Integer) ctx.getAttribute(ATTR_FILE_SIZE);
		this.testFileName = (String) ctx.getAttribute(ATTR_FILE_NAME);
		this.testFileMimeType = (String) ctx.getAttribute(ATTR_FILE_MIMETYPE);
		this.testFileCharset = (String) ctx.getAttribute(ATTR_FILE_CHARSET);
		this.serverStatusCode = (Integer) ctx.getAttribute(ATTR_STATUS_CODE);
		
		super.init(config);		
	}
	
	private void setResponseHeaders(HttpServletRequest req, HttpServletResponse resp) {
		if (this.testFileMimeType != null) {
			resp.setContentType(this.testFileMimeType);
		}
		if (this.testFileCharset != null) {
			resp.setCharacterEncoding(this.testFileCharset);
		}		
		if (this.testFileSize != null && this.testFileSize.intValue() > 0){
			resp.setContentLength(this.testFileSize.intValue());
		} else if (req.getProtocol().equalsIgnoreCase("HTTP/1.1")) {
			resp.setHeader("Transfer-Encoding", "chunked");
		}
		
		if (this.serverStatusCode != null) {
			resp.setStatus(this.serverStatusCode.intValue());
		}
	}
	
	@Override
	protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.setResponseHeaders(req, resp);
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.setResponseHeaders(req, resp);		
		
		OutputStream cout = resp.getOutputStream();
		if (this.testFileName != null) {
			File dataFile = new File(this.resourcesDir,this.testFileName);
			FileInputStream fin = new FileInputStream(dataFile);			
			this.ioTools.copy(fin, cout);
			fin.close();			
		} else {
			byte[] temp = new byte[256];
			Random rand = new Random();
			int counter = Math.abs(this.testFileSize.intValue());
			while (counter > 0) {
				int len  = Math.min(counter, temp.length);
				counter -= len;
				
				rand.nextBytes(temp);
				cout.write(temp, 0, len);
				cout.flush();
			}
		}
//		cout.close();
	}
}
