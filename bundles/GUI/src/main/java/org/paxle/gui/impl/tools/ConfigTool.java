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
package org.paxle.gui.impl.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.ValidScope;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.metatype.AttributeDefinition;
import org.osgi.service.metatype.MetaTypeInformation;
import org.osgi.service.metatype.MetaTypeProvider;
import org.osgi.service.metatype.MetaTypeService;
import org.osgi.service.metatype.ObjectClassDefinition;
import org.paxle.core.metadata.Attribute;
import org.paxle.core.metadata.Metadata;

@DefaultKey(ConfigTool.TOOL_NAME)
@ValidScope(Scope.REQUEST)
public class ConfigTool extends PaxleLocaleConfig {
	public static final String TOOL_NAME = "configTool";
	
	/**
	 * The OSGI meta-type service
	 */
	private MetaTypeService metaTypeService = null;
	
	/**
	 * The OSGI configuration admin service
	 */
	private ConfigurationAdmin configAdmin = null;
	
	@Override
	public void configure(@SuppressWarnings("unchecked") Map props) {
		super.configure(props);
		
		if (this.context != null) {
			// getting the meta-type service
			ServiceReference ref = this.context.getServiceReference(MetaTypeService.class.getName());
			if (ref != null) {
				this.metaTypeService = (MetaTypeService) this.context.getService(ref);
			}
			
			// getting the config-admin service
			ref = this.context.getServiceReference(ConfigurationAdmin.class.getName());
			if (ref != null) {
				this.configAdmin = (ConfigurationAdmin) this.context.getService(ref);
			}
		} else {
			this.logger.error("The bundle-context is null");
		}
	}
	
	/**
	 * @return the {@link Constants#SERVICE_PID PID}s of all {@link ManagedService managed-services}
	 * whose {@link MetaTypeInformation metatype-informations} are managed by the {@link MetaTypeService}
	 */	
	private Set<String> getMetaTypeServicePIDs(Bundle bundle) {
		final HashSet<String> pids = new HashSet<String>();
		
		if (this.metaTypeService != null) {
			MetaTypeInformation mti = this.metaTypeService.getMetaTypeInformation(bundle);
			String[] pidArray = (mti==null)?null:mti.getPids();
			if (pidArray != null) {
				pids.addAll(Arrays.asList(pidArray));
			} 
		} else {
			this.logger.warn("No metatype service available.");
		}	
		
		return pids;
	}
	
	/**
	 * @return the {@link Constants#SERVICE_PID PID} of all {@link ManagedService managed-services}
	 * whose {@link MetaTypeInformation metatype-informations} are provided by a {@link MetaTypeProvider}
	 */
	private Set<String> getMetaTypeProviderPIDs(Bundle bundle) {
		final HashSet<String> metaTypePIDs = new HashSet<String>();
		
		final ServiceReference[] serviceRefs = bundle.getRegisteredServices();
		if (serviceRefs != null) {
			for (ServiceReference ref : serviceRefs) {
				// a managed-service always has a PID
				String pid = (String) ref.getProperty(Constants.SERVICE_PID);
				if (pid!= null) {			
					HashSet<String> objectClassSet = new HashSet<String>(Arrays.asList((String[])ref.getProperty(Constants.OBJECTCLASS)));
					if (objectClassSet.contains(MetaTypeProvider.class.getName()) && !metaTypePIDs.contains(pid)) {
						metaTypePIDs.add(pid);
					}
				}
			}
		}
		
		return metaTypePIDs;
	}
	
	public List<Configurable> getConfigurables() {
		final ArrayList<Configurable> configurables = new ArrayList<Configurable>();
		
		for (Bundle bundle : this.context.getBundles()) {
			List<Configurable> bundleConfigurables = this.getConfigurables(bundle, null);
			if (configurables != null) {
				configurables.addAll(bundleConfigurables);
			}
		}
		
		return configurables;
	}
	
	public boolean hasConfigurables(Integer bundleID) {
		return this.getConfigurables(bundleID).size() > 0;
	}
	
	public List<Configurable> getConfigurables(Integer bundleID) {
		return this.getConfigurables(bundleID, null);
	}
	
	public boolean hasConfigurables(Bundle bundle) {
		return this.getConfigurables(bundle).size() > 0;
	}
	
	public List<Configurable> getConfigurables(Bundle bundle) {
		return this.getConfigurables(bundle, null);
	}	
	
	public List<Configurable> getConfigurables(Integer bundleID, String[] configurableIDs) {
		// getting a reference to the requested bundle
		final Bundle bundle = this.getBundleByBundleID(bundleID);
		return (bundle==null) ? null : this.getConfigurables(bundle, configurableIDs);
	}
	
