/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
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

public class MenuItem {
	private String url = null;
	private String name = null;
	private ServletManager sManager;
	
	public MenuItem(ServletManager sManager, String url, String name) {
		this.sManager = sManager;
		this.url = url;
		this.name = name;
	}
	
	public String getUrl() {
		return this.sManager.getFullAlias(this.url);
	}
	
	public String getName() {
		return this.name;
	}
	
	public static MenuItem newInstance(ServletManager sManager, String url, String name) {
		return new MenuItem(sManager, url,name);
	}
	
	@Override
	public int hashCode() {
		return this.url.hashCode();
	}
}
