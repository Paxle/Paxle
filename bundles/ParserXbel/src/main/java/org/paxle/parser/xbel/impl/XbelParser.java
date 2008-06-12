package org.paxle.parser.xbel.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.paxle.core.doc.IParserDocument;
import org.paxle.parser.ASubParser;
import org.paxle.parser.CachedParserDocument;
import org.paxle.parser.ISubParser;
import org.paxle.parser.ParserContext;
import org.paxle.parser.ParserException;
import org.paxle.parser.xbel.api.Bookmark;
import org.paxle.parser.xbel.api.Folder;
import org.paxle.parser.xbel.api.Xbel;

public class XbelParser extends ASubParser implements ISubParser {

	private static final String[] MIMETYPES = {
		"application/xbel+xml",
		"application/x-xbel"
	};	

	private final JAXBContext jaxbContext;
	
	public XbelParser() throws JAXBException {
		this.jaxbContext = JAXBContext.newInstance("org.paxle.parser.xbel.api");
	}
	
	/**
	 * @see ISubParser#getMimeTypes()
	 */
	public List<String> getMimeTypes() {
		return Arrays.asList(MIMETYPES);
	}

	@Override
	public IParserDocument parse(URI location, String charset, InputStream is) throws ParserException, UnsupportedEncodingException, IOException {
		
		try {
			// creating an empty parser document 
			final IParserDocument pdoc = new CachedParserDocument(ParserContext.getCurrentContext().getTempFileManager());
			
			// parsing the xbel stream
			Unmarshaller unmarshaller = this.jaxbContext.createUnmarshaller();
			Xbel xbelDoc = (Xbel) unmarshaller.unmarshal(is);
			
			String title = xbelDoc.getTitle();
			if (title != null) pdoc.setTitle(title);
			
			String desc = xbelDoc.getDesc();
			if (desc != null) pdoc.addText(desc + "\r\n");
			
			// loop through the bookmarks/folters
			List<Object> items = xbelDoc.getBookmarkOrFolderOrAliasOrSeparator();
			this.extract(items, pdoc);
			
			pdoc.setStatus(IParserDocument.Status.OK);
			return pdoc;
		} catch (Throwable e) {
			throw new ParserException("Unable to parse xbel document", e);
		}
	}
	
	private void extract(Folder folder, IParserDocument pdoc) throws IOException {
		if (folder == null) return;
		
		String title = folder.getTitle();
		if (title != null) pdoc.addText(title + ":" + "\r\n");
		
		String descr = folder.getDesc();
		if (descr != null) pdoc.addText(descr + "\r\n");
		
		List<Object> items = folder.getBookmarkOrFolderOrAliasOrSeparator();
		this.extract(items, pdoc);
	}
	
	private void extract(Bookmark bm, IParserDocument pdoc) throws IOException {
		if (bm == null) return;
		
		String title = bm.getTitle();
		String url = bm.getHref();
		if (url != null) {
			pdoc.addReference(URI.create(url), title==null?url:title);
		}
		
		String desc = bm.getDesc();
		if (desc != null) pdoc.addText(desc + "\r\n");
	}
	
	private void extract(List<Object> items, IParserDocument pdoc) throws IOException {
		if (items == null) return;

		for (Object item : items) {
			if (item instanceof Folder) {
				this.extract((Folder) item, pdoc);
			} else if (item instanceof Bookmark) {
				this.extract((Bookmark)item, pdoc);
			}
		}

	}
}
