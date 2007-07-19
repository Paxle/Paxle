package org.paxle.parser.impl;

import org.paxle.core.queue.ICommand;
import org.paxle.core.threading.AWorker;
import org.paxle.parser.ISubParser;

public class ParserWorker extends AWorker {

	/**
	 * A class to manage {@link ISubParser sub-parsers}
	 */
	private SubParserManager subParserManager = null;
	
	public ParserWorker(SubParserManager subParserManager) {
		this.subParserManager = subParserManager;
	}
	
	@Override
	protected void execute(ICommand cmd) {
		// TODO Auto-generated method stub
		
		// get a proper parser
		String mimeType = null;
		ISubParser parser = this.subParserManager.getSubParser(mimeType);
		if (parser == null) {
			// document not parsable
			// TODO: set an errorstatus in the command object
			return;
		}

	}
}
