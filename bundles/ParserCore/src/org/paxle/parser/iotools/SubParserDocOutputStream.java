package org.paxle.parser.iotools;

import java.io.IOException;

import org.paxle.core.doc.IParserDocument;
import org.paxle.parser.ParserException;

public class SubParserDocOutputStream extends ParserDocOutputStream {
	
	private final String location;
	private final IParserDocument pdoc;
	
	public SubParserDocOutputStream(IParserDocument pdoc, String location) throws IOException {
		this.location = location;
		this.pdoc = pdoc;
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		try {
			this.pdoc.addSubDocument(this.location, super.parse(this.location));
		} catch (ParserException e) {
			throw new IOException("Error parsing file on close", e);
		}
	}
}
