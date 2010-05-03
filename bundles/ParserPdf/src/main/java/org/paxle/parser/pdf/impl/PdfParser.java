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

package org.paxle.parser.pdf.impl;

import java.awt.geom.Rectangle2D;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardDecryptionMaterial;
import org.apache.pdfbox.pdmodel.interactive.action.type.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.type.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.PDFTextStripperByArea;
import org.osgi.framework.Constants;
import org.paxle.core.doc.ICommandProfile;
import org.paxle.core.doc.IParserDocument;
import org.paxle.parser.IParserContext;
import org.paxle.parser.ISubParser;
import org.paxle.parser.ParserContext;
import org.paxle.parser.ParserException;

@Component(name=PdfParser.PID,metatype=false)
@Service(ISubParser.class)
@Property(name=ISubParser.PROP_MIMETYPES, value={"application/pdf"})
public class PdfParser implements ISubParser {
	static final String PID = "org.paxle.parser.pdf.impl.PdfParser";
	
	/**
	 * For logging
	 */
	private final Log logger = LogFactory.getLog(this.getClass());	

	public IParserDocument parse(URI location, String charset, InputStream fileIn) throws ParserException, UnsupportedEncodingException, IOException {
		IParserDocument parserDoc = null;
		PDDocument pddDoc = null;
		
		try {
			final IParserContext pc = ParserContext.getCurrentContext();
			final ICommandProfile cmdProfile = pc.getCommandProfile();
			
			// create an empty document			
			parserDoc = pc.createDocument();
			
			// parse it
			final PDFParser parser = new PDFParser(fileIn);
			parser.parse();
			pddDoc = parser.getPDDocument();
			
			// check document encryption
			if (pddDoc.isEncrypted()) {
				if (this.logger.isDebugEnabled()) {
					this.logger.debug(String.format(
						"Document '%s' is encrypted."
					));
				}
				
				// determine the decryption password
				String pwd = "";
				if (cmdProfile != null) {
					String tmp = (String) cmdProfile.getProperty("org.paxle.parser.pdf.impl.decryptionPassword");
					if (tmp != null) pwd = tmp;
				}				
				
				// try to open document with the given password
				try {
					final StandardDecryptionMaterial dm = new StandardDecryptionMaterial(pwd);
					pddDoc.openProtection(dm);
					final AccessPermission accessPermission = pddDoc.getCurrentAccessPermission();
					
					if (accessPermission == null || !accessPermission.canExtractContent()) {
						if (this.logger.isInfoEnabled()) {
							this.logger.debug(String.format(
								"No permission to extract content of document '%s'."
							));						
						}
						parserDoc.setStatus(IParserDocument.Status.FAILURE,"PDF Document is encrypted.");
						return parserDoc;
					}
				} catch (Throwable e) {
					this.logger.error(String.format("Unable to decrypt document '%s'.",location),e);
					parserDoc.setStatus(IParserDocument.Status.FAILURE,String.format("Unable to decrypt document. %s: %s", e.getClass().getName(),e.getMessage()));
					return parserDoc;
				}
			}
			
			// extract metadata
			this.extractMetaData(parserDoc, pddDoc);
			
			// extract text
			final PDFTextStripper stripper = new PDFTextStripper();
			
			// XXX: we could limit the amount of parsed pages via crawling-profile properties?
			// stripper.setStartPage(startPageValue);
			// stripper.setEndPage(endPageValue);
			
			final Writer pdocWriter = parserDoc.getTextWriter();
			stripper.writeText(pddDoc, pdocWriter);
			pdocWriter.flush();
			
			// extracting URIs
			this.extractURLs(parserDoc, pddDoc);
			
			// extracting embedded files
			this.extractEmbeddedFiles(location, parserDoc, pddDoc);
			
			parserDoc.setStatus(IParserDocument.Status.OK);
			return parserDoc;
		} catch (Throwable e) {
			throw new ParserException("Error parsing pdf document. " + e.getMessage(), e);
		} finally {
			if (pddDoc != null) try { pddDoc.close(); } catch (Exception e) {/* ignore this */}
		}
	}
		
	/**
	 * A function to extract metadata from the PDF-document.
	 */
	protected void extractMetaData(IParserDocument parserDoc, PDDocument pddDoc) throws IOException {
		// extract metadata
		final PDDocumentInformation metadata = pddDoc.getDocumentInformation();
		if (metadata == null) return;
		
		// document title
		final String title = metadata.getTitle();
		if (title != null && title.length() > 0) parserDoc.setTitle(title);
		
		// document author(s)
		final String author = metadata.getAuthor();
		if (author != null && author.length() > 0) parserDoc.setAuthor(author);;
		
		// subject
		final String summary = metadata.getSubject();
		if (summary != null && summary.length() > 0) parserDoc.setSummary(summary);
		
		// keywords
		final String keywords = metadata.getKeywords();
		if (keywords != null && keywords.length() > 0) {
			String[] keywordArray = keywords.split("[,;\\s]");
			if (keywordArray != null && keywordArray.length > 0) {
				parserDoc.setKeywords(Arrays.asList(keywordArray));
			}
		}
		
		// last modification date
		final Calendar lastMod = metadata.getModificationDate();
		if (lastMod != null) {
			parserDoc.setLastChanged(lastMod.getTime());
		}
	}
	
