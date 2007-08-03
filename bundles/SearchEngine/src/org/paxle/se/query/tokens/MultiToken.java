package org.paxle.se.query.tokens;

import java.util.LinkedList;

import org.paxle.se.query.IParentToken;
import org.paxle.se.query.IToken;

public class MultiToken extends PlainToken implements IParentToken {
	
	protected final LinkedList<IToken> children = new LinkedList<IToken>();
	
	public MultiToken(String name) {
		super(name);
	}
	
	public void addToken(IToken child) {
		if (child == null) return;
		this.children.add(child);
	}
	
	public IToken[] children() {
		return this.children.toArray(new IToken[this.children.size()]);
	}
	
	public String toString() {
		final StringBuilder sb = new StringBuilder(super.str);
		sb.append(" { ");
		if (this.children.size() > 0) {
			for (IToken t : this.children)
				sb.append(t.toString()).append(", ");
			sb.deleteCharAt(sb.length() - 2);
		}
		return sb.append("}").toString();
	}
}
