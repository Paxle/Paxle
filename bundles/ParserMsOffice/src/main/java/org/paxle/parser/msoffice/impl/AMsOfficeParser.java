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

package org.paxle.parser.msoffice.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.paxle.core.doc.IParserDocument;
import org.paxle.parser.IParserContextLocal;
import org.paxle.parser.ISubParser;
import org.paxle.parser.ParserException;

@Component(componentAbstract=true)
public abstract class AMsOfficeParser implements ISubParser {
	
	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());
	
	@Reference
	protected IParserContextLocal contextLocal;
	
	/**
	 * The type of the parser, e.g. "excel"
	 */
	private final String docType;
	
	protected AMsOfficeParser(final String docType) {
		this.docType = docType;
	}
	
	public IParserDocument parse(URI location, String charset, File content) throws ParserException, UnsupportedEncodingException, IOException {
		InputStream fileIn = null;
		try {		
			// open file			
			fileIn = new BufferedInputStream(new FileInputStream(content));			
			return parse(location, charset, fileIn);
		} finally {
			if (fileIn != null) try { fileIn.close(); } catch (Exception e) {this.logger.error(e);}
		}
	}
	
	public IParserDocument parse(URI location, String charset, InputStream is)
			throws ParserException, UnsupportedEncodingException, IOException {
		IParserDocument parserDoc = null;
		try {
			// create an empty document
			parserDoc = this.contextLocal.getCurrentContext().createDocument();
			
			// open the POI filesystem
			final POIFSFileSystem fs = new POIFSFileSystem(is);
			
			// extract metadata
			this.extractMetadata(fs, parserDoc);
			
			// extract plain text
			this.extractText(fs, parserDoc);
			
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
