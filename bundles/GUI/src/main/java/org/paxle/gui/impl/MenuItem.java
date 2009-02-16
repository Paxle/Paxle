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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.ResourceBundle;

public class MenuItem {
	public static final String DEFAULT_RESOURCE_BUNDLE = "/OSGI-INF/l10n/menu";
	
	/**
	 * A map of all currently registeres {@link MenuItem menu-items}. <br/>
	 * Key = the name of the menu-item<br/>
	 * value = the {@link MenuItem} object
	 */
	protected LinkedHashMap<String,MenuItem> items = new LinkedHashMap<String,MenuItem>();		
	
	protected String url = null;
	protected String name = null;
	protected ServletManager sManager;
	protected String resourceBundleBase;
	protected ClassLoader resourceBundleLoader;
	
	public MenuItem(ServletManager sManager, String url, String name) {
		this(sManager, url, name, null, null);
	}
	
	public MenuItem(ServletManager sManager, String url, String name, String resourceBundleBaseName, ClassLoader loader) {
		// if (name == null) throw new NullPointerException("The menu-item name must not be null");
		this.sManager = sManager;
		this.url = url;
		this.name = name;
		this.resourceBundleBase = resourceBundleBaseName;
		this.resourceBundleLoader = loader;
	}
	
	public String getUrl() {
		return (this.url==null)?null:this.sManager.getFullAlias(this.url);
	}
	
	private void setUrl(String url) {
		this.url = url;
	}
	
	public String getName() {
		return this.getName("en");
	}
	
	public String getName(String localeStr) {
		if (this.name == null) return null;
		else if (!this.name.startsWith("%") || this.name.length() == 0 || this.resourceBundleBase == null) return this.name;
				
		String translatedName = null;
		
		// trying to load the translation using the specified resource-bundle
		if (this.resourceBundleBase != null) {
			translatedName = this.getName(localeStr, this.resourceBundleBase, this.resourceBundleLoader);
		}
		
		// trying to load the translation from the default menu resource-bundle
		if (translatedName == null) {
			translatedName = this.getName(localeStr, DEFAULT_RESOURCE_BUNDLE, this.getClass().getClassLoader());
		}
		
		// returning the key as value
		if (translatedName == null) {
			translatedName = this.name.substring(1);
		}
		
		return translatedName;
	}
	
	private String getName(String localeStr, String bundleBase, ClassLoader loader) {
		try {
			final Locale locale = (localeStr==null) ? Locale.ENGLISH : new Locale(localeStr);
			final ResourceBundle rb = (loader == null)
								    ? ResourceBundle.getBundle(bundleBase, locale)
								    : ResourceBundle.getBundle(bundleBase, locale, loader);

			final String resourceKey = this.name.substring(1);
			final String translatedName = rb.getString(resourceKey);
			return translatedName;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Collection<MenuItem> getMenuItemList() {
		return items.values();
	}	
	
	public void addItem(String url, String name) {
		this.addItem(url, name, DEFAULT_RESOURCE_BUNDLE, this.getClass().getClassLoader());
	}
	
	public void addItem(String url, String name, String resourceBundleBaseName, ClassLoader loader) {
		if (name == null) throw new NullPointerException("The menu-item name must not be null");		
		String[] nameParts = name.split("(?<!/)/(?!/)"); 
		this.addItem(url, nameParts, resourceBundleBaseName, loader);
	}
	
	public void addItem(String url, String[] nameParts, String resourceBundleBaseName, ClassLoader loader) {
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
						// the servlet-manager
						this.sManager,
						// the path to the servlet
						(i==nameParts.length-1)?url:null,
						// the menu-item name
						itemName,
						// resource-bundle name and classloader
						resourceBundleBaseName, 
						loader
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
		this.items.remove(name);
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
}
