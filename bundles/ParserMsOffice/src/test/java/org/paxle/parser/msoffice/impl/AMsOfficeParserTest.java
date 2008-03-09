package org.paxle.parser.msoffice.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.paxle.core.io.temp.ITempDir;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.parser.ParserContext;

import junit.framework.TestCase;

public class AMsOfficeParserTest extends TestCase {
	protected ParserContext parserContext = null;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		

		// create a parser context with a dummy temp-file-manager
		this.parserContext = new ParserContext(null,null,null, new ITempFileManager() {		
			public void setTempDirFor(ITempDir arg0, String... arg1) { }		
			public void removeTempDirFor(String... arg0) { }

			public void releaseTempFile(File arg0) throws FileNotFoundException, IOException {
				if (arg0 != null) arg0.delete();
			}

			public File createTempFile() throws IOException {
				File tempfile = File.createTempFile("mswordParser", ".doc");
				tempfile.deleteOnExit();
				return tempfile;
			}

		});
		ParserContext.setCurrentContext(this.parserContext);		
	}
}
