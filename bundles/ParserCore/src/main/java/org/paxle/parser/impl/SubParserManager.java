package org.paxle.parser.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.metatype.AttributeDefinition;
import org.osgi.service.metatype.MetaTypeProvider;
import org.osgi.service.metatype.ObjectClassDefinition;
import org.paxle.core.metadata.IMetaData;
import org.paxle.core.metadata.IMetaDataProvider;
import org.paxle.parser.ISubParser;
import org.paxle.parser.ISubParserManager;

public class SubParserManager implements ISubParserManager, MetaTypeProvider, ManagedService, IMetaDataProvider {
	public static final String PID = ISubParserManager.class.getName();
	
	/* ==============================================================
	 * CM properties
	 * ============================================================== */	
	private static final String ENABLED_MIMETYPES = PID + "." + "enabledMimeTypes";
	private static final String ENABLE_DEFAULT = PID + "." + "enableDefault";
	
	/**
	 * A {@link HashMap} containing the mime-types that is supported by the sub-parser as key and
	 * the {@link ServiceReference} as value.
	 */
	private final HashMap<String,TreeSet<ServiceReference>> subParserList = new HashMap<String,TreeSet<ServiceReference>>();
	
	/**
	 * A list of enabled sub-parsers. The list contains values in the form of
	 * <code>${bundle-symbolic-name}.${service-pid}.${mime-type}</code> or
	 * <code>${bundle-symbolic-name}.${service-class-name}.${mime-type}</code>
	 */
	private final Set<String> enabledServices = new HashSet<String>();	
	
	/**
	 * For logging
	 */
	private final Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * The CM configuration that belongs to this component
	 */
	private final Configuration config;	
	
	/**
	 * A list of {@link Locale} for which a {@link ResourceBundle} exists
	 * @see MetaTypeProvider#getLocales()
	 */
	private final String[] locales;
	
	private boolean enableDefault = true;
	
	private final BundleContext context;
	
	/**
	 * @param config the CM configuration that belongs to this component
	 * @throws IOException
	 * @throws ConfigurationException
	 */	
	public SubParserManager(Configuration config, String[] locales, final BundleContext context) throws IOException, ConfigurationException {
		if (config == null) throw new NullPointerException("The CM configuration is null");
		if (locales == null) throw new NullPointerException("The locale array is null");
		
		this.config = config;
		this.locales = locales;
		this.context = context;
		
		// initialize CM values
		if (config.getProperties() == null) {
			config.update(this.getCMDefaults());
		}
		
		// update configuration of this component
		this.updated(config.getProperties());
	}
	
	private String[] getMimeTypes(final ServiceReference ref) {
		final Object mimeTypesObj = ref.getProperty(ISubParser.PROP_MIMETYPES);
		if (mimeTypesObj instanceof String) {
			return ((String)mimeTypesObj).split(";|,");
		} else if (mimeTypesObj instanceof String[]) {
			return (String[])mimeTypesObj;
		} else {
			final ISubParser p = (ISubParser)context.getService(ref);
			logger.warn(String.format("Parser '%s' registered with no mime-types to the framework", p.getClass().getName()));
			final Collection<String> mimeTypes = p.getMimeTypes();
			if (mimeTypes == null || mimeTypes.size() == 0) {
				logger.error(String.format("Parser '%s' does not provide support for any mime-types", p.getClass().getName()));
				return null;
			}
			context.ungetService(ref);
			return mimeTypes.toArray(new String[mimeTypes.size()]);
		}
	}
	
	private String keyFor(final String mimeType, final ServiceReference ref) {
		final String bundle = (String)ref.getBundle().getHeaders().get(Constants.BUNDLE_SYMBOLICNAME);
		String pid = (String)ref.getProperty(Constants.SERVICE_PID);
		if (pid == null)
			pid = context.getService(ref).getClass().getName();
		final StringBuilder key = new StringBuilder(mimeType.length() + bundle.length() + pid.length() + 2);
		key.append(bundle).append('.')
		.append(pid).append('.')
		.append(mimeType);
		return key.toString().intern();
	}
	
	private boolean isEnabled(final String mimeType, final ServiceReference ref) {
		return this.enabledServices.contains(keyFor(mimeType, ref));
	}
	
	private void setEnabled(final String mimeType, final ServiceReference ref, final boolean enabled) {
		if (enabled) {
			this.enabledServices.add(keyFor(mimeType, ref));
		} else {
			this.enabledServices.remove(keyFor(mimeType, ref));
		}
	}
	
	public void addSubParser(final ServiceReference ref) {
		final String[] mimeTypes = getMimeTypes(ref);
		if (mimeTypes == null)
			return;
		for (String mimeType : mimeTypes) {
			mimeType = mimeType.trim();
			TreeSet<ServiceReference> refs = this.subParserList.get(mimeType);
			if (refs == null)
				this.subParserList.put(mimeType, refs = new TreeSet<ServiceReference>());
			refs.add(ref);
			setEnabled(mimeType, ref, this.enableDefault);
			this.logger.info(String.format(
					"Parser for mimetypes '%s' was installed.",
					mimeType
			));
		}
		if (this.enableDefault) try {
			config.update();
		} catch (IOException e) { logger.error("error updating configuration", e); }
	}
	
