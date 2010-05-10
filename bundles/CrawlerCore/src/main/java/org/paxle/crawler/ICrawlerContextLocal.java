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

import javax.annotation.Nonnull;


/**
 * An interface allowing {@link ISubCrawler sub-crawlers} to access the {@link ICrawlerContext}.
 * <p/>
 * <i>Usage example (for OSGi DS):</i>
 * <pre><code>
 * &#064;Component
 * &#064;Service(ISubCrawler.class)
 * &#064;Property(name=ISubCrawler.PROP_PROTOCOL, value={"myProtocol"})
 * public class MyCrawler implements ISubCrawler {
 *    &#064;Reference
 *    protected ICrawlerContextLocal ctxLocal;
 * 
 *    public ICrawlerDocument request(URI location) {
 *       // getting the crawler-context bound to the current thread
 *       ICrawlerContext ctx = this.ctxLocal.getCurrentContext();
 *       
 *       // using the context e.g. to create a new crawler-document
 *       ICrawlerDocument doc = context.createDocument();
 *    }
 * }	
 * </code></pre>
 * <p/>
 * Alternatively to get a reference to the ICrawlerContextLocal service via OSGi DS,
 * a {@link ISubCrawler} can also implement the {@link ICrawlerContextAware} interface.
 */
public interface ICrawlerContextLocal {
	public @Nonnull ICrawlerContext getCurrentContext();
}
