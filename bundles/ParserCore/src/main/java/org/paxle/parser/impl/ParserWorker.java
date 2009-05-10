/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package org.paxle.parser.impl;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.doc.ICrawlerDocument.Status;
import org.paxle.core.queue.ICommand;
import org.paxle.core.queue.ICommand.Result;
import org.paxle.core.threading.AWorker;
import org.paxle.parser.IParserContext;
import org.paxle.parser.ISubParser;
import org.paxle.parser.ISubParserManager;
import org.paxle.parser.ParserContext;
import org.paxle.parser.ParserException;

public class ParserWorker extends AWorker<ICommand> {

	/**
	 * A class to manage {@link ISubParser sub-parsers}
	 */
	private ISubParserManager subParserManager = null;
	
	/**
	 * A logger class 
	 */
	private final Log logger = LogFactory.getLog(ParserWorker.class);
	
	public ParserWorker(ISubParserManager subParserManager) {
		this.subParserManager = subParserManager;
	}
	
	private void setStatusAndLogWarning(ICommand command, Result result, String errorMessage) {
		this.logger.warn(String.format(
				"Won't parse resource '%s'. " + errorMessage,
				command.getLocation()
		));
		
		if (command.getResult() == Result.Passed) {
			command.setResult(result, errorMessage);
		}
	}
	
