package org.paxle.se.query.tokens;

public abstract class OrOperator extends MultiToken {
	
	public OrOperator() {
		super("or");
	}
	
	public int getMinMatchCount() {
		return 1;
	}
}
