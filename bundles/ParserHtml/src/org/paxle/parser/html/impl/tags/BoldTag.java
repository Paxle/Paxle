package org.paxle.parser.html.impl.tags;

import org.htmlparser.tags.CompositeTag;

public class BoldTag extends CompositeTag {
	
	private static final long serialVersionUID = 1L;
	
	private static final String[] mIds = { "B", "STRONG" };
	private static final String[] mEnders = { "B", "STRONG", "HTML", "BODY" };
	private static final String[] mEndTagEnders = { "P", "HTML", "BODY" };
	
	public BoldTag() {  }
	
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
