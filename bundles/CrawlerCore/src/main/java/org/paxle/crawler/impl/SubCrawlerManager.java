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
package org.paxle.crawler.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Services;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.metatype.AttributeDefinition;
import org.osgi.service.metatype.MetaTypeProvider;
import org.osgi.service.metatype.ObjectClassDefinition;
import org.paxle.core.io.IResourceBundleTool;
import org.paxle.core.metadata.IMetaData;
import org.paxle.core.metadata.IMetaDataProvider;
import org.paxle.core.metadata.IMetaDataService;
import org.paxle.crawler.ISubCrawler;
import org.paxle.crawler.ISubCrawlerManager;
import org.paxle.parser.ISubParserManager;

@Component(metatype=false, immediate=true, name=SubCrawlerManager.PID)
@Services({
	@Service(ISubCrawlerManager.class),
	@Service(MetaTypeProvider.class),
	@Service(IMetaDataProvider.class)
})
@Reference(
	name=SubCrawlerManager.SERVICE_REFS_SUBCRAWLERS,
	referenceInterface=ISubCrawler.class,
	cardinality=ReferenceCardinality.OPTIONAL_MULTIPLE,
	policy=ReferencePolicy.DYNAMIC,
	bind="addSubCrawler",
	unbind="removeSubCrawler",
	target="(&(service.pid=*)(Protocol=*))"
)
public class SubCrawlerManager implements ISubCrawlerManager, MetaTypeProvider, IMetaDataProvider {
	static final String SERVICE_REFS_SUBCRAWLERS = "subCrawlers";
	
	/**
	 * The {@link Constants#SERVICE_PID} of this component
	 */
	static final String PID = "org.paxle.crawler.ISubCrawlerManager";
	static final char SUBCRAWLER_PID_SEP = '#';	

	@Property
	private static final String DISABLED_PROTOCOLS = PID + "." + "disabledProtocols";
	
	/**
	 * For logging
	 */
	private final Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * A {@link HashMap} containing the protocol that is supported by a sub-crawler as key and
	 * the {@link ISubCrawler} as value
	 */
	private final HashMap<String,TreeSet<ServiceReference>> subCrawlerList = new HashMap<String,TreeSet<ServiceReference>>();
	
	/**
	 * A list of disabled crawling protocols. An entry in this list has the format:
	 * "<code>[protocol]#[crawler-PID]</code>"
	 * 
	 * @see #keyForCrawlerProtocol(String, ServiceReference)
	 */
	private final Set<String> disabledCrawlerProtocols = new HashSet<String>();
	
	@Reference
	protected IResourceBundleTool resourceBundleTool;	

	@Reference(cardinality=ReferenceCardinality.OPTIONAL_UNARY, policy=ReferencePolicy.DYNAMIC)
	protected IMetaDataService metaDataService;	
	
	/**
	 * A list of {@link Locale} for which a {@link ResourceBundle} exists
	 * @see MetaTypeProvider#getLocales()
	 */
	private String[] locales;

	/**
	 * The OSGi DS component context
	 */
	private ComponentContext context;
		
	protected void activate(ComponentContext context) {
		this.context = context;
		
		// the supported locales
		this.locales = this.resourceBundleTool.getLocaleArray(ISubCrawlerManager.class.getSimpleName(), Locale.ENGLISH);
		
		// reading CM data
		@SuppressWarnings("unchecked")
		final Dictionary<String, Object> properties = context.getProperties();
		
		// configuring enabled protocols
		final String[] disabledProtocols = (String[]) properties.get(DISABLED_PROTOCOLS);
		if (disabledProtocols != null) {
			this.disabledCrawlerProtocols.clear();
			this.disabledCrawlerProtocols.addAll(Arrays.asList(disabledProtocols));
		}		
	}
	
	protected void deactivate(ComponentContext context) {
		// Nothing special to do here
	}
	
	/**
	 * @return the {@link ISubCrawler#PROP_PROTOCOL protocols} supported by a {@link ISubCrawler sub-crawler} 
	 */
	private String[] getProtocols(final ServiceReference ref) {
		final Object protocolsObj = ref.getProperty(ISubCrawler.PROP_PROTOCOL);
		if (protocolsObj instanceof String) {
			return ((String)protocolsObj).split(";|,");
		} else if (protocolsObj instanceof String[]) {
			return (String[])protocolsObj;
		} 
		return new String[0];
	}
	
	/**
	 * This method generates a unique identifier of a protocol supported by a {@link ISubCrawler}. 
	 * The syntax of this key is "<code>[ProtocolName]#[{@link Constants#SERVICE_PID}]</code>".
	 * @param protocol the protocol
	 * @param ref a reference to the {@link ISubCrawler}
	 * @return a unique identifier of a protocol supported by a {@link ISubCrawler}, e.g.
	 * 	<code>http#org.paxle.crawler.http.impl.HttpCrawler</code>
	 */
	private String keyForCrawlerProtocol(final String protocol, final ServiceReference ref) {
		final String pid = (String)ref.getProperty(Constants.SERVICE_PID);
		
		final StringBuilder key = new StringBuilder(protocol.length() + pid.length() + 1);
		key.append(protocol)
		   .append(SUBCRAWLER_PID_SEP)
		   .append(pid);
		
		return key.toString().intern();
	}
	
