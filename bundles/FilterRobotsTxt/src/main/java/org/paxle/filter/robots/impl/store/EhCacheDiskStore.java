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

package org.paxle.filter.robots.impl.store;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.paxle.filter.robots.impl.rules.RobotsTxt;

@Component(immediate=true, metatype=false)
@Service(IRuleStore.class)
public class EhCacheDiskStore implements IRuleStore {
	private static String DB_PATH = "robots-db";
	
	/**
	 * A separate ehCache cache-manager
	 */
	private CacheManager cm;
	
	/**
	 * An ehCache instance that we use as disk-store
	 */
	private Cache store;
	
	@Activate
	protected void activate(ComponentContext context) {
		// getting data path
		final String dataPath = System.getProperty("paxle.data") + File.separatorChar + DB_PATH;

		// getting config files
		@SuppressWarnings("unchecked")
		Enumeration<URL> configFileEnum = context.getBundleContext().getBundle().findEntries("/resources/", "ehCache.xml", true);
		URL configFile = (configFileEnum.hasMoreElements()) ? configFileEnum.nextElement() : null;		
		
		// init
		this.init(dataPath, configFile);
	}
	
	void init(String dataPath, URL configFile) {
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
