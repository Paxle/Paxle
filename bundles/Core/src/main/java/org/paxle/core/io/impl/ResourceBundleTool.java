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
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.framework.Bundle;
import org.paxle.core.io.IResourceBundleTool;

public class ResourceBundleTool implements IResourceBundleTool {
	private final Bundle b;
	
	public ResourceBundleTool(Bundle b) {
		this.b = b;
	}
	
	/**
	 * @param resourceBundleBase
	 * @return a list of {@link URL} pointing to {@link ResourceBundle resource-bundles} available for the given base-name
	 */
	@SuppressWarnings("unchecked")
	public List<URL> getLocaleURL(String resourceBundleBase) {
		// find all resource-bundle files for the given base-name
		Enumeration<URL> e = this.b.findEntries(LOCALIZATION_LOCATION_DEFAULT, resourceBundleBase + "*.properties",false);
		List<URL> resourceBundleURLs = (e==null)?Collections.EMPTY_LIST:Collections.list(e);
		return resourceBundleURLs;
	}
	
	public String[] getLocaleArray(String resourceBundleBase, Locale defaultLocale) {
		List<String> localeList = this.getLocaleList(resourceBundleBase, defaultLocale);
		return localeList.toArray(new String[localeList.size()]);
	}
	
	/**
	 * @param defaultLocale the locale of the default {@link ResourceBundle} file
	 * @return a list of locale strings available for the given base-name
	 */
	public List<String> getLocaleList(String resourceBundleBase, Locale defaultLocale) {
		if (resourceBundleBase == null) return Collections.emptyList();
		
		// reg.exp to extract the locale-string
		Pattern pattern = Pattern.compile("[^_]+(_\\w+)*\\.properties");
		
		// loop through all files and determine the locale-strings
		ArrayList<String> locales = new ArrayList<String>();
		for (URL resourceBundle : this.getLocaleURL(resourceBundleBase)) {
			String file = resourceBundle.getFile();
			if (file.lastIndexOf('/') != -1) {
				file = file.substring(file.lastIndexOf('/'));
			}
			
			Matcher matcher = pattern.matcher(file);
			while (matcher.find()) {
				String localeString = matcher.group(1);
				if (localeString == null && defaultLocale != null) {
					// language of the default resourcebundle file
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
	
	public ResourceBundle getLocalization(String resourceBundleBase, String localeStr) throws MissingResourceException {
		final Locale locale = (localeStr==null) ? this.getDefaultLocale() : new Locale(localeStr);
		return this.getLocalization(resourceBundleBase, locale);
	}
	
	@SuppressWarnings("unchecked")
	public ResourceBundle getLocalization(String resourceBundleBase, Locale locale) throws MissingResourceException {
		if (locale == null) locale = this.getDefaultLocale();

		String localizationLocation = LOCALIZATION_LOCATION_DEFAULT;
		
		ResourceBundle bundle = null;		
		try {
			final Iterator<String> variants = this.getLocaleVariants(locale.toString());
			while(variants.hasNext()) {
				final String variant = variants.next();
				final String propFile = resourceBundleBase + (variant.equals("") ? variant : "_" + variant) + ".properties";
				final Enumeration<URL> e = this.b.findEntries(localizationLocation, propFile,false);
				if (e != null && e.hasMoreElements()) {
					InputStream in = e.nextElement().openStream();
					try {
						bundle = new PropertyResourceBundle(in);
					} finally {
						in.close();
					}
				}
				
				// TODO: should we append parents next?
				if (bundle != null) break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return bundle;
	}
	
	private Locale getDefaultLocale() {
		return Locale.ENGLISH;
	}
	
	Iterator<String> getLocaleVariants(String localeString) {
		final LinkedList<String> localeStrings = new LinkedList<String>();
		localeStrings.add(0, "");
		
		final String[] parts = localeString.split("_");
		final StringBuilder buf = new StringBuilder();
		
		for(String part : parts) {
			if (buf.length() > 0) buf.append("_");
			buf.append(part);
			localeStrings.add(0,buf.toString());
		}
		
		localeStrings.add("");		
		return localeStrings.iterator();
	}

}