	public List<Configurable> getConfigurables(Bundle bundle, String[] configurableIDs) {
		final ArrayList<Configurable> configurables = new ArrayList<Configurable>();
		if (bundle == null) return configurables;
		
		// processing all metatypes managed by the meta-type service
		Set<String> metaTypeServicePids = this.getMetaTypeServicePIDs(bundle);
		for (String pid : metaTypeServicePids) {
			configurables.add(new Configurable(bundle, pid, false));
		}
		
		// processing all metatypes managed by a meta-type provider
		Set<String>  metaTypeProviderPids = this.getMetaTypeProviderPIDs(bundle);
		for (String pid : metaTypeProviderPids) {
			if (metaTypeServicePids.contains(pid)) continue;
			configurables.add(new Configurable(bundle, pid, true));
		}
			
		return configurables;
	}
	
	public boolean isConfigurable(Integer bundleID, String configurableID) {
		return this.getConfigurable(bundleID, configurableID) != null;
	}
	
	public Configurable getConfigurable(Integer bundleID, String configurableID) {
		return this.getConfigurable(this.getBundleByBundleID(bundleID), configurableID);
	}
	
	public boolean isConfigurable(Bundle bundle, String configurableID) {
		return this.getConfigurable(bundle, configurableID) != null;
	}
	
	public Configurable getConfigurable(Bundle bundle, String configurableID) {
		if (bundle == null) return null;
		else if (configurableID == null) return null;
		
		final List<Configurable> configurables = this.getConfigurables(bundle);
		for (Configurable configurable : configurables) {
			if (configurable.getPID().equals(configurableID)) {
				return configurable;
			}
		}
		
		return null;
	}
	
	public final class Configurable {
		private final Bundle bundle;
		private final String pid;
		private boolean isProvider;
		
		public Configurable(Bundle bundle, String pid, boolean isProvider) {
			this.bundle = bundle;
			this.pid = pid;
			this.isProvider = isProvider;
		}
		
		/**
		 * @return the OSGi bundle the configurable service belongs to
		 */
		public Bundle getBundle() {
			return this.bundle;
		}
		
		/**
		 * @return the {@link Constants#SERVICE_PID} of the configurable service
		 */
		public String getPID() {
			return this.pid;
		}
		
		/**
		 * @return the {@link Configuration configuration} of the configurable service
		 */
		public Configuration getConfiguration() throws IOException {
			return configAdmin.getConfiguration(this.pid, this.bundle.getLocation());
		}
		
		/**
		 * @return the {@link ObjectClassDefinition configuration-options} of the configurable service
		 */
		public ObjectClassDefinition getObjectClassDefinition() {
			return this.getObjectClassDefinition(getLocale());
		}
		
		public ObjectClassDefinition getObjectClassDefinition(Locale locale) {
			final String localeStr = locale.toString();
			return (!this.isProvider) 
				? this.getObjectClassDefinitionFromMetaTypeService(localeStr)
				: this.getObjectClassDefinitionFromMetaTypeProvider(localeStr);
		}
		
		private ObjectClassDefinition getObjectClassDefinitionFromMetaTypeService(String locale) {
			final MetaTypeInformation mti = metaTypeService.getMetaTypeInformation(this.bundle);			
			return mti.getObjectClassDefinition(this.pid, locale);
		}
		
		private ObjectClassDefinition getObjectClassDefinitionFromMetaTypeProvider(String locale) {
			final ServiceReference[] serviceRefs = this.bundle.getRegisteredServices();
			if (serviceRefs != null) {
				for (ServiceReference ref : serviceRefs) {
					// a managed-service always has a PID
					String nextPID = (String) ref.getProperty(Constants.SERVICE_PID);
					if (nextPID!= null && nextPID.equals(this.pid)) {
						MetaTypeProvider mProvider = (MetaTypeProvider) context.getService(ref);
						return mProvider.getObjectClassDefinition(this.pid, locale);
					}
				}
			}
			return null;
		}
		
		public Object getPropertyValue(@SuppressWarnings("unchecked") Dictionary props, AttributeDefinition attribute) {
			if (attribute == null) throw new NullPointerException("Attribute definition is null");
			
			String propertyKey = attribute.getID();
			Object value = (props == null)?null:props.get(propertyKey);
			String[] defaultValues = attribute.getDefaultValue();
			
			if (value != null) {
				return value;
			} else if (defaultValues != null && defaultValues.length > 0){
				return (attribute.getCardinality()==0)?defaultValues[0]:defaultValues;
			} else {
				return null;
			}
		}
		
		public HashMap<String,Attribute> getAttributeMetadataMap() {
			final HashMap<String,Attribute> attrMetadata = new HashMap<String,Attribute>();
			final Metadata metadata = this.getObjectClassDefinition().getClass().getAnnotation(Metadata.class);
			if (metadata != null)
				for (final Attribute attr : metadata.value())
					attrMetadata.put(attr.id(), attr);
			return attrMetadata;
		}
	}
}
