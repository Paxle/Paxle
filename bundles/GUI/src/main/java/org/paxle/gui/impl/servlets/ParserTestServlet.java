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

package org.paxle.gui.impl.servlets;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.LineIterator;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.paxle.core.IMWComponent;
import org.paxle.core.charset.ICharsetDetector;
import org.paxle.core.doc.ICommand;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.doc.IDocumentFactory;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.doc.ICommand.Result;
import org.paxle.core.doc.IParserDocument.Status;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.core.mimetype.IMimeTypeDetector;
import org.paxle.gui.ALayoutServlet;

@Component(metatype=false, immediate=true)
@Service(Servlet.class)
@Properties({
	@Property(name="org.paxle.servlet.path", value="/testing/parserTest"),
	@Property(name="org.paxle.servlet.doUserAuth", boolValue=true)
})
public class ParserTestServlet extends ALayoutServlet {
	private static final long serialVersionUID = 1L;
	
	public static final String PARAM_PARSE_DOC = "doParseDocument";
	public static final String PARAM_COMMAND_URI = "commandLocation"; 
	public static final String PARAM_CRAWLERDOC_NAME = "crawlerDocumentName";
	public static final String PARAM_ENABLE_MIMETYPE_DETECTION = "mimeTypeDetection";
	public static final String PARAM_ENABLE_CHARSET_DETECTION = "charsetDetection";
		
	private static final String CONTEXT_CMD = "cmd";
	private static final String CONTEXT_PROP_UTIL = "propertyUtil";
	private static final String CONTEXT_ERROR_MSG = "errorMsg";
	private static final String CONTEXT_SERVLET = "servlet";
	private static final String CONTEXT_MIMETYPE_DETECTOR = "mimeTypeDetector";
	private static final String CONTEXT_CHARSET_DETECTOR = "charsetDetector";
	
	private static final String REQ_ATTR_CMD = "cmd";
	private static final String REQ_ATTR_LINEITERS = "pDocReaders";
	
	/**
	 * The parser component.<br/>
	 * This component is required to parse uploaded {@link ICrawlerDocument crawler-documents}
	 */
	@Reference(target="(mwcomponent.ID=org.paxle.parser)")
	protected IMWComponent<ICommand> parser;	
	
	/**
	 * A {@link IDocumentFactory} to create {@link ICommand commands}.
	 */
	@Reference(target="(docType=org.paxle.core.doc.ICommand)")
	protected IDocumentFactory cmdFactory;	
	
	/**
	 * A {@link IDocumentFactory} to create {@link ICrawlerDocument crawler-documents}
	 */
	@Reference(target="(docType=org.paxle.core.doc.ICrawlerDocument)")
	protected IDocumentFactory cDocFactory;		
	
	/**
	 * A component to create temp-files
	 */
	@Reference
	protected ITempFileManager tfm;		
	
	/**
	 * A component to detecte the mime-type of the uploaded file
	 */
	@Reference(cardinality=ReferenceCardinality.OPTIONAL_UNARY)
	protected IMimeTypeDetector mimeTypeDetector;
	
	/**
	 * A component to detect the charset of the uploaded file
	 */
	@Reference(cardinality=ReferenceCardinality.OPTIONAL_UNARY)
	protected ICharsetDetector charsetDetector;
	
	/**
	 * Choosing the template to use 
	 */
	@Override
	protected Template getTemplate(HttpServletRequest request, HttpServletResponse response) {
		return this.getTemplate("/resources/templates/ParserTest.vm");
	}
	
