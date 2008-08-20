package org.paxle.parser;

import java.util.Collection;
import java.util.Set;

public interface ISubParserManager {
		
	/**
	 * @return an unmodifiable collection of all currently registered {@link ISubParser sub-parsers}
	 */
	public Collection<ISubParser> getSubParsers();
	
	/**
	 * @param mimeType the mimetype
	 * @return a {@link ISubParser parser) supporting the given mimetype.
	 */	
	public ISubParser getSubParser(String mimeType);	
	
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
	
	/**
	 * Determines if a given mimeType is supported by one of the registered
	 * {@link ISubParser sub-parser}.
	 * @param mimeType the mime-type
	 * @return <code>true</code> if the given mimetype is supported or <code>false</code> otherwise
	 */	
	public boolean isSupported(String mimeType);
}
