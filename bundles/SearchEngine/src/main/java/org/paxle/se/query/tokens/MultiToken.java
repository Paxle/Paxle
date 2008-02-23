package org.paxle.se.query.tokens;

import java.util.Iterator;
import java.util.LinkedList;

public abstract class MultiToken extends AToken implements Iterable<AToken> {
	
	protected class MTIterator implements Iterator<AToken> {
		
		protected final Iterator<AToken> baseIt = MultiToken.this.children.iterator();
		protected AToken current = null;
		
		public boolean hasNext() {
			return this.baseIt.hasNext();
		}
		
		public AToken next() {
			this.current = this.baseIt.next();
			return this.current;
		}
		
		public void remove() {
			this.current.setParent(null);
			this.current = null;
			this.baseIt.remove();
		}
	}
	
	protected final LinkedList<AToken> children = new LinkedList<AToken>();
	protected final String str;
	
	public MultiToken(String name) {
		this.str = name;
	}
	
	public void addToken(AToken child) {
		if (child == null) return;
		child.setParent(this);
		this.children.add(child);
	}
	
	public AToken[] children() {
		return this.children.toArray(new AToken[this.children.size()]);
	}
	
	public Iterator<AToken> iterator() {
		return new MTIterator();
	}
}
