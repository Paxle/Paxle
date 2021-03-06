/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2010 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.desktop.impl;

import java.lang.reflect.Array;
import java.util.Hashtable;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.paxle.core.IMWComponent;
import org.paxle.core.doc.ICommand;
import org.paxle.core.prefs.IPropertiesStore;
import org.paxle.core.prefs.Properties;

public class ServiceManager {
	
	public static final int FRAMEWORK_BUNDLE_ID = 0;
	
	private final BundleContext context;
	
	public ServiceManager(BundleContext context) {
		this.context = context;
	}
	
	public void shutdownFramework() throws BundleException {
		String osgiFrameworkVendor = context.getProperty(Constants.FRAMEWORK_VENDOR);
		if (osgiFrameworkVendor.equalsIgnoreCase("Eclipse")) {
			// try to find an application launcher and shutdown
			Object app = this.getService("org.eclipse.osgi.service.runnable.ApplicationLauncher");
			if (app != null) {
				try {
					app.getClass().getMethod("shutdown", (Class[])null).invoke(app, (Object[])null);
					return;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}		
		
		final Bundle framework = this.context.getBundle(FRAMEWORK_BUNDLE_ID);
		if (framework != null) {
			framework.stop();
		}
	}
	
	public void restartFramework() throws BundleException {
		final Bundle framework = this.context.getBundle(FRAMEWORK_BUNDLE_ID);
		if (framework != null) {
			framework.update();
		}
	}
	
	public ServiceRegistration registerService(final Object service, final Hashtable<String,?> properties, final String cname) {
		return context.registerService(cname, service, properties);
	}
	
	public <E> ServiceRegistration registerService(final E service, final Hashtable<String,?> properties, final Class<? super E>... clazzes) {
		final String[] cnames = new String[clazzes.length];
		for (int i=0; i<clazzes.length; i++)
			cnames[i] = clazzes[i].getName();
		return context.registerService(cnames, service, properties);
	}
	
	public ServiceRegistration registerService(final Object service, final Hashtable<String,?> properties, final String[] cnames) {
		return context.registerService(cnames, service, properties);
	}
	
	public void addServiceListener(final ServiceListener listener) {
		try { addServiceListener(listener, null); } catch (InvalidSyntaxException e) { e.printStackTrace(); }
	}
	
	public void addServiceListener(final ServiceListener listener, final String filter) throws InvalidSyntaxException {
		context.addServiceListener(listener, filter);
	}
	
	public void removeServiceListener(final ServiceListener listener) {
		context.removeServiceListener(listener);
	}
	
	public Properties getServiceProperties() {
		final IPropertiesStore propstore = getService(IPropertiesStore.class);
		if (propstore == null)
			return null;
		return propstore.getProperties(this.context);
	}
	
	public String getProperty(String key) {
		return this.context.getProperty(key);
	}
	
	public Object getService(String name) {
		return getService(name, Object.class);
	}
	
	public <E> E getService(Class<E> service) {
		return getService(service.getName(), service);
	}
	
	public <E> E getService(String name, Class<E> service) {
		ServiceReference reference = this.context.getServiceReference(name);
		return (reference == null) ? null : getService(reference, service);
	}
	
	public <E> E getService(final ServiceReference ref, final Class<E> clazz) {
		final Object service = context.getService(ref);
		return clazz.cast(service);
	}
	
	public void ungetService(final ServiceReference ref) {
		context.ungetService(ref);
	}
	
	public boolean hasService(Class<?> service) {
		return hasService(service.getName());
	}
	
	public boolean hasService(Class<?> service, String query) throws InvalidSyntaxException {
		return hasService(service.getName(), query);
	}
	
	public boolean hasService(String name, String query) throws InvalidSyntaxException {
		try {
			return this.context.getServiceReferences(name, query) != null;
		} catch (InvalidSyntaxException e) {
			throw e;
		} catch (Exception e) {
			return false;
		}
	}
	
	public boolean hasService(String name) {
		try {
			return this.context.getServiceReference(name) != null;
		} catch (Exception e) {
			return false;
		}
	}
	
	public <E> E[] getServices(final Class<E> service) {
		try {
			return getServices(service, null);
		} catch (InvalidSyntaxException e) { e.printStackTrace(); }
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public <E> E[] getServices(Class<E> service, String query) throws InvalidSyntaxException {
		final ServiceReference[] references = this.context.getServiceReferences(service.getName(), query);
		if (references == null) return null;
		
		final E[] services = (E[])Array.newInstance(service, references.length);
		for (int i=0; i<references.length; i++)
			services[i] = getService(references[i], service);
		return services;
	}
	
	public ServiceReference[] getServiceReferences(final String clazz, final String filter) throws InvalidSyntaxException {
		return context.getServiceReferences(clazz, filter);
	}
	
    public Bundle[] getBundles() {
        return this.context.getBundles();
    }
    
    public Bundle getBundle() {
    	return context.getBundle();
    }
    
    public Bundle getBundle(long bundleID) {
        return this.context.getBundle(bundleID);
    }

	public void addBundleListener(final BundleListener bundleListener) {
		this.context.addBundleListener(bundleListener);
	}
	
	public void removeBundleListener(final BundleListener bundleListener) {
		this.context.removeBundleListener(bundleListener);
	}
	
	/* ========================================================================== *
	 * Convenience methods
	 * ========================================================================== */
	
	@SuppressWarnings("unchecked")
	private static final Class<IMWComponent> MWCOMP_CLASS = IMWComponent.class;
	
	@SuppressWarnings("unchecked")
	public IMWComponent<ICommand> getMWComponent(final MWComponents comp) {
		try {
			final IMWComponent<?>[] comps = getServices(MWCOMP_CLASS, comp.toQuery());
			if (comps != null && comps.length > 0)
				return (IMWComponent<ICommand>)comps[0];
		} catch (InvalidSyntaxException e) {
			Utilities.instance.showExceptionBox(e);
			e.printStackTrace();
		}
		return null; 
	}
	
	public boolean isServiceAvailable(MWComponents comp) {
		try {
			return hasService(MWCOMP_CLASS, comp.toQuery());
		} catch (InvalidSyntaxException e) {
			Utilities.instance.showExceptionBox(e);
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Contains conveninience-constants for locating the {@link IMWComponent}s of the bundles
	 * CrawlerCore, ParserCore and Indexer.
	 */
	public static enum MWComponents {
		/** Refers to the {@link IMWComponent} provided by the bundle "CrawlerCore" */
		CRAWLER,
		/** Refers to the {@link IMWComponent} provided by the bundle "ParserCore" */
		PARSER,
		/** Refers to the {@link IMWComponent} provided by the bundle "Indexer" */
		INDEXER
		
		;
		
		/**
		 * @return the {@link org.osgi.framework.Constants#BUNDLE_SYMBOLICNAME symbolic-name}
		 *         of this {@link IMWComponent}, which also denotes the value for
		 *         {@link IMWComponent#COMPONENT_ID}
		 */
		public String getID() {
			return String.format("org.paxle.%s", name().toLowerCase());
		}
		
		/**
		 * @return a LDAP-style expression which matches this components's
		 *         {@link IMWComponent#COMPONENT_ID}
		 */
		public String toQuery() {
			return toQuery(IMWComponent.COMPONENT_ID);
		}
		
		/**
		 * @param key the key of the resulting expression
		 * @return a LDAP-style expression which matches the given <code>key</code> to this
		 *         component's {@link IMWComponent#COMPONENT_ID}
		 * @see #getID()
		 */
		public String toQuery(final String key) {
			return String.format("(%s=%s)", key, getID());
		}
		
		/**
		 * @return the human-readable name of this component, such as "Crawler", "Parser" or
		 *         "Indexer" 
		 */
		@Override
		public String toString() {
			return Character.toUpperCase(name().charAt(0)) + name().substring(1).toLowerCase();
		}
		
		/**
		 * @param id the {@link #getID() ID} of the component's representative constant to return
		 * @return the {@link MWComponents}-constant whose ID is given by <code>id</code> or <code>null</code>
		 *         if no such component is known 
		 * @see #getID()
		 */
		public static MWComponents valueOfID(final String id) {
			return valueOf(id.substring("org.paxle.".length()).toUpperCase());
		}
		
		/**
		 * @param name the {@link #toString() name} of the component's representative constant to return
		 * @return the {@link MWComponents}-constant whose human-readable name is given by <code>name</code>
		 *         or <code>null</code> if no such component is known
		 * @see #toString()
		 */
		public static MWComponents valueOfHumanReadable(final String name) {
			return valueOf(name.toUpperCase());
		}
		
		/**
		 * @return an array of the human-readable {@link #toString() names} of all known {@link IMWComponent}s
		 *         in the order {@link #values()} returns the {@link Enum#valueOf(Class, String)}-constants
		 * @see #values()
		 * @see #toString() 
		 */
		public static String[] humanReadableNames() {
			final MWComponents[] comps = values();
			final String[] compStrs = new String[comps.length];
			for (int i=0; i<comps.length; i++)
				compStrs[i] = comps[i].toString();
			return compStrs;
		}
	}
}
