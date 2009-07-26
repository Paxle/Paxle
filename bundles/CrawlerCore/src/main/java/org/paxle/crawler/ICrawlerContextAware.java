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
package org.paxle.crawler;


/**
 * {@link ISubCrawler Crawlers} that want to be aware of the {@link ICrawlerContext} should 
 * implement this interface. This will give them access to the {@link ICrawlerContext crawler-context} object.
 * <p/>
 * <i>Usage example:</i>
 * <pre><code>
 * public class MyCrawler implements ISubCrawler, ICrawlerContextAware {
 *	private ICrawlerContextLocal contextLocal;
 *  
 *	public void setCrawlerContextLocal(ICrawlerContextLocal contextLocal) {
 *		this.context = context;
 *	}
 *   
 *	public ICrawlerDocument request(URI requestUri) {
 *		// getting the crawler-context
 *		ICrawlerContext context = this.contextLocal.get();
 *
 *		// using the context e.g. to create a new crawler-document
 *		ICrawlerDocument doc = context.createDocument();
 *	}
 *   
 * }
 * </code></pre>
 */
public interface ICrawlerContextAware {
	public void setCrawlerContextLocal(ICrawlerContextLocal contextLocal);
}
