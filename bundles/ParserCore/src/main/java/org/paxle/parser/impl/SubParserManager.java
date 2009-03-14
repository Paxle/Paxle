/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
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
import java.util.Properties;
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
	private static final char SUBPARSER_PID_SEP = '#';	
	
	/* ==============================================================
	 * CM properties
	 * ============================================================== */	
	private static final String ENABLED_MIMETYPES = PID + "." + "enabledMimeTypes";
	private static final String ENABLE_DEFAULT = PID + "." + "enableDefault";
	
	/* ==============================================================
	 * Property keys
	 * ============================================================== */	
	private static final String PROPS_KNOWN_PARSER_PIDS = PID + "." + "knownParserPids";
	
	/**
	 * A {@link HashMap} containing the mime-types that is supported by the sub-parser as key and
	 * the {@link ServiceReference} as value.
	 */
	private final HashMap<String,TreeSet<ServiceReference>> subParserList = new HashMap<String,TreeSet<ServiceReference>>();
	
	private final Map<String,ServiceReference> services = new HashMap<String,ServiceReference>();
	
	/**
	 * A list of enabled sub-parsers. The list contains values in the form of
	 * <code>${bundle-symbolic-name}#${service-pid}#${mime-type}</code> or
	 * <code>${bundle-symbolic-name}#${service-class-name}#${mime-type}</code>
	 */
	private final Set<String> enabledServices = new HashSet<String>();
	
	private final Set<String> knownServicePids = new HashSet<String>();
	
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
	private final Properties props;
	
	/**
	 * @param config the CM configuration that belongs to this component
	 * @throws IOException
	 * @throws ConfigurationException
	 */	
	public SubParserManager(Configuration config, String[] locales, final BundleContext context, final Properties props) throws IOException, ConfigurationException {
		if (config == null) throw new NullPointerException("The CM configuration is null");
		if (locales == null) throw new NullPointerException("The locale array is null");
		
		this.config = config;
		this.locales = locales;
		this.context = context;
		this.props = props;
		if (props.get(PROPS_KNOWN_PARSER_PIDS) != null) {
			final String knownStr = props.getProperty(PROPS_KNOWN_PARSER_PIDS);
			if (knownStr.length() > 2)
				for (final String parserPid : knownStr.substring(1, knownStr.length() - 1).split(","))
					this.knownServicePids.add(parserPid.trim());
		}
		
		// initialize CM values
		if (config.getProperties() == null) {
			config.update(this.getCMDefaults());
		}
		
		// update configuration of this component
		this.updated(config.getProperties());
	}
	
	public void close() {
		this.props.put(PROPS_KNOWN_PARSER_PIDS, this.knownServicePids.toString());
	}
	
	private String[] getMimeTypes(final ServiceReference ref) {
		final Object mimeTypesObj = ref.getProperty(ISubParser.PROP_MIMETYPES);
		if (mimeTypesObj instanceof String) {
			return ((String)mimeTypesObj).split(";|,");
		} else if (mimeTypesObj instanceof String[]) {
			return (String[])mimeTypesObj;
		} else {
			try {
				final ISubParser p = (ISubParser)context.getService(ref);
				this.logger.warn(String.format("Parser '%s' registered with no mime-types to the framework", p.getClass().getName()));
				return null;
			} finally {		
				context.ungetService(ref);
			}
		}
	}
	
	private String keyFor(final String mimeType, final ServiceReference ref) {
		final String bundle = (String)ref.getBundle().getHeaders().get(Constants.BUNDLE_SYMBOLICNAME);
		String pid = (String)ref.getProperty(Constants.SERVICE_PID);
		if (pid == null) pid = context.getService(ref).getClass().getName();
		
		final StringBuilder key = new StringBuilder(mimeType.length() + bundle.length() + pid.length() + 2);
		key.append(bundle).append(SUBPARSER_PID_SEP)
		   .append(pid).append(SUBPARSER_PID_SEP)
		   .append(mimeType);
		
		return key.toString().intern();
	}
	
	private String extractMimeType(String servicePID) {
		return servicePID.substring(servicePID.lastIndexOf(SUBPARSER_PID_SEP) + 1);
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
	
	@SuppressWarnings("unchecked")
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
			final String parserPid = keyFor(mimeType, ref);
			this.services.put(parserPid, ref);
			if (!this.knownServicePids.contains(parserPid)) {
				this.knownServicePids.add(parserPid);
				setEnabled(mimeType, ref, this.enableDefault);
			}
			this.logger.info(String.format(
					"Parser for mimetypes '%s' was installed.",
					mimeType
			));
		}
		try {
			final Dictionary props = config.getProperties();
			props.put(ENABLED_MIMETYPES, this.enabledServices.toArray(new String[this.enabledServices.size()]));
			config.update(props);
		} catch (IOException e) { logger.error("error updating configuration", e); }
	}
	
	@SuppressWarnings("unchecked")
	public void removeSubParser(final ServiceReference ref) {
		final String[] mimeTypes = getMimeTypes(ref);
		if (mimeTypes == null)
			return;
		for (String mimeType : mimeTypes) {
			mimeType = mimeType.trim();
			TreeSet<ServiceReference> refs = this.subParserList.get(mimeType);
			if (refs == null)
				continue;
			this.services.remove(keyFor(mimeType, ref));
			refs.remove(ref);
			this.logger.info(String.format(
					"Parser for mimetypes '%s' was uninstalled.",
					mimeType
			));
		}
		try {
			final Dictionary props = config.getProperties();
			props.put(ENABLED_MIMETYPES, this.enabledServices.toArray(new String[this.enabledServices.size()]));
			config.update(props);
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
		final ArrayList<ISubParser> list = new ArrayList<ISubParser>(refs.size());
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
	public Map<String,ISubParser> getSubParsers() {
		final HashMap<String,ISubParser> map = new HashMap<String, ISubParser>();
		for (final TreeSet<ServiceReference> refs : this.subParserList.values()) {
			for (final ServiceReference ref : refs) {
				final ISubParser parser = (ISubParser) this.context.getService(ref);
				final String servicePID = (String) ref.getProperty(Constants.SERVICE_PID);
				map.put(servicePID, parser);
			}
		}
		return map;
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
			props.put(ENABLED_MIMETYPES, enabledServices.toArray(new String[enabledServices.size()]));
			this.config.update(props);
		} catch (IOException e) {
			this.logger.error(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void enableParser(final String service) {
		if (service == null) return;
		try {
			final String mimeType = this.extractMimeType(service);
			setEnabled(mimeType, this.services.get(service), true);
			
			// updating CM
			Dictionary<String,Object> props = this.config.getProperties();
			props.put(ENABLED_MIMETYPES, enabledServices.toArray(new String[enabledServices.size()]));
			this.config.update(props);
		} catch (IOException e) {
			this.logger.error(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void disableParser(final String service) {
		if (service == null) return;
		try {
			final String mimeType = this.extractMimeType(service);
			setEnabled(mimeType, this.services.get(service), false);
			
			// updating CM
			Dictionary<String,Object> props = this.config.getProperties();
			props.put(ENABLED_MIMETYPES, enabledServices.toArray(new String[enabledServices.size()]));
			this.config.update(props);
		} catch (IOException e) {
			this.logger.error(e);
		}
	}
	
	public Set<String> enabledParsers() {
		final String[] services = this.enabledServices.toArray(new String[this.enabledServices.size()]);
		return new HashSet<String>(Arrays.asList(services));
	}
	
	public Map<String,Set<String>> getParsers() {
		final HashMap<String,Set<String>> r = new HashMap<String,Set<String>>();
		for (final Map.Entry<String,ServiceReference> entry : this.services.entrySet()) {
			final String bundleName = (String)entry.getValue().getBundle().getHeaders().get(Constants.BUNDLE_NAME);
			Set<String> keys = r.get(bundleName);
			if (keys == null)
				r.put(bundleName, keys = new HashSet<String>());
			keys.add(entry.getKey());
		}
		return Collections.unmodifiableMap(r);
	}
	
	public Set<String> enabledMimeTypes() {
		HashSet<String> mimeTypes = new HashSet<String>();
		for (final String enabledService : this.enabledServices) {
			final String mimeType = this.extractMimeType(enabledService);
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
		return this.locales==null?null:this.locales.clone();
	}
	
	private final class OCD implements ObjectClassDefinition, IMetaData {
		
		@SuppressWarnings("unchecked")
		private final HashMap<String,TreeSet<ServiceReference>> parsers = (HashMap<String,TreeSet<ServiceReference>>)subParserList.clone();
		private final String localeStr;
		private final ResourceBundle rb;
		
		public OCD(final String localeStr) {
			this.localeStr = localeStr;
			Locale locale = (localeStr == null) ? Locale.ENGLISH : new Locale(localeStr);
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
						public String[] getOptionLabels() 	{ return null; }
						public String[] getOptionValues() 	{ return null; }
						public int getType() 				{ return BOOLEAN; }
						public String validate(String value) { return null; }
					},
					
					// Attribute definition for ENABLED_MIMETYPES
					new AttributeDefinition() {
						
						private final String[] optionValues, optionLabels; {
							
							final TreeMap<String,String> options = new TreeMap<String,String>();
							for (final Map.Entry<String,TreeSet<ServiceReference>> entry : parsers.entrySet())
								for (final ServiceReference ref : entry.getValue()) {
									try {
										// getting the attribute-key to use
										final String key = keyFor(entry.getKey(), ref);
										
										// getting the SubParser-service
										final Object service = context.getService(ref);
										
										// getting additional-metadata (if available)
										IMetaData metadata = null; 
										if (service instanceof IMetaDataProvider) {
											metadata = ((IMetaDataProvider)service).getMetadata(null, localeStr);
										}
										
										String name = (metadata != null)
													? metadata.getName()
													: service.getClass().getName();
													
										options.put(key, name + " (" + entry.getKey() + ")");
									} finally {
										context.ungetService(ref);
									}
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
		return new OCD(localeStr);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.paxle.core.metadata.IMetaDataProvider#getMetadata(java.util.Locale)
	 */
	public IMetaData getMetadata(String id, String localeStr) {
		return new OCD(localeStr);
	}
	
	private Hashtable<String,Object> getCMDefaults() {
		final Hashtable<String,Object> defaults = new Hashtable<String,Object>();
		
		// per default parsing of html and plain-text should be enabled
		// FIXME: this setting is dependant on the HtmlParser and PlainParser to be installed
		defaults.put(ENABLED_MIMETYPES, new String[] {
				"org.paxle.ParserHtml" + SUBPARSER_PID_SEP + "org.paxle.parser.html.impl.HtmlParser" + SUBPARSER_PID_SEP + "text/html",
				"org.paxle.ParserPlain" + SUBPARSER_PID_SEP + "org.paxle.parser.plain.impl.PlainParser" + SUBPARSER_PID_SEP + "text/plain"
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
			this.enableDefault = ((Boolean)enableDefault).booleanValue();
	}
}
