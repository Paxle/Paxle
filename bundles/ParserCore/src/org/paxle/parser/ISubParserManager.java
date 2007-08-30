package org.paxle.parser;

import java.util.Collection;

public interface ISubParserManager {
	
	public ISubParser getSubParser(String mimeType);
	
	/**
	 * @return a collection of all currently registered sub-parsers
	 */
	public Collection<ISubParser> getSubParsers();
	
	/**
	 * @return a collection of mime-types supported by the registered
	 * 		   sub-parsers.
	 */
	public Collection<String> getMimeTypes();
}
