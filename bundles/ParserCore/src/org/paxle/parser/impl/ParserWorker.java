package org.paxle.parser.impl;

import org.paxle.core.doc.IParserDocument;
import org.paxle.core.mimetype.IMimeTypeDetector;
import org.paxle.core.queue.ICommand;
import org.paxle.core.threading.AWorker;
import org.paxle.parser.ISubParser;
import org.paxle.parser.ParserContext;

public class ParserWorker extends AWorker<ICommand> {

	/**
	 * A class to manage {@link ISubParser sub-parsers}
	 */
	private SubParserManager subParserManager = null;
	
	/**
	 * A class to detect mimetypes
	 */
	IMimeTypeDetector mimeTypeDetector = null;
	
	public ParserWorker(SubParserManager subParserManager) {
		this.subParserManager = subParserManager;
	}
	
	protected void initParserContext() {
		// init the parser context object
		ParserContext parserContext = new ParserContext(this.subParserManager, this.mimeTypeDetector);
		ParserContext.setCurrentContext(parserContext);		
	}
	
	@Override
	protected void execute(ICommand cmd) {
		// init the parser context
		this.initParserContext();
		
		// get a proper parser
		String mimeType = cmd.getCrawlerDocument().getMimeType();
		ISubParser parser = this.subParserManager.getSubParser(mimeType);
		if (parser == null) {
			// document not parsable
			cmd.setResult(ICommand.Result.Failure, "No parser for MIME type '" + mimeType + "' found");
			return;
		}
		
		try {
			IParserDocument parserdoc = parser.parse(
					cmd.getLocation(), 
					cmd.getCrawlerDocument().getCharset(), 
					cmd.getCrawlerDocument().getContent());
			
			cmd.setParserDocument(parserdoc);
		} catch (Exception e) {
			cmd.setResult(ICommand.Result.Failure, e.getMessage());
			return;
		}
		
		cmd.setResult(ICommand.Result.Passed);
	}
	
	@Override
	protected void reset() {
		// do some cleanup
		ParserContext parserContext = ParserContext.getCurrentContext();
		if (parserContext != null) parserContext.reset();
		
		// reset all from parent
		super.reset();
	}
}
