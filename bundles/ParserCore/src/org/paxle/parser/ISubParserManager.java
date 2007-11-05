package org.paxle.parser;

import java.util.Collection;

public interface ISubParserManager {
	
	public ISubParser getSubParser(String mimeType);
	
	/**
	 * @return an unmodifiable collection of all currently registered {@link ISubParser sub-parsers}
	 */
	public Collection<ISubParser> getSubParsers();
	
	/**
	 * @return an unmodifiable collection of all mime-types supported by the registered {@link ISubParser sub-parsers}
	 */
	public Collection<String> getMimeTypes();
}
