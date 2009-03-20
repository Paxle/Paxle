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
package org.paxle.gui.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.SortedMap;
import java.util.TreeMap;

import org.paxle.gui.IMenuManager;

public class MenuItem implements Comparable<MenuItem> {
	public static final String DEFAULT_RESOURCE_BUNDLE = "/OSGI-INF/l10n/menu";
	
	/**
	 * A map of all currently registeres {@link MenuItem menu-items}. <br/>
	 * Key = the name of the menu-item<br/>
	 * value = the {@link MenuItem} object
	 */
	protected SortedMap<String,MenuItem> items = new TreeMap<String, MenuItem>();		
	
	protected int pos = IMenuManager.DEFAULT_MENU_POS;
	protected String url = null;
	protected String name = null;
	protected String resourceBundleBase;
	protected ClassLoader resourceBundleLoader;
	protected URL iconURL;
	
	public MenuItem(String url, String name) {
		this(url, name, null, null, IMenuManager.DEFAULT_MENU_POS, null);
	}
	
	public MenuItem(String url, String name, String resourceBundleBaseName, ClassLoader loader, int pos, URL iconURL) {
		// if (name == null) throw new NullPointerException("The menu-item name must not be null");
		this.url = url;
		this.name = name;
		if (this.name != null) this.name = name.replaceAll("//", "/"); // unescaping double-slash
		this.resourceBundleBase = resourceBundleBaseName;
		this.resourceBundleLoader = loader;
		this.pos = pos;
		this.iconURL = iconURL;
	}
	
	public String getUrl() {
		return (this.url==null)?null:this.url;
	}
	
	private void setUrl(String url) {
		this.url = url;
	}
	
	public URL getIconURL() {
		return this.iconURL;
	}
	
	public String getName() {
		return this.getName("en");
	}
	
	public String getName(String localeStr) {
		final Locale locale = (localeStr==null) ? Locale.ENGLISH : new Locale(localeStr);
		return this.getName(locale);
	}
	
	public String getName(Locale locale) {
		return this.getName(Arrays.asList(new Locale[]{locale}));
	}
	
	public String getName(Enumeration<Locale> locales) {
		List<Locale> localeList = Collections.list(locales);
		return this.getName(localeList);
	}
	
	private String getName(List<Locale> locales) {
		if (this.name == null) return null;
		else if (!this.name.startsWith("%") || this.name.length() == 0 || this.resourceBundleBase == null) return this.name;
				
		String translatedName = null;
		
		// trying to load the translation using the specified resource-bundle
		if (this.resourceBundleBase != null) {
			translatedName = this.getName(locales, this.resourceBundleBase, this.resourceBundleLoader);
		}
		
		// trying to load the translation from the default menu resource-bundle
		if (translatedName == null) {
			translatedName = this.getName(locales, DEFAULT_RESOURCE_BUNDLE, this.getClass().getClassLoader());
		}
		
		// returning the key as value
		if (translatedName == null) {
			translatedName = this.name.substring(1);
		}
		
		return translatedName;
	}
	
	private String getName(List<Locale> locales, String bundleBase, ClassLoader loader) {
		try {
			ResourceBundle rb = null;
			Iterator<Locale> localeEnum = locales.iterator();
						
			while (localeEnum.hasNext()) {
				Locale locale = localeEnum.next();
				rb = (loader == null)
				   ? ResourceBundle.getBundle(bundleBase, locale)
				   : ResourceBundle.getBundle(bundleBase, locale, loader);
				
				if (rb.getLocale().equals(locale)) break;
			}
			if (rb == null) return null;

			final String resourceKey = this.name.substring(1);
			final String translatedName = rb.getString(resourceKey);
			return translatedName;
		} catch (MissingResourceException e) {
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Collection<MenuItem> getMenuItemList() {
		ArrayList<MenuItem> temp = new ArrayList<MenuItem>(items.values());
		Collections.sort(temp);
		return temp;
	}	
	
	public void addItem(String url, String name, int pos, URL iconURL) {
		this.addItem(url, name, DEFAULT_RESOURCE_BUNDLE, this.getClass().getClassLoader(), pos, iconURL);
	}
	
	public void addItem(String url, String name, String resourceBundleBaseName, ClassLoader loader, int pos, URL iconURL) {
		if (name == null) throw new NullPointerException("The menu-item name must not be null");		
		String[] nameParts = name.split("(?<!/)/(?!/)"); 
		this.addItem(url, nameParts, resourceBundleBaseName, loader, pos, iconURL);
	}
	
	public void addItem(String url, String[] nameParts, String resourceBundleBaseName, ClassLoader loader, int pos, URL iconURL) {
		MenuItem parent = this;
		
		for (int i=0; i < nameParts.length; i++) {
			MenuItem currentItem = null;
			String itemName = nameParts[i];
			
			if (parent.hasItem(itemName)) {
				currentItem = parent.getItem(itemName);
				
				/* 
				 * If the url was not already set for this item (e.g. if a subitem was registered
				 * previously to the parent-item, then we set it now.
				 */
				if (currentItem.getUrl() == null && (i == nameParts.length-1)) {
					currentItem.setUrl(url);
				}
			} else {
				currentItem = new MenuItem(
						// the path to the servlet
						(i==nameParts.length-1)?url:null,
						// the menu-item name
						itemName,
						// resource-bundle name and classloader
						resourceBundleBaseName, 
						loader,
						pos,
						iconURL
				);
				parent.addItem(itemName, currentItem);
			}
			
			parent = currentItem;
		}
	}	
	
	public void addItem(String name, MenuItem subItem) {
		this.items.put(name, subItem);
	}	
	
	public void removeItem(String name) {
		if (name == null) throw new NullPointerException("The menu-item name must not be null");		
		String[] nameParts = name.split("(?<!/)/(?!/)");
		this.removeItem(nameParts, 0);
	}
	
	private void removeItem(String[] nameParts, int idx) {
		if (idx == nameParts.length-1) {
			MenuItem theItem = this.items.get(nameParts[idx]);
			if (!theItem.hasSubItems()) this.items.remove(nameParts[idx]);
			theItem.setUrl(null);
		} else {
			MenuItem parent = this.items.get(nameParts[idx]);
			if (parent == null) throw new IllegalStateException("No menu-item found with name: " + nameParts[idx]);
			parent.removeItem(nameParts,idx+1);
			if (!parent.hasSubItems()) this.items.remove(nameParts[idx]);
		}	
	}
	
	public boolean hasItem(String name) {
		return this.items.containsKey(name);
	}
	
	public MenuItem getItem(String name) {
		return this.items.get(name);
	}
	
	public boolean hasSubItems() {
		return this.items.size() > 0;
	}

	public int compareTo(MenuItem o) {
		return this.pos - o.pos;
	}
}
