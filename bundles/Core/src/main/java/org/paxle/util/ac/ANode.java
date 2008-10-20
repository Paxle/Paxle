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

package org.paxle.util.ac;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

public abstract class ANode<E> implements Iterable<ANode<E>>, Cloneable {
	
	static class ValueEntry<E> {
		public final E value;
		public final int length;
		
		public ValueEntry(final E value, final int length) {
			this.value = value;
			this.length = length;
		}
		
		@Override
		public String toString() {
			return "'" + value + "' (" + length + ")";
		}
	}
	
	public abstract ANode<E> funcGoto(final byte b);
	public abstract ANode<E> funcFail();
	
	public abstract void setGoto(final byte b, final ANode<E> node);
	public abstract void setFail(final ANode<E> node);
	
	public abstract byte[] getKeys();
	
	/* ------------------------------------------------------------------- */
	
	private int usage;
	protected LinkedList<ValueEntry<E>> values = null;
	
	public ANode() {
		usage = 1;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected ANode<E> clone() {
		try {
			final ANode<E> n = (ANode<E>)super.clone();
			n.usage = usage;
			n.values = (values == null) ? null : (LinkedList<ValueEntry<E>>)values.clone();
			n.setFail(funcFail());
			for (final byte b : getKeys())
				n.setGoto(b, funcGoto(b));
			return n;
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ANode<?>))
			return super.equals(obj);
		
		final ANode<?> n = (ANode<?>)obj;
		if (usage != n.usage)
			return false;
		if (funcFail() != n.funcFail())
			return false;
		
		if (values == null) {
			if (n.values != null)
				return false;
		} else {
			if (n.values == null)
				return false;
			if (values.size() != n.values.size())
				return false;
			if (!values.equals(n.values))
				return false;
		}
		
		final byte[] nkeys = n.getKeys();
		final byte[] keys = getKeys();
		if (keys.length != nkeys.length)
			return false;
		for (final byte b : keys)
			if (funcGoto(b) != n.funcGoto(b))		// don't check nodes via equals() to avoid recursion
				return false;
		return true;
	}
	
	// package privates
	boolean decUsageCount() {
		return usage-- > 1;
	}
	
	void incUsageCount() {
		usage++;
	}
	
	int getUsageCount() {
		return usage;
	}
	
	/* ------------------------------------------------------------------- */
	
	public Iterator<ANode<E>> iterator() {
		return new ChildIterator();
	}
	
	protected class ChildIterator implements Iterator<ANode<E>> {
		
		private final byte[] keys = getKeys();
		private int idx = 0;
		
		public boolean hasNext() {
			return idx < keys.length;
		}
		
		public ANode<E> next() {
			return funcGoto(keys[idx++]);
		}
		
		public void remove() {
			if (idx == 0)
				throw new IllegalStateException();
			setGoto(keys[idx - 1], null);
		}
	}
	
	void addValues(final LinkedList<ValueEntry<E>> values) {
		if (values == null || values.size() == 0)
			return;
		if (this.values == null) {
			this.values = new LinkedList<ValueEntry<E>>();
			this.values.add(null);				// first and actual element of this node
		}
		this.values.addAll(values);
	}
	
	void clearFailValues() {
		if (values == null || values.size() <= 1)
			return;
		for (int i=1; i<values.size(); i++)
			values.remove(i);
	}
	
	LinkedList<ValueEntry<E>> getValues() {
		return values;
	}
	
	ValueEntry<E> getFirst() {
		if (values == null)
			return null;
		final ValueEntry<E> first = values.getFirst();
		if (first == null && values.size() > 1)
			return values.get(1);
		return first;
	}
	
	E getFirstValue() {
		final ValueEntry<E> entry = getFirst();
		return (entry == null) ? null : entry.value;
	}
	
	void setFirstValue(final E value) {
		setFirstValue(value, -1);
	}
	
	void setFirstValue(final E value, final int length) {
		final int len;
		if (length == -1) {
			final ValueEntry<E> first = getFirst();
			if (first == null)
				throw new IllegalStateException("no previous value available and length is -1");
			len = first.length;
		} else {
			len = length;
		}
		final ValueEntry<E> entry = new ValueEntry<E>(value, len);
		if (values == null) {
			values = new LinkedList<ValueEntry<E>>();
			values.add(entry);
		} else {
			this.values.set(0, entry);
		}
	}
	
	@Override
	public String toString() {
		return '@' + Integer.toHexString(hashCode()) +
		" " + values +
		", keys: " + Arrays.toString(getKeys()) +
		", " + failToString();
	}
	
	protected String failToString() {
		final ANode<E> fail = funcFail();
		return "fail: { " + ((fail == this) ? "this" : fail) + " }";
	}
	
	public String treeToString() {
		final StringBuilder sb = new StringBuilder();
		treeToString(sb, 0);
		return sb.toString();
	}
	
	protected void treeToString(final StringBuilder sb, final int space) {
		sb.append(toString()).append(": [");
		final byte[] keys = getKeys();
		if (keys.length > 0) {
			sb.append('\n');
			final int klen_ = keys.length - 1;
			for (int i=0; i<klen_; i++) {
				for (int j=0; j<space; j++)
					sb.append(((j & 1) == 0) ? '.' : ' ');
				sb.append("'").append((char)keys[i]).append("' -> ");
				funcGoto(keys[i]).treeToString(sb, space + 2);
				sb.append(",\n");
			}
			for (int j=0; j<space; j++)
				sb.append(((j & 1) == 0) ? '.' : ' ');
			sb.append("'").append((char)keys[klen_]).append("' -> ");
			funcGoto(keys[klen_]).treeToString(sb, space + 2);
		}
		sb.append(']');
	}
}
