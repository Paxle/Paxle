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