    protected void fillContext(Context context, HttpServletRequest request) {
		String cmdLocation = null;
		String cDocCharset = "UTF-8";
		File cDocContentFile = null;
		String cDocContentType = null; 
		boolean mimeTypeDetection = false;
		boolean charsetDetection = false;
    			
    	try {
    		context.put(CONTEXT_MIMETYPE_DETECTOR, this.mimeTypeDetector);
    		context.put(CONTEXT_CHARSET_DETECTOR, this.charsetDetector);
    		
	    	if (ServletFileUpload.isMultipartContent(request)) {
	    		// Create a factory for disk-based file items
	    		final FileItemFactory factory = new DiskFileItemFactory();
	    		
	    		// Create a new file upload handler
	    		final ServletFileUpload upload = new ServletFileUpload(factory);
	    		
	    		// Parse the request
	    		@SuppressWarnings("unchecked")
	    		final List<FileItem> items = upload.parseRequest(request);
	    		
	    		// Process the uploaded items
	    		final Iterator<FileItem> iter = items.iterator();
	    		while (iter.hasNext()) {
	    			final FileItem item = iter.next();
	    			final String fieldName = item.getFieldName();
	    			
	    			if (item.isFormField()) {
	    				if (fieldName.equals(PARAM_COMMAND_URI)) {
	    					cmdLocation = item.getString();
	    					context.put(PARAM_COMMAND_URI, cmdLocation);
	    				} else if (fieldName.equals(PARAM_ENABLE_MIMETYPE_DETECTION)) {
	    					mimeTypeDetection = true;
	    				} else if (fieldName.equals(PARAM_ENABLE_CHARSET_DETECTION)) {
	    					charsetDetection = true;
	    				}
	    			} else {	    			
		    			try {
			    			// ignore unknown items
			    			if (!fieldName.equals(PARAM_CRAWLERDOC_NAME)) continue;

			    			// getting the content type
			    			cDocContentType = item.getContentType();
			    			
			    			// getting the file-Name
							String fileName = item.getName();
							if (fileName != null) {
								context.put(PARAM_CRAWLERDOC_NAME, fileName);
								fileName = FilenameUtils.getName(fileName);
							} else {
								String errorMsg = String.format("Fileupload field '%s' has no valid filename '%s'.", PARAM_CRAWLERDOC_NAME, item.getFieldName());
								this.logger.warn(errorMsg);
								context.put(CONTEXT_ERROR_MSG,errorMsg);
								continue;
							}
							
							// creating a temp-file
							cDocContentFile = this.tfm.createTempFile();
			    			
							// getting the content
							item.write(cDocContentFile);
		    			} finally {
		    				// delete uploaded item
		    				item.delete();
		    			}
	    			}
	    		}
	    		
	    		// detect the mime-type of the uploaded file
	    		if (this.mimeTypeDetector != null && mimeTypeDetection) {
	    			final String tempMimeType = this.mimeTypeDetector.getMimeType(cDocContentFile);
	    			if (tempMimeType != null) cDocContentType = tempMimeType;
	    		}
	    		
	    		// determine the charset of the uploaded file
	    		if (this.charsetDetector != null && charsetDetection) {
	    			final String tempCharset = this.charsetDetector.detectCharset(cDocContentFile);
	    			if (tempCharset != null) cDocCharset = tempCharset;
	    		}
				
				// creating a crawler-document
				final ICrawlerDocument cDoc = this.cDocFactory.createDocument(ICrawlerDocument.class);
				cDoc.setStatus(ICrawlerDocument.Status.OK);
				cDoc.setContent(cDocContentFile);
				cDoc.setMimeType(cDocContentType);
				cDoc.setCharset(cDocCharset);
										
				// creating a dummy command
				final ICommand cmd = this.cmdFactory.createDocument(ICommand.class);
				cmd.setLocation(URI.create(cmdLocation));
				cmd.setCrawlerDocument(cDoc);
				
				// parsing the command
				this.parser.process(cmd);
				if (cmd.getResult() != Result.Passed) {
					context.put(CONTEXT_ERROR_MSG,String.format(
							"Unable to parse the document: %s",
							cmd.getResultText()
					));
					return;
				}
				
				// trying to get the parsed content
				final IParserDocument pdoc = cmd.getParserDocument();
				if (pdoc == null) {
					context.put("errorMsg","Unable to parse the document: parser-document is null.");
					return;
				} else if (pdoc.getStatus() != Status.OK) {
					context.put(CONTEXT_ERROR_MSG,String.format(
							"Unable to parse the document: %s",
							pdoc.getStatusText()
					));
					return;
				} 
				
				/*
				 * Remembering some object in the request-context
				 * This is required for cleanup. See #requestCleanup(...)
				 */
				request.setAttribute(CONTEXT_CMD, cmd);
				request.setAttribute(REQ_ATTR_LINEITERS, new ArrayList<Reader>());
				
				/*
				 * Passing some properties to the rendering engine
				 */
				context.put(CONTEXT_CMD, cmd);
				context.put(CONTEXT_PROP_UTIL, new PropertyUtils());
				context.put(CONTEXT_SERVLET, this);
				if (mimeTypeDetection && this.mimeTypeDetector != null) {
					context.put(PARAM_ENABLE_MIMETYPE_DETECTION, Boolean.TRUE);
				}
				if (charsetDetection && this.charsetDetector != null) {
					context.put(PARAM_ENABLE_CHARSET_DETECTION, Boolean.TRUE);
				}				
	    	}
    	} catch (Throwable e) {
    		this.logger.error(e);
    		
    		// delete temp-file
			if (cDocContentFile != null) {
				try { this.tfm.releaseTempFile(cDocContentFile); } catch (IOException e2) {/* ignore this */}
			}    		
    	} 
    }	
    
