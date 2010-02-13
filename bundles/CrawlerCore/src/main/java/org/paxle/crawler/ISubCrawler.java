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

package org.paxle.crawler;

import java.net.URI;

import javax.annotation.Nonnull;

import org.paxle.core.doc.ICrawlerDocument;


/**
 * The interface that must be implemented by all sub-crawlers.
 * <hr/>
 * <i>Usage Example (using BundleContext.registerService(...)):</i>
 * <pre><code>
 * public class Activator implements BundleActivator {
 *    public void start(BundleContext bc) throws Exception {
 *       // creating the crawler
 *       ISubCrawler myCrawler = new MyCrawler();
 * 
 *       // setting the crawler properties
 *       Hashtable<String,Object> propsCrawler = new Hashtable<String, Object>();
 *       propsCrawler.put(Constants.SERVICE_PID, MyCrawler.class.getName());
 *       propsCrawler.put(ISubCrawler.PROP_PROTOCOL, new String[]{"myProtocol"});	  
 * 
 *       // registering the crawler as a service to the framework
 *       bc.registerService(new String[]{ISubCrawler.class.getName()}, myCrawler, propsCrawler);
 *    }
 * }
 * </code></pre>
 * <hr/>
 * <i>Usage Example (using OSGi DS):</i>
 * <pre><code>
 * &#064;Component
 * &#064;Service(ISubCrawler.class)
 * &#064;Property(name=ISubCrawler.PROP_PROTOCOL, value={"myProtocol"})
 * public class MyCrawler implements ISubCrawler {
 * 
 *    public ICrawlerDocument request(URI location) {
 *       // do crawling here ...
 *    }
 * }
 * </code></pre>
 */
public interface ISubCrawler {
	/*
	 * A list of service properties
	 */
	/** 
	 * The network protocols supported by the sub-crawler. <br/>
	 * The value of this property must
	 * be of type String[].
	 */
	public static final String PROP_PROTOCOL = "Protocol";
	
	public ICrawlerDocument request(@Nonnull URI requestUri);
}
