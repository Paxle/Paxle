package org.paxle.parser.html.impl.tags;

import org.htmlparser.tags.CompositeTag;

public class ItalicTag extends CompositeTag {
	
	private static final long serialVersionUID = 1L;
	
	private static final String[] mIds = { "I", "EM" };
	private static final String[] mEnders = { "I", "EM", "HTML", "BODY" };
	private static final String[] mEndTagEnders = { "P", "HTML", "BODY" };
	
	public ItalicTag() {  }
	
	@Override
	public String[] getIds() {
		return mIds;
	}
	
	@Override
	public String[] getEnders() {
		return mEnders;
	}
	
	@Override
	public String[] getEndTagEnders() {
		return mEndTagEnders;
	}

}
