package org.paxle.se.query.tokens;

import java.util.LinkedList;

import org.paxle.se.query.IParentToken;
import org.paxle.se.query.IToken;

public class MultiToken implements IParentToken {
	
	protected final LinkedList<IToken> children = new LinkedList<IToken>();
	protected final String str;
	
	public MultiToken(String name) {
		this.str = name;
	}
	
	public void addToken(IToken child) {
		if (child == null) return;
		this.children.add(child);
	}
	
	public IToken[] children() {
		return this.children.toArray(new IToken[this.children.size()]);
	}
	
	public String getString() {
		return "(" + getClass().getSimpleName() + ") '" + this.str + "'";
	}
	
	public String toString() {
		final StringBuilder sb = new StringBuilder(this.str);
		sb.append(" { ");
		if (this.children.size() > 0) {
			for (IToken t : this.children)
				sb.append(t.toString()).append(", ");
			sb.deleteCharAt(sb.length() - 2);
		}
		return sb.append("}").toString();
	}
}