	private boolean isEnabled(final String protocol, final ServiceReference ref) {
		final String protocolCrawlerKey = this.keyForCrawlerProtocol(protocol, ref);
		return !this.disabledCrawlerProtocols.contains(protocolCrawlerKey);
	}
	
	/**
	 * Adds a newly detected {@link ISubCrawler} to the {@link #subCrawlerList subcrawler-list}
	 * @param ref the reference to the deployed {@link ISubCrawler subcrawler-service}
	 */
	protected void addSubCrawler(final ServiceReference ref) {
		final String crawlerPid = (String) ref.getProperty(Constants.SERVICE_PID);
		
		// getting all protocols supported by the crawler
		final String[] protocols = this.getProtocols(ref);
		if (protocols == null) return;
				
		for (String protocol : protocols) {
			protocol = protocol.trim().toLowerCase();
			
			// getting all crawlers supporting the given protocol
			TreeSet<ServiceReference> refs = this.subCrawlerList.get(protocol);
			if (refs == null) this.subCrawlerList.put(protocol, refs = new TreeSet<ServiceReference>());
			
			// adding new crawler to the list
			refs.add(ref);
		}
		
		if (this.logger.isInfoEnabled()) {
			final StringBuilder msg = new StringBuilder();
			msg.append(String.format(
					"Crawler '%s' installed. %d supported protocol(s):",
					crawlerPid,
					protocols.length
			));
			for (String protocol : protocols) {
				msg.append("\r\n\t")
				   .append(protocol)
				   .append(": ")
				   .append(this.isEnabled(protocol, ref)?"enabled":"disabled");
			}
			this.logger.info(msg.toString());			
		}
	}
	
	/**
	 * Removes a uninstalled {@link ISubCrawler} from the {@link Activator#subCrawlerList subcrawler-list}
	 * @param ref the reference to the {@link ISubCrawler subcrawler-service} to be removed
	 */
	protected void removeSubCrawler(final ServiceReference ref) {
		final String crawlerPid = (String) ref.getProperty(Constants.SERVICE_PID);
		
		// getting all protocols supported by the crawler
		final String[] protocols = getProtocols(ref);
		if (protocols == null) return;
		
		for (String protocol : protocols) {
			protocol = protocol.trim().toLowerCase();
			
			// getting all crawlers supporting the given protocol
			TreeSet<ServiceReference> refs = this.subCrawlerList.get(protocol);
			if (refs == null) continue;
			
			// removing crawler fro the list
			refs.remove(ref);
		}
		
		if (this.logger.isInfoEnabled()) {
			final StringBuilder msg = new StringBuilder();
			msg.append(String.format(
					"Crawler '%s' uninstalled. %d supported protocols: ",
					crawlerPid,
					protocols.length
			));
			for (String protocol : protocols) {
				msg.append("\r\n\t").
					append(protocol).
					append(": ").
					append(this.isEnabled(protocol, ref)?"enabled":"disabled");
			}
			this.logger.info(msg.toString());			
		}
	}
	
	public ISubCrawler getSubCrawler(String protocol) {
		if (protocol == null) return null;
		protocol = protocol.trim().toLowerCase();
		
		final TreeSet<ServiceReference> refs = this.subCrawlerList.get(protocol);
		if (refs == null) return null;
		
		for (final ServiceReference ref : refs) {
			if (this.isEnabled(protocol, ref)) {
				return (ISubCrawler) this.context.locateService(SERVICE_REFS_SUBCRAWLERS, ref);
			}
		}
		
		return null;
	}
	
	public Collection<ISubCrawler> getSubCrawlers(String protocol) {
		if (protocol == null) return Collections.emptyList();
		protocol = protocol.trim().toLowerCase();
		
		final TreeSet<ServiceReference> refs = this.subCrawlerList.get(protocol);
		if (refs == null) return Collections.emptyList();
		
		final ArrayList<ISubCrawler> list = new ArrayList<ISubCrawler>(refs.size());
		for (final ServiceReference ref : refs) {
			if (this.isEnabled(protocol, ref)) {
				list.add((ISubCrawler) this.context.locateService(SERVICE_REFS_SUBCRAWLERS, ref));
			}
		}
		
		return list;
	}
	
