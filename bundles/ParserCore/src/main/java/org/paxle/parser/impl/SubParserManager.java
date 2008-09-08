package org.paxle.parser.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.metatype.AttributeDefinition;
import org.osgi.service.metatype.MetaTypeProvider;
import org.osgi.service.metatype.ObjectClassDefinition;
import org.paxle.parser.ISubParser;
import org.paxle.parser.ISubParserManager;

public class SubParserManager implements ISubParserManager, MetaTypeProvider, ManagedService {
	public static final String PID = ISubParserManager.class.getName();
	
	/* ==============================================================
	 * CM properties
	 * ============================================================== */	
	private static final String ENABLED_MIMETYPES = PID + "." + "enabledMimeTypes";
	
	/**
	 * A {@link HashMap} containing the mime-types that is supported by the sub-parser as key and
	 * the {@link ServiceReference} as value.
	 */
	private final HashMap<String, ISubParser> subParserList = new HashMap<String, ISubParser>();
	
	/**
	 * A list of enabled mime-types
	 */
	private final Set<String> enabledMimeTypes = new HashSet<String>();	
		
	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * The CM configuration that belongs to this component
	 */
	private final Configuration config;	
		
	/**
	 * A list of {@link Locale} for which a {@link ResourceBundle} exists
	 * @see MetaTypeProvider#getLocales()
	 */
	private String[] locales;	
	
	/**
	 * @param config the CM configuration that belongs to this component
	 * @throws IOException
	 * @throws ConfigurationException
	 */	
	public SubParserManager(Configuration config, String[] locales) throws IOException, ConfigurationException {
		if (config == null) throw new NullPointerException("The CM configuration is null");
		if (locales == null) throw new NullPointerException("The locale array is null");
		
		this.config = config;
		this.locales = locales;
		
		// initialize CM values
		if (config.getProperties() == null) {
			config.update(this.getCMDefaults());
		}
		
		// update configuration of this component
		this.updated(config.getProperties());
	}
	
	/**
	 * Adds a newly detected {@link ISubParser} to the {@link Activator#subParserList subparser-list}
	 * @param mimeTypes a list of mimeTypes supported by the sub-parser
	 * @param subParser the newly detected sub-parser
	 */
	public void addSubParser(String mimeTypes, ISubParser subParser) {
		if (mimeTypes == null) throw new NullPointerException("The mimetypes string must not be null.");
		if (subParser == null) throw new NullPointerException("The parser object must not be null.");
		
		this.addSubParser(mimeTypes.split(";|,"), subParser);
	}
	
	public void addSubParser(String[] mimeTypes, ISubParser subParser) {
		if (mimeTypes == null) throw new NullPointerException("The mimetype-array must not be null.");
		if (subParser == null) throw new NullPointerException("The parser object must not be null.");
		
		for (String mimeType : mimeTypes) {
			this.subParserList.put(mimeType.trim(), subParser);
			this.logger.info(String.format(
					"Parser '%s' for mimetypes '%s' was installed.",
					subParser.getClass().getName(),
					mimeType
			));
		}			
	}
	
	/**
	 * Removes a uninstalled {@link ISubParser} from the {@link Activator#subParserList subparser-list}
	 * @param mimeTypes a list of mimeTypes supported by the sub-parser
	 */
	public void removeSubParser(String mimeTypes) {
		if (mimeTypes == null) throw new NullPointerException("The mimetypes string must not be null.");
		
		this.removeSubParser(mimeTypes.split(";|,"));
	}	
	
	public void removeSubParser(String[] mimeTypes) {
		if (mimeTypes == null) throw new NullPointerException("The mimetype-array must not be null.");
		
		for (String mimeType : mimeTypes) {			
			this.subParserList.remove(mimeType.trim());
			this.logger.info(String.format(
					"Parser for mimetypes '%s' was installed.",
					mimeType
			));
		}			
	}
	
	/**
	 * Getting a {@link ISubParser} which is capable to handle the given mime-type
	 * @param mimeType the mime-type of the document which should be parsed
	 * @return a {@link ISubParser} which is capable to parse a document with the given mime-type
	 */
	public ISubParser getSubParser(String mimeType) {
		if (mimeType == null) return null;
		if (!this.enabledMimeTypes.contains(mimeType)) return null;
		return this.subParserList.get(mimeType);
	}
	
