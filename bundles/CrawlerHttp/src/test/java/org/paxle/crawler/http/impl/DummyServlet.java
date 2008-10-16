package org.paxle.crawler.http.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

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
	
	/**
	 * Directory containing test resources
	 */
	private final File resourcesDir = new File("src/test/resources");
	
	private String testFileName = null;
	private String testFileMimeType = null;
	private String testFileCharset = null;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		ServletContext ctx = config.getServletContext();
		this.testFileName = (String) ctx.getAttribute(ATTR_FILE_NAME);
		this.testFileMimeType = (String) ctx.getAttribute(ATTR_FILE_MIMETYPE);
		this.testFileCharset = (String) ctx.getAttribute(ATTR_FILE_CHARSET);
		
		super.init(config);		
	}
	
	@Override
	protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType(this.testFileMimeType);
		resp.setCharacterEncoding(this.testFileCharset);
		resp.setContentLength((int) new File(this.resourcesDir,this.testFileName).length());
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		File dataFile = new File(this.resourcesDir,this.testFileName);
		
		resp.setContentType(this.testFileMimeType);
		resp.setCharacterEncoding(this.testFileCharset);
		resp.setContentLength((int) dataFile.length());
		
		FileInputStream fin = new FileInputStream(dataFile);
		OutputStream cout = resp.getOutputStream();
		IOTools.copy(fin, cout);
		fin.close();
		cout.close();
	}
}
