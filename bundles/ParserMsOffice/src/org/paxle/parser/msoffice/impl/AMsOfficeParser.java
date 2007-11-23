package org.paxle.parser.msoffice.impl;

import java.util.Arrays;
import java.util.Date;

import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.paxle.parser.CachedParserDocument;

public class AMsOfficeParser {
	protected void extractMetadata(POIFSFileSystem fs, CachedParserDocument parserDoc) {
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
			}			
			
			// doc author
			String author = summary.getAuthor();
			if (author != null && author.length() > 0) {
				parserDoc.setAuthor(author);
			}
			
			// subject
			String subject = summary.getSubject();
			if (subject != null && subject.length() > 0) {
				parserDoc.setSummary(subject);
			}
			
			// doc keywords
			String keywords = summary.getKeywords();
			if (keywords != null && keywords.length() > 0) {
				String[] keywordArray = keywords.split("[,;\\s]");
				if (keywordArray != null && keywordArray.length > 0) {
					parserDoc.setKeywords(Arrays.asList(keywordArray));
				}
			}
			
			// last modification date
			if (summary.getEditTime() > 0) {
				parserDoc.setLastChanged(new Date(summary.getEditTime()));
			} else if (summary.getCreateDateTime() != null) {
				parserDoc.setLastChanged(summary.getCreateDateTime());
			} else if (summary.getLastSaveDateTime() != null) {
				parserDoc.setLastChanged(summary.getLastSaveDateTime());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (docIn != null) try { docIn.close(); } catch (Exception e) {/* ignore this */}
		}
	}
}