    @Override
    protected void requestCleanup(HttpServletRequest request, HttpServletResponse response, Context context) {
    	super.requestCleanup(request, response, context);
    	
    	// cleanup the command
    	try {
	    	final ICommand cmd = (ICommand) request.getAttribute(REQ_ATTR_CMD);
	    	if (cmd != null) {
    			// cleanup the crawler-doc temp-file
	    		final ICrawlerDocument cDoc = cmd.getCrawlerDocument();
	    		if (cDoc != null) {
	    			final File cDocContentFile = cDoc.getContent();
	    			if (cDocContentFile != null) {
	    				this.tfm.releaseTempFile(cDocContentFile);
	    			}
	    		}
	    		
	    		// cleanup the parser-doc temp-file
	    		final IParserDocument pDoc = cmd.getParserDocument();
	    		if (pDoc != null) {
		    		final File pDocContentFile = pDoc.getTextFile();
		    		if (pDocContentFile != null) {
		    			this.tfm.releaseTempFile(pDocContentFile);
		    		}
	    		}
	    	}
    	} catch (Throwable e) {
    		this.logger.error(e);
    	}     	
    	
    	// cleanup line iterators
    	@SuppressWarnings("unchecked")
    	final List<LineIterator> iterators = (List<LineIterator>) request.getAttribute(REQ_ATTR_LINEITERS);
    	if (iterators != null) {
    		for (LineIterator iter : iterators) {
    			LineIterator.closeQuietly(iter);
    		}
    	}
    }
    
    public boolean isEmptyOrNull(Object obj) {
    	if (obj == null) return true;
    	else if (obj instanceof String && ((String)obj).length() == 0) return true;
    	else if (obj instanceof Collection && ((Collection<?>)obj).size() == 0) return true;
    	else if (obj instanceof Map && ((Map<?,?>)obj).size() == 0) return true;
    	return false;
    }
        
	public LineIterator readLines(HttpServletRequest request, IParserDocument pDoc) throws IOException {
    	if (pDoc == null) return null;
    	
    	// getting the pdoc Reader
    	final Reader pDocReader = pDoc.getTextAsReader();
    	if (pDocReader != null) {	    	
	    	// creating a line iterator
	    	final LineIterator lineIter = new ParserTestLineIterator(pDocReader);
	    
	    	// remembering itarators (required for cleanup)
	    	@SuppressWarnings("unchecked")
	    	final List<LineIterator> iterators = (List<LineIterator>) request.getAttribute(REQ_ATTR_LINEITERS);
	    	iterators.add(lineIter);
	    	
	    	// creating a line iterator
	    	return lineIter;
    	}
    	return null;
    }
	
	class ParserTestLineIterator extends LineIterator {

		public ParserTestLineIterator(Reader reader) throws IllegalArgumentException {
			super(reader);
		}
		
//		@Override
//		public String nextLine() {
//			String line = super.nextLine();
//			return (line==null)?null:line.replaceAll(" ","&nbsp;");
//		}
		
		@Override
		protected boolean isValidLine(String line) {
//			if (line == null) return false;
//			else if (line.trim().length() == 0) return false;			
			return super.isValidLine(line);
		}
	}
}
