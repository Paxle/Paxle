package org.paxle.parser.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.osgi.framework.ServiceReference;
import org.paxle.core.prefs.Properties;
import org.paxle.parser.ISubParser;
import org.paxle.parser.ISubParserManager;

public class SubParserManager implements ISubParserManager {
	private static final String DISABLED_MIMETYPES = ISubParserManager.class.getName() + "." + "disabledMimeTypes";
	
	/**
	 * A {@link HashMap} containing the mime-types that is supported by the sub-parser as key and
	 * the {@link ServiceReference} as value.
	 */
	private HashMap<String, ISubParser> subParserList = new HashMap<String, ISubParser>();
	
	/**
	 * A list of disabled mime-types
	 */
	private Set<String> disabledMimeTypes = new HashSet<String>();	
	
	/**
	 * The properties of this component
	 */
	private Properties props = null;	
	
	public SubParserManager(Properties props) {
		this.props = props;
		if (this.props != null && this.props.containsKey(DISABLED_MIMETYPES)) {
			this.disabledMimeTypes = this.props.getSet(DISABLED_MIMETYPES);
		}
	}
	
	/**
	 * Adds a newly detected {@link ISubParser} to the {@link Activator#subParserList subparser-list}
	 * @param mimeTypes a list of mimeTypes supported by the sub-parser
	 * @param subParser the newly detected sub-parser
	 */
	public void addSubParser(String mimeTypes, ISubParser subParser) {
		this.addSubParser(mimeTypes.split(";|,"), subParser);
	}
	
	public void addSubParser(String[] mimeTypes, ISubParser subParser) {
		for (String mimeType : mimeTypes) {
			this.subParserList.put(mimeType.trim(), subParser);
			System.out.println("Parser for mimetypes '" + mimeType + "' was installed.");
		}			
	}
	
	/**
	 * Removes a uninstalled {@link ISubParser} from the {@link Activator#subParserList subparser-list}
	 * @param mimeTypes a list of mimeTypes supported by the sub-parser
	 */
	public void removeSubParser(String mimeTypes) {
		this.removeSubParser(mimeTypes.split(";|,"));
	}	
	
	public void removeSubParser(String[] mimeTypes) {
		for (String mimeType : mimeTypes) {			
			this.subParserList.remove(mimeType.trim());
			System.out.println("Parser for mimetypes '" + mimeType + "' was uninstalled.");
		}			
	}
	
	/**
	 * Getting a {@link ISubParser} which is capable to handle the given mime-type
	 * @param mimeType the mime-type of the document which should be parsed
	 * @return a {@link ISubParser} which is capable to parse a document with the given mime-type
	 */
	public ISubParser getSubParser(String mimeType) {
		if (mimeType == null) return null;
		if (this.disabledMimeTypes.contains(mimeType)) return null;
		return this.subParserList.get(mimeType);
	}
	
	/**
	 * Determines if a given mime-type is supported by one of the registered
	 * {@link ISubParser sub-parsers}.
	 * @param mimeType the mime-type
	 * @return <code>true</code> if the given mime-tpye is supported or <code>false</code> otherwise
	 */
	public boolean isSupported(String mimeType) {
		if (this.disabledMimeTypes.contains(mimeType)) return false;
		return this.subParserList.containsKey(mimeType);
	}

	/**
	 * @see ISubParserManager#getSubParsers()
	 */
	public Collection<ISubParser> getSubParsers() {
		return Collections.unmodifiableCollection(this.subParserList.values());
	}
	
	/**
	 * @see ISubParserManager#getMimeTypes()
	 */
	public Collection<String> getMimeTypes() {
		Set<String> keySet = this.subParserList.keySet();
		String[] keyArray = keySet.toArray(new String[keySet.size()]);
		return Collections.unmodifiableCollection(Arrays.asList(keyArray));
	}
	

	/**
	 * @see ISubParserManager#disableMimeType(String)
	 */
	public void disableMimeType(String mimeType) {
		this.disabledMimeTypes.add(mimeType);
		if (this.props != null) this.props.setSet(DISABLED_MIMETYPES, this.disabledMimeTypes);
	}

	/**
	 * @see ISubParserManager#enableMimeType(String)
	 */
	public void enableMimeType(String mimeType) {
		this.disabledMimeTypes.remove(mimeType);		
		if (this.props != null) this.props.setSet(DISABLED_MIMETYPES, this.disabledMimeTypes);
	}

	/**
	 * @see ISubParserManager#disabledMimeType()
	 */
	public Set<String> disabledMimeTypes() {
		return Collections.unmodifiableSet(this.disabledMimeTypes);
	}	
}
