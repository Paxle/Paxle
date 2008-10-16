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

import org.paxle.core.io.IOTools;

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
	
	private void setResponseHeaders(HttpServletResponse resp) {
		if (this.testFileMimeType != null) {
			resp.setContentType(this.testFileMimeType);
		}
		if (this.testFileCharset != null) {
			resp.setCharacterEncoding(this.testFileCharset);
		}		
		if (this.testFileName == null) {
			if (this.testFileSize == null) {
				resp.setContentLength(0);
			} else {
				resp.setContentLength(this.testFileSize.intValue());
			}
		} else {
			resp.setContentLength((int) new File(this.resourcesDir,this.testFileName).length());
		}
		if (this.serverStatusCode != null) {
			resp.setStatus(this.serverStatusCode.intValue());
		}
	}
	
	@Override
	protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.setResponseHeaders(resp);
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.setResponseHeaders(resp);
		File dataFile = new File(this.resourcesDir,this.testFileName);
		
		OutputStream cout = resp.getOutputStream();
		if (this.testFileName != null) {
			FileInputStream fin = new FileInputStream(dataFile);			
			IOTools.copy(fin, cout);
			fin.close();			
		} else {
			byte[] temp = new byte[256];
			Random rand = new Random();
			int counter = this.testFileSize.intValue();
			while (counter > 0) {
				counter -= Math.min(counter, temp.length);
				rand.nextBytes(temp);
				cout.write(temp, 0, counter);
				cout.flush();
			}
		}
		cout.close();
	}
}
