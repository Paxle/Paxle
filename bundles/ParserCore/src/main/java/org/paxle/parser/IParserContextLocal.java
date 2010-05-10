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

package org.paxle.parser;

import javax.annotation.Nonnull;

/**
 * An interface allowing {@link ISubParser sub-parsers} to access the {@link IParserContext}.
 * <p/>
 * <i>Usage example (for OSGi DS):</i>
 * <pre><code>
 * &#064;Component
 * &#064;Service(ISubCrawler.class)
 * &#064;Property(name=ISubParser.PROP_MIMETYPES, value={"myMimeTypes"})
 * public class MyParser implements ISubParser {
 *    &#064;Reference
 *    protected IParserContextLocal ctxLocal;
 * 
 *    public IParserDocument parse(URI location, String charset, File content) {
 *       // getting the crawler-context bound to the current thread
 *       IParserContextLocal ctx = this.ctxLocal.getCurrentContext();
 *       
 *       // using the context e.g. to create a new parser-document
 *       IParserDocument doc = context.createDocument();
 *    }
 * }	
 * </code></pre>
 * <p/>
 * Alternatively to get a reference to the IParserContextLocal service via OSGi DS,
 * a {@link ISubParser} can also implement the {@link IParserContextAware} interface.
 * 
 * @since 0.1.14-SNAPSHOT
 */
public interface IParserContextLocal {
	public @Nonnull IParserContext getCurrentContext();
}
