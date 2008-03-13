
package org.paxle.se.query.tokens;

public class OrOperator extends MultiToken {
	
	public OrOperator() {
		super("or");
	}
	
	public int getMinMatchCount() {
		return 1;
	}
}