	public void removeSubParser(final ServiceReference ref) {
		final String[] mimeTypes = getMimeTypes(ref);
		if (mimeTypes == null)
			return;
		for (String mimeType : mimeTypes) {
			mimeType = mimeType.trim();
			TreeSet<ServiceReference> refs = this.subParserList.get(mimeType);
			if (refs == null)
				continue;
			refs.remove(ref);
			setEnabled(mimeType, ref, false);
			this.logger.info(String.format(
					"Parser for mimetypes '%s' was uninstalled.",
					mimeType
			));
		}
		if (this.enableDefault) try {
			config.update();
		} catch (IOException e) { logger.error("error updating configuration", e); }
	}
	
	/**
	 * Getting a {@link ISubParser} which is capable to handle the given mime-type
	 * @param mimeType the mime-type of the document which should be parsed
	 * @return a {@link ISubParser} which is capable to parse a document with the given mime-type
	 */
	public ISubParser getSubParser(String mimeType) {
		if (mimeType == null)
			return null;
		mimeType = mimeType.trim();
		final TreeSet<ServiceReference> refs = this.subParserList.get(mimeType);
		if (refs == null)
			return null;
		for (final ServiceReference ref : refs)
			if (isEnabled(mimeType, ref))
				return (ISubParser)context.getService(ref);
		return null;
	}
	
	public Collection<ISubParser> getSubParsers(String mimeType) {
		if (mimeType == null)
			return null;
		mimeType = mimeType.trim();
		final TreeSet<ServiceReference> refs = this.subParserList.get(mimeType);
		if (refs == null)
			return null;
		final ArrayList<ISubParser> list = new ArrayList<ISubParser>();
		for (final ServiceReference ref : refs)
			if (isEnabled(mimeType, ref))
				list.add((ISubParser)context.getService(ref));
		return list;
	}
	
	/**
	 * Determines if a given mime-type is supported by one of the registered
	 * {@link ISubParser sub-parsers}.
	 * @param mimeType the mime-type
	 * @return <code>true</code> if the given mime-tpye is supported or <code>false</code> otherwise
	 */
	public boolean isSupported(String mimeType) {
		if (mimeType == null)
			return false;
		mimeType = mimeType.trim();
		final TreeSet<ServiceReference> refs = this.subParserList.get(mimeType);
		if (refs == null)
			return false;
		for (final ServiceReference ref : refs)
			if (isEnabled(mimeType, ref))
				return true;
		return false;
	}
	
