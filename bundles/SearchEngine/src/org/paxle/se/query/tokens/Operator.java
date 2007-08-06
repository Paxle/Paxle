package org.paxle.se.query.tokens;

public abstract class Operator extends MultiToken {
	
	public Operator(String name) {
		super(name);
	}
	
	public abstract int getMinMatchCount();
}
