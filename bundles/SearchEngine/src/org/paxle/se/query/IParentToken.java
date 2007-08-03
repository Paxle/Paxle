package org.paxle.se.query;


public interface IParentToken extends IToken {
	
	public void addToken(IToken child);
	public IToken[] children();
}
