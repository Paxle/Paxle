package org.paxle.parser.msoffice.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.paxle.parser.CachedParserDocument;
import org.paxle.parser.ParserException;

public class AMsOfficeParser {
	private Log logger = LogFactory.getLog(this.getClass());
	
	protected void extractMetadata(POIFSFileSystem fs, CachedParserDocument parserDoc) throws ParserException {
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
