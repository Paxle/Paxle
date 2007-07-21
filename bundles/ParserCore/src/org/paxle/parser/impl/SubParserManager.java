package org.paxle.parser.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.osgi.framework.ServiceReference;
import org.paxle.parser.ISubParser;
import org.paxle.parser.ISubParserManager;

public class SubParserManager implements ISubParserManager {
	
	/**
	 * A {@link HashMap} containing the protocol that is supported by the sub-crawler as key and
	 * the {@link ServiceReference} as value.
	 */
	private HashMap<String, ISubParser> subParserList = new HashMap<String, ISubParser>();
	
	/**
	 * Adds a newly detected {@link ISubParser} to the {@link Activator#subParserList subparser-list}
	 * @param mimeTypes a list of mimeTypes supported by the sub-parser
	 * @param subParser the newly detected sub-parser
	 */
	public void addSubParser(String mimeTypes, ISubParser subParser) {
		for (String mimeType : mimeTypes.split(";")) {
			this.subParserList.put(mimeType, subParser);					
		}
		System.out.println("Parser for mimetypes '" + mimeTypes + "' was installed.");
	}
	
	/**
	 * Removes a uninstalled {@link ISubParser} from the {@link Activator#subParserList subparser-list}
	 * @param mimeTypes a list of mimeTypes supported by the sub-parser
	 */
	public void removeSubParser(String mimeTypes) {
		for (String mimeType : mimeTypes.split(";")) {			
			this.subParserList.remove(mimeType);					
		}
		System.out.println("Parser for mimetypes '" + mimeTypes + "' was uninstalled.");
	}	
	
	/**
	 * Getting a {@link ISubParser} which is capable to handle the given mime-type
	 * @param mimeType the mime-type of the document which should be parsed
	 * @return a {@link ISubParser} which is capable to parse a document with the given mime-type
	 */
	public ISubParser getSubParser(String mimeType) {
		return this.subParserList.get(mimeType);
	}
	
	/**
	 * Determines if a given mime-type is supported by one of the registered
	 * {@link ISubParser sub-parsers}.
	 * @param mimeType the mime-type
	 * @return <code>true</code> if the given mime-tpye is supported or <code>false</code> otherwise
	 */
	public boolean isSupported(String mimeType) {
		return this.subParserList.containsKey(mimeType);
	}

	/**
	 * @see ISubParserManager#getSubParsers()
	 */
	public Collection<ISubParser> getSubParsers() {
		return new HashSet<ISubParser>(this.subParserList.values());
	}
}
