package org.paxle.parser.html.impl.tags;

import org.htmlparser.tags.CompositeTag;

public class AddressTag extends CompositeTag {
	
	private static final long serialVersionUID = 1L;
	
	private static final String[] mIds = { "ADDRESS" };
	private static final String[] mEnders = { "ADDRESS", "HTML", "BODY" };
	private static final String[] mEndTagEnders = { "HTML", "BODY" };
	
	public AddressTag() {  }
	
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
