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
package org.paxle.core.io.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.paxle.core.io.IResourceBundleTool;

public class ResourceBundleTool implements IResourceBundleTool {
	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());
	
	private final Bundle b;
	
	public ResourceBundleTool(Bundle b) {
		this.b = b;
	}
	
	/**
	 * @param resourceBundleBase
	 * @return a list of {@link URL} pointing to {@link ResourceBundle resource-bundles} available for the given base-name
	 */
	
	public List<URL> getLocaleURL(String resourceBundleBase) {
		return this.getLocaleURL(this.b, resourceBundleBase);
	}
	
	@SuppressWarnings("unchecked")
	public List<URL> getLocaleURL(Bundle osgiBundle, String resourceBundleBase) {
		if (osgiBundle == null) throw new NullPointerException("The osgi-bundle was null");
		else if (resourceBundleBase == null) throw new NullPointerException("The resource-bundle base-name was null");
			
		// calculating the resource-bundle location
		String localizationLocation = IResourceBundleTool.LOCALIZATION_LOCATION_DEFAULT;
		if (resourceBundleBase.contains("/")) {
			localizationLocation = resourceBundleBase.substring(0,resourceBundleBase.lastIndexOf('/'));
			resourceBundleBase = resourceBundleBase.substring(resourceBundleBase.lastIndexOf('/') + 1);
		}
		
		// find all resource-bundle files for the given base-name
		final Enumeration<URL> e = osgiBundle.findEntries(localizationLocation, resourceBundleBase + "*.properties",false);
		final List<URL> resourceBundleURLs = (e==null)?Collections.EMPTY_LIST:Collections.list(e);
		return resourceBundleURLs;
	}
	
	public String[] getLocaleArray(String resourceBundleBase, Locale defaultLocale) {
		if (resourceBundleBase == null) throw new NullPointerException("The resource-bundle base-name was null");
		
		return this.getLocaleArray(this.b, resourceBundleBase, defaultLocale);
	}
	
	public String[] getLocaleArray(Bundle osgiBundle, String resourceBundleBase, Locale defaultLocale) {
		if (osgiBundle == null) throw new NullPointerException("The osgi-bundle was null");
		else if (resourceBundleBase == null) throw new NullPointerException("The resource-bundle base-name was null");		
		
		final List<String> localeList = this.getLocaleList(osgiBundle, resourceBundleBase, defaultLocale);
		return localeList.toArray(new String[localeList.size()]);
	}
	
	/**
	 * @param defaultLocale the locale of the default {@link ResourceBundle} file
	 * @return a list of locale strings available for the given base-name
	 */
	public List<String> getLocaleList(String resourceBundleBase, Locale defaultLocale) {
		if (resourceBundleBase == null) throw new NullPointerException("The resource-bundle base-name was null");		
		
		return this.getLocaleList(this.b, resourceBundleBase, defaultLocale);
	}	
	
	public List<String> getLocaleList(Bundle osgiBundle, String resourceBundleBase, Locale defaultLocale) {
		if (osgiBundle == null) throw new NullPointerException("The osgi-bundle was null");
		else if (resourceBundleBase == null) throw new NullPointerException("The resource-bundle base-name was null");
		
		// reg.exp to extract the locale-string
		final Pattern pattern = Pattern.compile("[^_]+(_\\w+)*\\.properties");
		
		// loop through all files and determine the locale-strings
		ArrayList<String> locales = new ArrayList<String>();
		for (URL resourceBundle : this.getLocaleURL(osgiBundle, resourceBundleBase)) {
			String file = resourceBundle.getFile();
			if (file.lastIndexOf('/') != -1) {
				file = file.substring(file.lastIndexOf('/'));
			}
			
			final Matcher matcher = pattern.matcher(file);
			while (matcher.find()) {
				String localeString = matcher.group(1);
				if (localeString == null && defaultLocale != null) {
					// language of the default resource-bundle file
					localeString = defaultLocale.toString();
				} else if (localeString != null) {
					// trim first "_"
					localeString = localeString.substring(1);
				}
				
				if (localeString != null) {
					locales.add(localeString);
				}
			}
		}
		
		return locales;
	}
	
	public ResourceBundle getLocalization(String resourceBundleBase, String localeStr) {
		if (resourceBundleBase == null) throw new NullPointerException("The resource-bundle base-name was null");
		
		return this.getLocalization(this.b, resourceBundleBase, localeStr);
	}
	
	public ResourceBundle getLocalization(Bundle osgiBundle, String resourceBundleBase, String localeStr) {
		if (osgiBundle == null) throw new NullPointerException("The osgi-bundle was null");
		else if (resourceBundleBase == null) throw new NullPointerException("The resource-bundle base-name was null");		
		
		final Locale locale = (localeStr==null) ? this.getDefaultLocale() : new Locale(localeStr);
		return this.getLocalization(osgiBundle, resourceBundleBase, locale);
	}
	
	public ResourceBundle getLocalization(String resourceBundleBase, Locale locale) {
		if (resourceBundleBase == null) throw new NullPointerException("The resource-bundle base-name was null");		
		
		return this.getLocalization(this.b, resourceBundleBase, locale);
	}
	
	public ResourceBundle getLocalization(Bundle osgiBundle, String resourceBundleBase, Locale locale)  {
		if (osgiBundle == null) throw new NullPointerException("The osgi-bundle was null");
		else if (resourceBundleBase == null) throw new NullPointerException("The resource-bundle base-name was null");
		
		// if locale is null we are using the default locale
		if (locale == null) locale = this.getDefaultLocale();

		// calculating the directory to use
		// calculating the resource-bundle location
		String localizationLocation = IResourceBundleTool.LOCALIZATION_LOCATION_DEFAULT;
		if (resourceBundleBase.contains("/")) {
			localizationLocation = resourceBundleBase.substring(0,resourceBundleBase.lastIndexOf('/'));
			resourceBundleBase = resourceBundleBase.substring(resourceBundleBase.lastIndexOf('/') + 1);
		}
		
		ResourceBundle bundle = null;		
		try {
			// loop through all variants to find a matching resourcebundle 
			final Iterator<String> variants = this.getLocaleVariants(locale.toString());
			while(variants.hasNext()) {
				final String variant = variants.next();
				final String propFile = resourceBundleBase + (variant.equals("") ? variant : "_" + variant) + ".properties";
				
				@SuppressWarnings("unchecked")
				final Enumeration<URL> e = osgiBundle.findEntries(localizationLocation, propFile,false);
				if (e != null && e.hasMoreElements()) {
					final InputStream in = e.nextElement().openStream();
					try {
						bundle = new OsgiResourceBundle(in, variant);
					} finally {
						in.close();
					}
				}
				
				// TODO: should we append parents next?
				if (bundle != null) break;
			}
		} catch (IOException e) {
			this.logger.error(String.format(
					"Unexpected '%s' while loading the resource-bundle for '%s' and locale '%s'.",
					e.getClass().getName(),
					resourceBundleBase,
					locale
			),e);
		}
		
		return bundle;
	}
	
	private Locale getDefaultLocale() {
		return Locale.ENGLISH;
	}
	
	@Nonnull Iterator<String> getLocaleVariants(String localeString) {
		final LinkedList<String> localeStrings = new LinkedList<String>();
		localeStrings.add(0, "");
		
		final String[] parts = localeString.split("_");
		final StringBuilder buf = new StringBuilder();
		
		for(String part : parts) {
			if (buf.length() > 0) buf.append("_");
			buf.append(part);
			localeStrings.add(0,buf.toString());
		}
		
		return localeStrings.iterator();
	}

	private class OsgiResourceBundle extends PropertyResourceBundle {
		private String variant;
		
		public OsgiResourceBundle(InputStream stream,String variant) throws IOException {
			super(stream);
			this.variant = variant;
		}
		
		@Override
		public Locale getLocale() {
			return new Locale(this.variant);
		}
	}
}