	/**
	 * Determines if a given protocol is supported by one of the registered
	 * {@link ISubCrawler sub-crawlers}.
	 * @param protocol the protocol
	 * @return <code>true</code> if the given protocol is supported or <code>false</code> otherwise
	 */
	public boolean isSupported(String protocol) {
		if (protocol == null) return false;
		protocol = protocol.trim().toLowerCase();
		
		final TreeSet<ServiceReference> refs = this.subCrawlerList.get(protocol);
		if (refs == null) return false;
		
		for (final ServiceReference ref : refs) {
			if (this.isEnabled(protocol, ref)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * @see ISubCrawlerManager#getSubCrawlers()
	 */
	public Map<String,ISubCrawler> getSubCrawlers() {
		final HashMap<String,ISubCrawler> map = new HashMap<String, ISubCrawler>();
		
		for (final TreeSet<ServiceReference> refs : this.subCrawlerList.values()) {
			for (final ServiceReference ref : refs) {
				final ISubCrawler crawler = (ISubCrawler) this.context.locateService(SERVICE_REFS_SUBCRAWLERS, ref);
				final String servicePID = (String) ref.getProperty(Constants.SERVICE_PID);
				map.put(servicePID, crawler);
			}
		}
		
		return map;
	}
	
	/**
	 * @see ISubCrawler#PROP_PROTOCOL
	 */
	public Set<String> getProtocols() {
		final Set<String> protocols = this.subCrawlerList.keySet();
		return Collections.unmodifiableSet(protocols);
	}	

	/**
	 * @see ISubParserManager#disabledMimeType()
	 */
	public Set<String> disabledProtocols() {
		// get all available mime-types and remove enabled mime-types
		final HashSet<String> protocols = new HashSet<String>(this.subCrawlerList.keySet());
		
		final Iterator<String> protocolIter = protocols.iterator();
		while(protocolIter.hasNext()) {
			final String protocol = protocolIter.next();
			if (this.isSupported(protocol)) {
				protocolIter.remove();
			}
		}

		return protocols;
	}
	
	/**
	 * @see MetaTypeProvider#getLocales()
	 */
	public String[] getLocales() {
		return this.locales==null?null:this.locales.clone();
	}
	
	private final class OCD implements ObjectClassDefinition, IMetaData {
		
		@SuppressWarnings("unchecked")
		private final HashMap<String,TreeSet<ServiceReference>> crawlers = (HashMap<String,TreeSet<ServiceReference>>)subCrawlerList.clone();
		private final String localeStr;
		private final ResourceBundle rb;
		
		public OCD(final String localeStr) {
			this.localeStr = localeStr;
			Locale locale = (localeStr==null)? Locale.ENGLISH : new Locale(localeStr);
			this.rb = ResourceBundle.getBundle("OSGI-INF/l10n/" + ISubCrawlerManager.class.getSimpleName(), locale);
		}
		
		public AttributeDefinition[] getAttributeDefinitions(int filter) {
			return new AttributeDefinition[] {
					// Attribute definition for ENABLE_DEFAULT
//					new AttributeDefinition() {
//						public int getCardinality() 		{ return 0; }
//						public String[] getDefaultValue() 	{ return new String[] { Boolean.TRUE.toString() }; }
//						public String getDescription() 		{ return rb.getString("subcrawlerManager.enableDefault.desc"); }
//						public String getID() 				{ return ENABLE_DEFAULT; }
//						public String getName() 			{ return rb.getString("subcrawlerManager.enableDefault.name"); }
//						public String[] getOptionLabels() 	{ return null; }
//						public String[] getOptionValues() 	{ return null; }
//						public int getType() 				{ return BOOLEAN; }
//						public String validate(String value) { return null; }
//					},
					
					// Attribute definition for DISABLED_PROTOCOLS
					new AttributeDefinition() {
						
						private final String[] optionValues, optionLabels; {							
							final TreeMap<String,String> options = new TreeMap<String,String>();
							
							for (final Map.Entry<String,TreeSet<ServiceReference>> entry : crawlers.entrySet()) {
								for (final ServiceReference ref : entry.getValue()) {
									// getting the attribute-key to use																			
									final String key = keyForCrawlerProtocol(entry.getKey(), ref);
									final String PID = (String) ref.getProperty(Constants.SERVICE_PID);
									
									// getting the SubCrawler-service
									final Object service = context.locateService(SERVICE_REFS_SUBCRAWLERS, ref);
									
									// getting additional-metadata (if available)
									IMetaData metadata = null; 
									if (metaDataService != null) {
										metadata = metaDataService.getMetadata(PID, localeStr);
									}
									
									String name = (metadata != null)
												? metadata.getName()
												: service.getClass().getName();
												
									options.put(key, name + " [" + entry.getKey() + "]");
								}
							}
								
							optionValues = options.keySet().toArray(new String[options.size()]);
							optionLabels = options.values().toArray(new String[options.size()]);
						}
						
						public int getCardinality() {
							return optionValues.length;
						}
						
						public String[] getDefaultValue() {
							return new String[0];
						}
						
						public String getDescription() {
							return rb.getString("subcrawlerManager.disabledProtocols.desc");
						}
						
						public String getID() {
							return DISABLED_PROTOCOLS;
						}
						
						public String getName() {
							return rb.getString("subcrawlerManager.disabledProtocols.name");
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
			return rb.getString("subcrawlerManager.desc");
		}
		
		public String getID() {
			return ISubCrawlerManager.class.getName();
		}
		
		public InputStream getIcon(int size) throws IOException {
			return (size == 16) 
			? this.getClass().getResourceAsStream("/OSGI-INF/images/network.png")
					: null;
		}
		
		public String getName() {				
			return rb.getString("subcrawlerManager.name");
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
	
	public IMetaData getMetadata(String id, String localeStr) {
		return new OCD(localeStr);
	}
}
