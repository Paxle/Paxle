package org.paxle.core.io.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
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
		Enumeration<URL> e = this.b.findEntries("OSGI-INF/l10n", resourceBundleBase + "*.properties",false);
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
				} else {
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
}
