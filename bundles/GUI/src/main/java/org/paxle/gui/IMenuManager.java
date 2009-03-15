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
package org.paxle.gui;

import java.util.Collection;
import java.util.ResourceBundle;

import org.paxle.gui.impl.MenuItem;

public interface IMenuManager {	
	public Collection<MenuItem> getMenuItemList();
	
	/**
	 * @param url the path or URL to the servlet the new {@link MenuItem} should link to
	 * @param name the name of the {@link MenuItem}, if this name starts with <code>%</code> the name will be translated using
	 * the specified resource-bundle.
	 * @param resourceBundleBaseName the {@link ResourceBundle} to translate the {@link MenuItem}-name
	 * @param loader the {@link ClassLoader} that should be used to load the {@link ResourceBundle}
	 */
	public void addItem(String url, String name, String resourceBundleBaseName, ClassLoader loader);
	
	/**
	 * @param name the name of the {@link MenuItem} to remove
	 */
	public void removeItem(String name);
}
