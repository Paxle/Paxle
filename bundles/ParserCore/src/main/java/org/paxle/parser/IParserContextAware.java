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
