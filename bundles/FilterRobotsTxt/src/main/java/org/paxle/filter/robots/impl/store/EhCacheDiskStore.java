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

package org.paxle.filter.robots.impl.store;

import java.io.IOException;
import java.net.URL;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;

import org.paxle.filter.robots.impl.rules.RobotsTxt;

public class EhCacheDiskStore implements IRuleStore {

	/**
	 * A separate ehCache cache-manager
	 */
	private final CacheManager cm;
	
	/**
	 * An ehCache instance that we use as disk-store
	 */
	private final Cache store;
	
	public EhCacheDiskStore(String dataPath, URL configFile) {
		// reading configuration
		Configuration config = ConfigurationFactory.parseConfiguration(configFile);
		config.getDiskStoreConfiguration().setPath(dataPath);
		
		// init cache
		this.cm = new CacheManager(config);
		this.store = this.cm.getCache("robotsTxt.store");
	}

	public RobotsTxt read(String hostPort) throws IOException {
		Element e = this.store.get(hostPort);
		return (RobotsTxt) (e == null ? null : e.getValue());
	}
	
	public void write(RobotsTxt robotsTxt) throws IOException {
		Element e = new Element(robotsTxt.getHostPort(),robotsTxt);
		store.put(e);
	}	

	public int size() {
		return this.store.getSize();
	}	
	
	public void close() throws IOException {
		this.cm.shutdown();
	}
}
