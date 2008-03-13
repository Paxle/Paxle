
package org.paxle.se.query.tokens;

public class AndOperator extends MultiToken {
	
	public AndOperator() {
		super("and");
	}
	
	public int getMinMatchCount() {
		return super.children.size();
	}
}
