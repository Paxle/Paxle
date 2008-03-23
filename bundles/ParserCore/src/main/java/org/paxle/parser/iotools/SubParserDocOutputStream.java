
package org.paxle.parser.iotools;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.charset.ICharsetDetector;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.parser.ParserException;

public class SubParserDocOutputStream extends ParserDocOutputStream {
	
	private final URI location;
	private final IParserDocument pdoc;
	private final Log logger = LogFactory.getLog(SubParserDocOutputStream.class);
	private final String name;
	
	public SubParserDocOutputStream(ITempFileManager tfm, ICharsetDetector cd, IParserDocument pdoc, URI location, String name) throws IOException {
		super(tfm, cd);
		this.location = location;
		this.pdoc = pdoc;
		this.name = URLEncoder.encode(name, "UTF-8");
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		try {
			this.pdoc.addSubDocument(name, super.parse(location));
		} catch (ParserException e) {
			// ignore the sub-document if we cannot parse it
			if (logger.isTraceEnabled()) {
				logger.trace("error parsing sub-document '" + location + "'", e);
			} else {
				logger.info("error parsing sub-document '" + location + "': " + e.getMessage());
			}
			/*
			final IOException ret = new IOException("Error parsing file on close");
			ret.initCause(e);
			throw ret;*/
		}
	}
}
