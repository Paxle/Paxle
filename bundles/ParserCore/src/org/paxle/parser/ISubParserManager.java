package org.paxle.parser;

import java.util.Collection;
import java.util.Set;

public interface ISubParserManager {
	
	public ISubParser getSubParser(String mimeType);
	
	/**
	 * @return an unmodifiable collection of all currently registered {@link ISubParser sub-parsers}
	 */
	public Collection<ISubParser> getSubParsers();
	
	/**
	 * @return a list of known but disabled mime-types
	 */
	public Set<String> disabledMimeTypes();
	
	/**
	 * Disable parsing of a given mime-type
	 * @param mimeType the mime-type to disable 
	 */
	public void enableMimeType(String mimeType);
	
	/**
	 * Enables parsing of a given mime-type
	 * @param mimeType the mime-type to enable
	 */
	public void disableMimeType(String mimeType);	
	
	/**
	 * @return an unmodifiable collection of all mime-types supported by the registered {@link ISubParser sub-parsers}
	 */
	public Collection<String> getMimeTypes();
}
