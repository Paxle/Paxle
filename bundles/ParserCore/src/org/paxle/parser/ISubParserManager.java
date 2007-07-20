package org.paxle.parser;

import java.util.Collection;

public interface ISubParserManager {
	
	/**
	 * @return a collection of all currently registered sub-parsers
	 */
	public Collection<ISubParser> getSubParsers();
}
