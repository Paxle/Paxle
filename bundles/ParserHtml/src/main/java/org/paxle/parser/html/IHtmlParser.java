
package org.paxle.parser.html;

import org.paxle.parser.ISubParser;

public interface IHtmlParser extends ISubParser {
	
	public static final String PROP_VALIDATE_META_ROBOTS_NOINDEX = IHtmlParser.class.getName() + ".validateMetaRobots.noindex";
	public static final String PROP_VALIDATE_META_ROBOTS_NOFOLLOW = IHtmlParser.class.getName() + ".validateMetaRobots.nofollow";
	public static final String PROP_HCARD_ENABLE = IHtmlParser.class.getName() + ".hcard.enable";
}
