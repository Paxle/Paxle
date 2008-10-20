/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 * 
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.se.query.tokens;

import java.util.Iterator;
import java.util.LinkedList;

abstract class MultiToken extends AToken implements Iterable<AToken> {
	
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
	
	public String getString() {
		return str;
	}
	
	public void addToken(AToken child) {
		if (child == null) return;
		child.setParent(this);
		this.children.add(child);
	}
	
	public AToken[] children() {
		return this.children.toArray(new AToken[this.children.size()]);
	}
	
	public int getChildCount() {
		return this.children.size();
	}
	
	public Iterator<AToken> iterator() {
		return new MTIterator();
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("(").append(getClass().getSimpleName()).append(") { ");
		if (children.size() > 0) {
			for (AToken t : children)
				sb.append(t).append(", ");
			sb.deleteCharAt(sb.length() - 2);
		}
		return sb.append('}').toString();
	}
}
