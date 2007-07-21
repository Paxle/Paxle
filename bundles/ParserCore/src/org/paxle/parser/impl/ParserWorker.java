package org.paxle.parser.impl;

import org.paxle.core.queue.ICommand;
import org.paxle.core.threading.AWorker;
import org.paxle.parser.ISubParser;
import org.paxle.parser.ParserContext;
import org.paxle.parser.ParserException;

public class ParserWorker extends AWorker {

	/**
	 * A class to manage {@link ISubParser sub-parsers}
	 */
	private SubParserManager subParserManager = null;
	
	public ParserWorker(SubParserManager subParserManager) {
		this.subParserManager = subParserManager;
	}
	
	protected void initParserContext() {
		// init the parser context object
		ParserContext parserContext = new ParserContext(subParserManager);
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
			// TODO: set an errorstatus in the command object
			return;
		}
		
		try {
			parser.parse(cmd.getLocation(), 
						 cmd.getCrawlerDocument().getCharset(), 
						 cmd.getCrawlerDocument().getContent());
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
