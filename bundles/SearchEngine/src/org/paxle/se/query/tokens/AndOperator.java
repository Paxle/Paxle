package org.paxle.se.query.tokens;

import org.paxle.se.query.IParentToken;

public class AndOperator extends Operator implements IParentToken {
	
	public AndOperator() {
		super("and");
	}
	
	@Override
	public int getMinMatchCount() {
		return super.children.size();
	}
}