	/**
	 * A function to extract embedded URIs from the PDF-document.
	 * 
	 */
	protected void extractURLs(IParserDocument parserDoc, PDDocument pddDoc) throws IOException {
		final PDDocumentCatalog pddDocCatalog = pddDoc.getDocumentCatalog();
		if (pddDocCatalog == null) return;
		
		@SuppressWarnings("unchecked")
        final List<PDPage> allPages = pddDocCatalog.getAllPages();
        if (allPages == null || allPages.isEmpty()) return;
        
        for( int i=0; i<allPages.size(); i++ ) {
            final PDFTextStripperByArea stripper = new PDFTextStripperByArea();
            final PDPage page = (PDPage)allPages.get(i);
            
            @SuppressWarnings("unchecked")
            final List<PDAnnotation> annotations = page.getAnnotations();
            if (annotations == null || annotations.isEmpty()) return;
            
            //first setup text extraction regions
            for( int j=0; j<annotations.size(); j++ ) {
                final PDAnnotation annot = (PDAnnotation)annotations.get(j);
                if( annot instanceof PDAnnotationLink ) {
                    final PDAnnotationLink link = (PDAnnotationLink)annot;
                    final PDRectangle rect = link.getRectangle();
                    
                    //need to reposition link rectangle to match text space
                    float x = rect.getLowerLeftX();
                    float y = rect.getUpperRightY();
                    float width = rect.getWidth();
                    float height = rect.getHeight();
                    int rotation = page.findRotation();
                    if(rotation == 0) {
                        PDRectangle pageSize = page.findMediaBox();
                        y = pageSize.getHeight() - y;
                    } else if( rotation == 90 ) {
                        //do nothing
                    }

                    Rectangle2D.Float awtRect = new Rectangle2D.Float( x,y,width,height );
                    stripper.addRegion("" + j, awtRect );
                }
            }

            stripper.extractRegions( page );

            for( int j=0; j<annotations.size(); j++ ) {
                final PDAnnotation annot = (PDAnnotation)annotations.get( j );
                if( annot instanceof PDAnnotationLink ) {
                    final PDAnnotationLink link = (PDAnnotationLink)annot;
                    final PDAction action = link.getAction();
                    final String urlText = stripper.getTextForRegion("" + j);
                    
                    if(action instanceof PDActionURI) {
                        final PDActionURI embeddedUri = (PDActionURI)action; 
                        final URI temp = URI.create(embeddedUri.getURI());
                        
                        parserDoc.addReference(temp, urlText, Constants.SERVICE_PID + ":" + PID);
                    }
                }
            }
        }		
	}
	
	/**
	 * A function to extract the content of embedded files from a PDF document.
	 */
	protected void extractEmbeddedFiles(URI location, IParserDocument parserDoc, PDDocument pddDoc) throws IOException {
		final PDDocumentCatalog pddDocCatalog = pddDoc.getDocumentCatalog();
		if (pddDocCatalog == null) return;
		
		final PDDocumentNameDictionary nameDic = pddDocCatalog.getNames();
		if (nameDic == null) return;
		
		final PDEmbeddedFilesNameTreeNode embeddedFiles = nameDic.getEmbeddedFiles();
		if (embeddedFiles == null) return;
		
		@SuppressWarnings("unchecked")
		final Map<String,Object> names = embeddedFiles.getNames();
		if (names == null || names.isEmpty()) return;
		
		final IParserContext context = ParserContext.getCurrentContext();
		
		for (Entry<String,Object> name : names.entrySet()) {
			// final String fileDesc = name.getKey();
			final Object fileObj = name.getValue();
			if (fileObj == null) continue;
			
			if (fileObj instanceof PDComplexFileSpecification) {
				final PDComplexFileSpecification embeddedFileSpec = (PDComplexFileSpecification) fileObj;
				final PDEmbeddedFile embeddedFile = embeddedFileSpec.getEmbeddedFile();
				
				// getting the embedded file name and mime-type
				final String fileName = embeddedFileSpec.getFile();
				final String fileMimeType = embeddedFile.getSubtype();
				if (fileMimeType == null) {
					this.logger.warn(String.format(
						"No mime-type specified form embedded file '%s#%s'.",
						location,fileName
					));
					continue;					
				}
				
				// getting a parser to parse the content
				final ISubParser sp = context.getParser(fileMimeType);
				if (sp == null) {
					this.logger.warn(String.format(
						"No parser found to parse embedded file '%s#%s' with type '%s'.",
						location, fileName, fileMimeType
					));
					continue;
				}
				
				// parsing content
				InputStream embeddedFileStream = null;
				try {
					embeddedFileStream = embeddedFile.createInputStream();
					final IParserDocument subParserDoc = sp.parse(location, "UTF-8", embeddedFileStream);
					if (subParserDoc.getMimeType() == null) {
						subParserDoc.setMimeType(fileMimeType);
					}
					
					parserDoc.addSubDocument(fileName, subParserDoc);
				} catch (ParserException e) {
					this.logger.error(String.format(
						"Unexpected error while parsing parse embedded file '%s#%s' with type '%s': %s",
						location, fileName, fileMimeType, e.getMessage()
					));
				} finally {
					if (embeddedFileStream != null) try { embeddedFileStream.close(); } catch (Exception e) {/* ignore this */}
				}
			}
		}
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
	
}
