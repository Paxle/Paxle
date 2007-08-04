package org.paxle.se.query.tokens;

import org.paxle.se.query.IParentToken;

public abstract class Operator extends MultiToken implements IParentToken {
	
	public Operator(String name) {
		super(name);
	}
}