	/**
	 * Determines if a given mime-type is supported by one of the registered
	 * {@link ISubParser sub-parsers}.
	 * @param mimeType the mime-type
	 * @return <code>true</code> if the given mime-tpye is supported or <code>false</code> otherwise
	 */
	public boolean isSupported(String mimeType) {
		if (!this.enabledMimeTypes.contains(mimeType)) return false;
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
	@SuppressWarnings("unchecked")
	public void disableMimeType(String mimeType) {
		try {
			if (mimeType == null) return;
			mimeType = mimeType.toLowerCase();

			// update enabled mimetype list
			this.enabledMimeTypes.remove(mimeType);
			
			// updating CM
			Dictionary<String,Object> props = this.config.getProperties();			
			props.put(ENABLED_MIMETYPES, this.enabledMimeTypes.toArray(new String[this.enabledMimeTypes.size()]));
			this.config.update(props);
		} catch (IOException e) {
			this.logger.error(e);
		}
	}

	/**
	 * @see ISubParserManager#enableMimeType(String)
	 */
	@SuppressWarnings("unchecked")
	public void enableMimeType(String mimeType) {
		try {
			if (mimeType == null) return;
			mimeType = mimeType.toLowerCase();

			// updating enabled mimetype list
			this.enabledMimeTypes.add(mimeType);

			// updating CM
			Dictionary<String,Object> props = this.config.getProperties();			
			props.put(ENABLED_MIMETYPES, this.enabledMimeTypes.toArray(new String[this.enabledMimeTypes.size()]));
			this.config.update(props);
		} catch (IOException e) {
			this.logger.error(e);
		}
	}

	/**
	 * @see ISubParserManager#disabledMimeType()
	 */
	public Set<String> disabledMimeTypes() {
		// get all available protocols and remove enabled protocols
		HashSet<String> mimeTypes = new HashSet<String>(this.subParserList.keySet());
		mimeTypes.removeAll(this.enabledMimeTypes);		
		return mimeTypes;
	}

	/**
	 * @see MetaTypeProvider#getLocales()
	 */
	public String[] getLocales() {
		return this.locales;
	}

	/**
	 * @see MetaTypeProvider#getObjectClassDefinition(String, String)
	 */
	@SuppressWarnings("unchecked")
	public ObjectClassDefinition getObjectClassDefinition(String id, String localeStr) {
		final HashMap<String, ISubParser> parsers = (HashMap<String, ISubParser>) this.subParserList.clone();
		
		Locale locale = (localeStr==null) ? Locale.ENGLISH : new Locale(localeStr);
		final ResourceBundle rb = ResourceBundle.getBundle("OSGI-INF/l10n/" + ISubParserManager.class.getSimpleName(), locale);	
		
		return new ObjectClassDefinition() {
			public AttributeDefinition[] getAttributeDefinitions(int filter) {
				return new AttributeDefinition[]{
					// Attribute definition for ENABLED_MIMETYPES
					new AttributeDefinition(){
						private String[] getSupportedMimeTypes() {
							// get all supported protocols and sort them
							String[] protocols = parsers.keySet().toArray(new String[parsers.size()]);
							Arrays.sort(protocols);							
							return protocols;							
						}
						
						public int getCardinality() {
							return parsers.size();
						}

						public String[] getDefaultValue() {
							return this.getSupportedMimeTypes();
						}

						public String getDescription() {
							return rb.getString("subparserManager.enabledMimeTypes.desc");
						}

						public String getID() {
							return ENABLED_MIMETYPES;
						}

						public String getName() {
							return rb.getString("subparserManager.enabledMimeTypes.name");
						}

						public String[] getOptionLabels() {
							return this.getSupportedMimeTypes();
						}

						public String[] getOptionValues() {
							return this.getSupportedMimeTypes();
						}

						public int getType() {
							return AttributeDefinition.STRING;
						}

						public String validate(String value) {
							return null;
						}						
					}	
				};
			}

			public String getDescription() {
				return rb.getString("subparserManager.desc");
			}

			public String getID() {
				return PID;
			}

			public InputStream getIcon(int size) throws IOException {
				return (size == 16) 
				? this.getClass().getResourceAsStream("/OSGI-INF/images/filetypes.png")
				: null;
			}

			public String getName() {				
				return rb.getString("subparserManager.name");
			}			
		};
	}

	private Hashtable<String,Object> getCMDefaults() {
		final Hashtable<String,Object> defaults = new Hashtable<String,Object>();
		
		// per default parsing of html and plain-text should be enabled
		defaults.put(ENABLED_MIMETYPES, new String[]{"text/html","text/plain"});
		
		return defaults;
	}	
	
	/**
	 * @see ManagedService#updated(Dictionary)
	 */
	public void updated(Dictionary properties) throws ConfigurationException {
		if (properties == null ) {
			logger.warn("updated configuration is null");
			/*
			 * Generate default configuration
			 */
			properties = this.getCMDefaults();
		}
		
		// configuring enabled protocols
		String[] enabledMimeTypes = (String[]) properties.get(ENABLED_MIMETYPES);
		if (enabledMimeTypes != null) {
			this.enabledMimeTypes.clear();
			this.enabledMimeTypes.addAll(Arrays.asList(enabledMimeTypes));
		}
	}
}
