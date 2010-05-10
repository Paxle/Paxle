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

/**
 * {@link ISubParser Parsers} that want to be aware of the {@link IParserContext} should 
 * implement this interface. This will give them access to the {@link IParserContext parser-context} object.
 * <p/>
 * <i>Usage example:</i>
 * <pre><code>
 * public class MyParser implements ISubParser, IParserContextAware {
 *	private IParserContextLocal contextLocal;
 *  
 *	public void setParserContextLocal(IParserContextLocal contextLocal) {
 *		this.contextLocal = contextLocal;
 *	}
 *   
 *	public IParserDocument parse(URI location, String charset, File content) {
 *		// getting the current parser-context
 *		IParserContext context = this.contextLocal.getCurrentContext();
 *
 *		// using the context e.g. to create a new parser-document
 *		IParserDocument doc = context.createDocument();
 *
 *	}
 * }
 * </code></pre>
 * 
 * @since 0.1.14-SNAPSHOT
 */
public interface IParserContextAware {
	public void setParserContextLocal(IParserContextLocal contextLocal);
}
