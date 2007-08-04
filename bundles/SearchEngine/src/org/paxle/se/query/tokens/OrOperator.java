package org.paxle.se.query.tokens;

import org.paxle.se.query.IParentToken;

public class OrOperator extends Operator implements IParentToken {
	
	public OrOperator() {
		super("or");
	}
	
	public int getMinMatchCount() {
		return 1;
	}
}