	@Override
	protected void execute(ICommand command) {
		final long start = System.currentTimeMillis();
		
		IParserDocument parserDoc = null;
		ICrawlerDocument crawlerDocument = null;
		File crawlerDocContent = null;
		String mimeType = null;
		
		try {
			
			/* ================================================================
			 * Input Parameter Check
			 * ================================================================ */
			
			// check command status			
			if (command.getResult() != ICommand.Result.Passed) {
				this.setStatusAndLogWarning(command, Result.Rejected, String.format("Command status is: '%s' (%s)",
						command.getResult(),
						command.getResultText()
				));
				return;
			} 
			
			// check crawlerDocument status
			crawlerDocument = command.getCrawlerDocument();
			if (crawlerDocument == null) {
				this.setStatusAndLogWarning(command, Result.Rejected, "Crawler-document is null.");
				return;
			} else if (crawlerDocument.getStatus() != ICrawlerDocument.Status.OK) {
				this.setStatusAndLogWarning(command, Result.Rejected, String.format("Crawler-document status is: '%s' (%s)",
						crawlerDocument.getStatus(),
						crawlerDocument.getStatusText()
				));
				return;
			} 

			// check crawlerDocument content
			crawlerDocContent = crawlerDocument.getContent();
			if (crawlerDocContent == null) {
				this.setStatusAndLogWarning(command, Result.Rejected, "Crawler-document content is null.");
				return;
			} else if (!crawlerDocContent.exists()) {
				this.setStatusAndLogWarning(command, Result.Rejected, "Crawler-document file not found.");
				return;
			} else if (!crawlerDocContent.canRead()) {
				this.setStatusAndLogWarning(command, Result.Rejected, "Crawler-document file can not be read.");
				return;
			} else if (crawlerDocContent.length() == 0) {
				this.setStatusAndLogWarning(command, Result.Rejected, "Crawler-document file is empty.");
				return;
			}
			
			// check crawlerDoc mimetype
			mimeType = crawlerDocument.getMimeType();
			if (mimeType == null) {
				this.setStatusAndLogWarning(command, Result.Rejected, "No mime-type was specified.");
				return;			
			}
			
			/* ================================================================
			 * Parse Resource
			 * 
			 * a) determine content mime-type
			 * b) fetch appropriate parser
			 * c) parse resource
			 * d) process parser response
			 * ================================================================ */			

			// get appropriate parser
			this.logger.debug(String.format("Getting parsers for mime-type '%s' ...", mimeType));
			final Collection<ISubParser> parsers = this.subParserManager.getSubParsers(mimeType);
			
			if (parsers == null || parsers.size() == 0) {
				// document not parsable
				this.setStatusAndLogWarning(command, Result.Rejected, String.format("No parser for mime-type '%s' found.",
						mimeType
				));
				return;
			}
			this.logger.debug(String.format(
					"%d parser(s) found for mime-type '%s': %s",
					Integer.valueOf(parsers.size()),
					mimeType,
					parsers
			));
			
			// adding some properties into the parser-context
			IParserContext pc = ParserContext.getCurrentContext();
			pc.setProperty("cmd", command);
			pc.setProperty("cmd.profileOID",Integer.valueOf(command.getProfileOID()));
			
			// parse resource
			final Iterator<ISubParser> it = parsers.iterator();
			ISubParser parser;
			do {
				parser = it.next();
				this.logger.debug(String.format("Parsing resource '%s' (%s) with parser '%s' ...",
						command.getLocation(), mimeType, parser.getClass().getName()));
				try {
					// parsing the document using the next available parser
					parserDoc = parser.parse(
							command.getLocation(), 
							crawlerDocument.getCharset(), 
							crawlerDocContent
					);
					
					if (parserDoc == null || parserDoc.getStatus() == null || parserDoc.getStatus() != IParserDocument.Status.OK) {
						logger.info(String.format(
								"Unknown error parsing '%s' (%s) with parser '%s'",
								command.getLocation(), 
								mimeType, 
								parser.getClass().getName()
						));
						continue;
					}
					break;
				} catch (ParserException e) {
					final String msg = String.format(
							"Error parsing '%s' (%s) with parser '%s': %s",
							command.getLocation(), 
							mimeType, 
							parser.getClass().getName(), 
							e.getMessage()
					);
					if (logger.isDebugEnabled()) {
						logger.warn(msg, e);
					} else {
						logger.warn(msg);
					}
					
					if (!it.hasNext()) {
						parserDoc = pc.createDocument();
						parserDoc.setStatus(IParserDocument.Status.FAILURE, e.getMessage());
					}
				}
			} while (it.hasNext());
			
			/* ================================================================
			 * Process parser response
			 * ================================================================ */			
			
			if (parserDoc == null) {
				command.setResult(
						ICommand.Result.Failure, 
						String.format("Parser '%s' returned no parser-document.",parser.getClass().getName())
				);
				return;
			} else if (parserDoc.getStatus() == null || parserDoc.getStatus() != IParserDocument.Status.OK) {
				this.setStatusAndLogWarning(command, Result.Failure, String.format("Parser-document status is: '%s' (%s)",
						parserDoc.getStatus(),
						parserDoc.getStatusText()
				));
				return;
			}
			
			// setting of default properties
			if (parserDoc.getMimeType() == null) {
				parserDoc.setMimeType(mimeType);
			}
			
			// setting command status to passed
			command.setResult(ICommand.Result.Passed,null);
			
		} catch (Throwable e) {
			// setting command status
			command.setResult(
					ICommand.Result.Failure, 
					String.format("Unexpected '%s' while parsing resource: %s",e.getClass().getName(),e.getMessage())
			);
			
			// log error
			this.logger.warn(String.format(
					"Unexpected '%s' while parsing resource '%s'.",
					e.getClass().getName(),
					command.getLocation()
			),e);			
		} finally {
			/* 
			 * Append parser-doc to command object.
			 * 
			 * This must be done even in error situations to 
			 * - allow filters to correct the error (if possible)
			 * - to report the error back properly (e.g. to store it into db
			 *   or send it back to a remote peer). 
			 */
			if (parserDoc != null) {
				command.setParserDocument(parserDoc);
			}
			
			if (logger.isInfoEnabled()) {
				// some crawler-doc properties
				final ICrawlerDocument crawlerDoc = command.getCrawlerDocument();
				String cDocStatus = "unknown";
				String cDocStatusText = "";				
				String cDocMimeType = "unknown"; 
				Long cDocSize = Long.valueOf(-1);
				
				if (crawlerDoc != null) {
					cDocStatus = crawlerDoc.getStatus().toString();
					cDocStatusText = crawlerDoc.getStatusText()==null?"":crawlerDoc.getStatusText();
					
					if (crawlerDoc.getStatus() == Status.OK) {
						cDocMimeType = crawlerDoc.getMimeType();
						cDocSize = Long.valueOf(crawlerDoc.getContent() == null ? -1 : crawlerDoc.getContent().length() >> 10);
					}
				}
				
				
				// sompe parser-doc properties
				final Long pDocProcessingTime = Long.valueOf(System.currentTimeMillis() - start);
				String pDocStatus = "unknown";
				String pDocStatusText = "";
				Charset pDocCharset = null;
				
				if (parserDoc != null) {
					pDocCharset = parserDoc.getCharset();
					pDocStatus = parserDoc.getStatus().toString();
					pDocStatusText = parserDoc.getStatusText()==null?"":parserDoc.getStatusText();
				}
				
				// building log message
				StringBuilder logMsg = new StringBuilder();
				
				// general data
				logMsg.append(String.format(
							"Finished parsing of resource '%s' [mime-type: '%s', charset: '%s', size: %,d KB%s%s] in %d ms.\r\n",
							command.getLocation(),
							cDocMimeType,
							pDocCharset,
							cDocSize,
							(parserDoc != null && (parserDoc.getFlags() & IParserDocument.FLAG_NOINDEX) != 0) ? ", noindex" : "",
							(parserDoc != null && (parserDoc.getFlags() & IParserDocument.FLAG_NOFOLLOW) != 0) ? ", nofollow" : "",
							pDocProcessingTime
				));
				
				// command-status
				if (logger.isDebugEnabled() || command.getResult() != Result.Passed) {
					logMsg.append(String.format(
							"\tCommand-Status: '%s' %s\r\n",
							command.getResult(),
							command.getResultText()								
					));
				}
								
				// crawler-doc status
				if (logger.isDebugEnabled() || command.getResult() != Result.Passed) {
					logMsg.append(String.format(
							"\tCrawler-Status: '%s' %s\r\n",
							cDocStatus,
							cDocStatusText								
					));
				}
				
				// parser-doc status
				logMsg.append(String.format(
							"\tParser-Status:  '%s' %s",
							pDocStatus,
							pDocStatusText
				));
				
				logger.info(logMsg.toString());
			}
		}
	}
	
	@Override
	protected void reset() {
		// do some cleanup
		ParserContext.removeCurrentContext();
		
		// reset all from parent
		super.reset();
	}
}