	/**
	 * @see ISubParserManager#getSubParsers()
	 */
	public Collection<ISubParser> getSubParsers() {
		final ArrayList<ISubParser> list = new ArrayList<ISubParser>();
		for (final TreeSet<ServiceReference> refs : this.subParserList.values())
			for (final ServiceReference ref : refs)
				list.add((ISubParser)context.getService(ref));
		return Collections.unmodifiableCollection(list);
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
			final TreeSet<ServiceReference> refs = this.subParserList.get(mimeType.trim());
			if (refs != null)
				for (final ServiceReference ref : refs)
					setEnabled(mimeType, ref, false);
			
			// updating CM
			Dictionary<String,Object> props = this.config.getProperties();
			System.out.println("disabled '" + mimeType + "': " + enabledServices);
			props.put(ENABLED_MIMETYPES, enabledServices.toArray(new String[enabledServices.size()]));
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
			final TreeSet<ServiceReference> refs = this.subParserList.get(mimeType.trim());
			if (refs != null)
				for (final ServiceReference ref : refs)
					setEnabled(mimeType, ref, true);
			
			// updating CM
			Dictionary<String,Object> props = this.config.getProperties();
			System.out.println("enabled '" + mimeType + "': " + enabledServices);
			props.put(ENABLED_MIMETYPES, enabledServices.toArray(new String[enabledServices.size()]));
			this.config.update(props);
		} catch (IOException e) {
			this.logger.error(e);
		}
	}
	
	public Set<String> enabledMimeTypes() {
		HashSet<String> mimeTypes = new HashSet<String>();
		for (final String enabledService : this.enabledServices) {
			final String mimeType = enabledService.substring(enabledService.lastIndexOf('.') + 1);
			if (this.subParserList.containsKey(mimeType))
				mimeTypes.add(mimeType);
		}
		
		return mimeTypes;
	}
	
	/**
	 * @see ISubParserManager#disabledMimeType()
	 */
	public Set<String> disabledMimeTypes() {
		// get all available mime-types and remove enabled mime-types
		HashSet<String> mimeTypes = new HashSet<String>(this.subParserList.keySet());
		mimeTypes.removeAll(enabledMimeTypes());
		return mimeTypes;
	}
	
	/**
	 * @see MetaTypeProvider#getLocales()
	 */
	public String[] getLocales() {
		return this.locales;
	}
	
	private final class OCD implements ObjectClassDefinition, IMetaData {
		
		@SuppressWarnings("unchecked")
		private final HashMap<String,TreeSet<ServiceReference>> parsers = (HashMap<String,TreeSet<ServiceReference>>)subParserList.clone();
		private final Locale locale;
		private final ResourceBundle rb;
		
		public OCD(final Locale locale) {
			this.locale = locale;
			this.rb = ResourceBundle.getBundle("OSGI-INF/l10n/" + ISubParserManager.class.getSimpleName(), locale);
		}
		
		public AttributeDefinition[] getAttributeDefinitions(int filter) {
			return new AttributeDefinition[] {
					// Attribute definition for ENABLE_DEFAULT
					new AttributeDefinition() {
						public int getCardinality() 		{ return 0; }
						public String[] getDefaultValue() 	{ return new String[] { Boolean.TRUE.toString() }; }
						public String getDescription() 		{ return rb.getString("subparserManager.enableDefault.desc"); }
						public String getID() 				{ return ENABLE_DEFAULT; }
						public String getName() 			{ return rb.getString("subparserManager.enableDefault.name"); }
						public String[] getOptionLabels() 	{ return new String[] {
								rb.getString("subparserManager.enableDefault.val.true"),
								rb.getString("subparserManager.enableDefault.val.false")
						}; }
						public String[] getOptionValues() 	{ return new String[] { Boolean.TRUE.toString(), Boolean.FALSE.toString() }; }
						public int getType() 				{ return BOOLEAN; }
						public String validate(String value) { return null; }
					},
					
					// Attribute definition for ENABLED_MIMETYPES
					new AttributeDefinition() {
						
						private final String[] optionValues, optionLabels; {
							
							final TreeMap<String,String> options = new TreeMap<String,String>();
							for (final Map.Entry<String,TreeSet<ServiceReference>> entry : parsers.entrySet())
								for (final ServiceReference ref : entry.getValue()) {
									final String key = keyFor(entry.getKey(), ref);
									
									final Object service = context.getService(ref);
									IMetaData metadata = null; 
									if (service instanceof IMetaDataProvider)
										metadata = ((IMetaDataProvider)service).getMetadata(locale);
									
									String name = null;
									if (metadata != null)
										name = metadata.getName();
									if (name == null)
										name = service.getClass().getName();
									context.ungetService(ref);
									
									options.put(key, name + " (" + entry.getKey() + ")");
								}
							
							optionValues = options.keySet().toArray(new String[options.size()]);
							optionLabels = options.values().toArray(new String[options.size()]);
						}
						
						public int getCardinality() {
							return parsers.size();
						}
						
						public String[] getDefaultValue() {
							return this.optionValues;
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
							return this.optionLabels;
						}
						
						public String[] getOptionValues() {
							return this.optionValues;
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
		
		public String getVersion() {
			return null;
		}
	}
	
	/**
	 * @see MetaTypeProvider#getObjectClassDefinition(String, String)
	 */
	public ObjectClassDefinition getObjectClassDefinition(String id, String localeStr) {
		return new OCD((localeStr == null) ? Locale.ENGLISH : new Locale(localeStr));
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.paxle.core.metadata.IMetaDataProvider#getMetadata(java.util.Locale)
	 */
	public IMetaData getMetadata(final Locale locale) {
		return new OCD(locale);
	}
	
	private Hashtable<String,Object> getCMDefaults() {
		final Hashtable<String,Object> defaults = new Hashtable<String,Object>();
		
		// per default parsing of html and plain-text should be enabled
		// FIXME: this setting is dependant on the HtmlParser and PlainParser to be installed
		defaults.put(ENABLED_MIMETYPES, new String[] {
				"org.paxle.ParserHtml.org.paxle.parser.html.impl.HtmlParser.text/html",
				"org.paxle.ParserPlain.org.paxle.parser.plain.impl.PlainParser.text/plain"
		});
		defaults.put(ENABLE_DEFAULT, Boolean.TRUE);
		return defaults;
	}	
	
	/**
	 * @see ManagedService#updated(Dictionary)
	 */
	public void updated(@SuppressWarnings("unchecked") Dictionary properties) throws ConfigurationException {
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
			this.enabledServices.clear();
			this.enabledServices.addAll(Arrays.asList(enabledMimeTypes));
		}
		
		final Object enableDefault = properties.get(ENABLE_DEFAULT);
		if (enableDefault != null)
			if (enableDefault instanceof String) {
				this.enableDefault = Boolean.parseBoolean((String)enableDefault);
			} else {
				this.enableDefault = ((Boolean)enableDefault).booleanValue();
			}
	}
}
