package org.paxle.parser.html.impl.tags;

import org.htmlparser.tags.LinkTag;


@SuppressWarnings("serial")
public class FixedLink extends LinkTag {
	
    private static final String[] mEnders = new String[] {"A", "P", "DIV", "TD", "TR", "FORM", "LI", "DL", "DT", "DD"};
	
	public FixedLink() {
	}
	
	@Override
	public String[] getEnders() {
		return mEnders;
	}
}
