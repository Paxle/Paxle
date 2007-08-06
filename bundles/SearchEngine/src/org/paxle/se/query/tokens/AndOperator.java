package org.paxle.se.query.tokens;

public abstract class AndOperator extends MultiToken {
	
	public AndOperator() {
		super("and");
	}
	
	public int getMinMatchCount() {
		return super.children.size();
	}
}
