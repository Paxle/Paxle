package org.paxle.parser.pdf.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import org.paxle.core.doc.IParserDocument;
import org.paxle.parser.CachedParserDocument;
import org.paxle.parser.ParserContext;
import org.paxle.parser.ParserException;
import org.paxle.parser.pdf.IPdfParser;
import org.pdfbox.pdfparser.PDFParser;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.PDDocumentInformation;
import org.pdfbox.pdmodel.encryption.StandardDecryptionMaterial;
import org.pdfbox.util.PDFTextStripper;

public class PdfParser implements IPdfParser {

	private static final List<String> MIME_TYPES = Arrays.asList(
			"application/pdf"
	);

	public List<String> getMimeTypes() {
		return MIME_TYPES;
	}

	public IParserDocument parse(String location, String charset, File content) throws ParserException, UnsupportedEncodingException, IOException {
		CachedParserDocument parserDoc = null;
		PDDocument pddDoc = null;
		InputStream fileIn = null;
		try {
			// create an empty document
			parserDoc = new CachedParserDocument(ParserContext.getCurrentContext().getTempFileManager());

			// open file
			fileIn = new BufferedInputStream(new FileInputStream(content));
			
			// parse it
			PDFParser parser = new PDFParser(fileIn);
			parser.parse();
			pddDoc = parser.getPDDocument();
			
			// check document encryption
			if (pddDoc.isEncrypted()) {
				// try to open document with standard pwd
				StandardDecryptionMaterial dm = new StandardDecryptionMaterial("");
				try {
					pddDoc.openProtection(dm);
					if (!pddDoc.getCurrentAccessPermission().canExtractContent()) {
						parserDoc.setStatus(IParserDocument.Status.FAILURE,"PDF Document is encrypted.");
						return parserDoc;
					}
				} catch (Throwable e) {
					parserDoc.setStatus(IParserDocument.Status.FAILURE,"Unable to decrypt document. " + e.getMessage());
					return parserDoc;
				}
			}
			
			// extract metadata
			PDDocumentInformation metadata = pddDoc.getDocumentInformation();
			if (metadata != null) {
				// document title
				String title = metadata.getTitle();
				if (title != null && title.length() > 0) parserDoc.setTitle(title);
				
				// document author(s)
				String author = metadata.getAuthor();
				if (author != null && author.length() > 0) parserDoc.setAuthor(author);;
								
				// subject
				String summary = metadata.getSubject();
				if (summary != null && summary.length() > 0) parserDoc.setSummary(summary);
				
				// keywords
				String keywords = metadata.getKeywords();
				if (keywords != null && keywords.length() > 0) {
					String[] keywordArray = keywords.split("[,;\\s]");
					if (keywordArray != null && keywordArray.length > 0) {
						parserDoc.setKeywords(Arrays.asList(keywordArray));
					}
				}
			}
			
			// init text stripper
			PDFTextStripper stripper = new PDFTextStripper();
			stripper.writeText(pddDoc, parserDoc.getTextWriter());
			parserDoc.close();
			
			parserDoc.setStatus(IParserDocument.Status.OK);
			return parserDoc;
		} catch (Throwable e) {
			throw new ParserException("Error parsing pdf document. " + e.getMessage(), e);
		} finally {
			if (fileIn != null) try { fileIn.close(); } catch (Exception e) {/* ignore this */}
			if (pddDoc != null) try { pddDoc.close(); } catch (Exception e) {/* ignore this */}
		}
	}

}
