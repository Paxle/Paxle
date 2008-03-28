
package org.paxle.parser.msoffice.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.paxle.core.doc.IParserDocument;
import org.paxle.parser.CachedParserDocument;
import org.paxle.parser.ISubParser;
import org.paxle.parser.ParserContext;
import org.paxle.parser.ParserException;

public abstract class AMsOfficeParser implements ISubParser {
	
	private Log logger = LogFactory.getLog(this.getClass());
	private final String docType;
	private final List<String> mimeTypes;
	
	protected AMsOfficeParser(final String docType, final String... mimeTypes) {
		this.docType = docType;
		this.mimeTypes = Arrays.asList(mimeTypes);
	}
	
	public List<String> getMimeTypes() {
		return mimeTypes;
	}
	
	public IParserDocument parse(URI location, String charset, File content) throws ParserException, UnsupportedEncodingException, IOException {
		InputStream fileIn = null;
		try {		
			// open file			
			fileIn = new BufferedInputStream(new FileInputStream(content));			
			return parse(location, charset, fileIn);
		} finally {
			if (fileIn != null) try { fileIn.close(); } catch (Exception e) {/* ignore this */}
		}
	}
	
	public IParserDocument parse(URI location, String charset, InputStream is)
			throws ParserException, UnsupportedEncodingException, IOException {
		CachedParserDocument parserDoc = null;
		try {
			// create an empty document
			parserDoc = new CachedParserDocument(ParserContext.getCurrentContext().getTempFileManager());
			
			// open the POI filesystem
			POIFSFileSystem fs = new POIFSFileSystem(is);
			
			// extract metadata
			this.extractMetadata(fs, parserDoc);
			
			// extract plain text
			extractText(fs, parserDoc);
			
			parserDoc.setStatus(IParserDocument.Status.OK);
			return parserDoc;
		} catch (Throwable e) {
			throw new ParserException(String.format("Error parsing ms-%s document. %s: %s",
					docType,
					e.getClass().getName(),
					e.getMessage()), e);
		}
	}
	
	protected abstract void extractText(final POIFSFileSystem fs, final IParserDocument parserDoc) throws ParserException, IOException;
	
	protected void extractMetadata(POIFSFileSystem fs, IParserDocument parserDoc) throws ParserException {
		DocumentInputStream docIn = null;
		try {
			// read the summary info
			DirectoryEntry dir = fs.getRoot();
			DocumentEntry siEntry = (DocumentEntry) dir.getEntry(SummaryInformation.DEFAULT_STREAM_NAME);
			docIn = new DocumentInputStream(siEntry);
			
			// get properties
			PropertySet props = new PropertySet(docIn);
			docIn.close();
			
			// extract info
			SummaryInformation summary = new SummaryInformation(props);
			
			// doc title
			String title = summary.getTitle();
			if (title != null && title.length() > 0) {
				parserDoc.setTitle(title);
				this.logger.debug(String.format("Document title is: %s",title));
			}			
			
			// doc author
			String author = summary.getAuthor();
			if (author != null && author.length() > 0) {
				parserDoc.setAuthor(author);
				this.logger.debug(String.format("Document author is: %s",author));
			}
			
			// subject
			String subject = summary.getSubject();
			if (subject != null && subject.length() > 0) {
				parserDoc.setSummary(subject);
				this.logger.debug(String.format("Document summary is: %s",subject));
			}
			
			// doc keywords
			String keywords = summary.getKeywords();
			if (keywords != null && keywords.length() > 0) {
				String[] keywordArray = keywords.split("[,;\\s]");
				if (keywordArray != null && keywordArray.length > 0) {
					ArrayList<String> keywordsList = new ArrayList<String>(keywordArray.length);
					for (String keyword :keywordArray) {
						keyword = keyword.trim();
						if (keyword.length() > 0) {
							keywordsList.add(keyword);
						}
					}					
					parserDoc.setKeywords(keywordsList);
					this.logger.debug(String.format("Document keywords are: %s",keywordsList.toString()));
				}
			}
			
			// last modification date
			if (summary.getEditTime() > 0) {
				Date editTime = new Date(summary.getEditTime());
				parserDoc.setLastChanged(editTime);
				this.logger.debug(String.format("Document last-changed-date is: %s",editTime.toString()));
			} else if (summary.getCreateDateTime() != null) {
				Date creationDate = summary.getCreateDateTime();
				parserDoc.setLastChanged(creationDate);
				this.logger.debug(String.format("Document creation-date is: %s",creationDate.toString()));
			} else if (summary.getLastSaveDateTime() != null) {
				Date lastSaveDate = summary.getLastSaveDateTime();
				parserDoc.setLastChanged(lastSaveDate);
				this.logger.debug(String.format("Document last-save-date is: %s",lastSaveDate.toString()));
			}
			
		} catch (Exception e) {
			String errorMsg = String.format(
					"Unexpected '%s' while extracting metadata: %s",
					e.getClass().getName(),
					e.getMessage());
			logger.error(errorMsg,e);
			throw new ParserException(errorMsg);
		} finally {
			if (docIn != null) try { docIn.close(); } catch (Exception e) {/* ignore this */}
		}
	}
}
