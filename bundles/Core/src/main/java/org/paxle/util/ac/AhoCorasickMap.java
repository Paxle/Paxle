
package org.paxle.util.ac;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;

import org.paxle.util.ArrayByteBuffer;

public class AhoCorasickMap<E> extends AbstractMap<byte[],E> implements Map<byte[],E> {
	
	private static final class AMemory<E> {
		
		private final LinkedList<E> list = new LinkedList<E>();
		private final boolean queue;
		
		public AMemory(final boolean queue) {
			this.queue = queue;
		}
		
		public void add(E e) {
			if (queue) {
				list.addLast(e);
			} else {
				list.addFirst(e);
			}
		}
		
		public E remove() {
			return list.removeFirst();
		}
		
		public boolean isEmpty() {
			return list.isEmpty();
		}
	}
	
	private final AhoCorasick<E> ac;
	
	public AhoCorasickMap() {
		ac = new AhoCorasick<E>();
	}
	
	public AhoCorasickMap(final int threshold) {
		ac = new AhoCorasick<E>(threshold);
	}
	
	public AhoCorasickMap(final INodeFactory<E> factory) {
		ac = new AhoCorasick<E>(factory);
	}
	
	@Override
	public void clear() {
		ac.clear();
	}
	
	@Override
	public int size() {
		return ac.size();
	}
	
	@Override
	public boolean containsKey(Object key) {
		if (key == null)
			throw new NullPointerException("no null keys permitted");
		final byte[] k = (byte[])key;
		return ac.matchingNode(k, ac.root, 0, k.length) != null;
	}
	
	@Override
	public E remove(Object key) {
		if (key == null)
			throw new NullPointerException("no null keys permitted");
		final byte[] k = (byte[])key;
		return ac.removePattern(k);
	}
	
	@Override
	public E put(byte[] key, E value) {
		if (key == null)
			throw new NullPointerException("no null keys permitted");
		final E r = ac.match(key);
		final boolean added = ac.addPattern(key, value);
		return (added) ? r : null;
	}
	
	@Override
	public E get(Object key) {
		if (key == null)
			throw new NullPointerException("no null keys permitted");
		return ac.match((byte[])key);
	}
	
	@Override
	public EntrySet entrySet() {
		return new EntrySet();
	}
	
	private class ACEntry implements Map.Entry<byte[],E> {
		
		protected final byte[] key;
		protected final ANode<E> node;
		
		public ACEntry(final byte[] key, final ANode<E> node) {
			this.key = key;
			this.node = node;
		}
		
		public byte[] getKey() {
			return key;
		}
		
		public E getValue() {
			return node.getFirstValue();
		}
		
		public E setValue(E value) {
			final E old = node.getFirstValue();
			node.setFirstValue(value, -1);
			return old;
		}
	}
	
	private final class EntryIterator implements Iterator<Map.Entry<byte[],E>> {
		
		private final class ItACEntry extends ACEntry {
			
			private ArrayByteBuffer bb;
			
			public ItACEntry(final ArrayByteBuffer bb, final ANode<E> node) {
				super(bb.toByteArray(), node);
				this.bb = bb;
			}
			
			public ItACEntry next(final byte b) {
				final ItACEntry entry = new ItACEntry(bb.append(b), node.funcGoto(b));
				bb = null;
				return entry;
			}
			
			public ItACEntry nextClone(final byte b) {
				final ItACEntry entry = new ItACEntry(bb.clone().append(b), node.funcGoto(b));
				bb = null;
				return entry;
			}
		}
		
		private final AMemory<ItACEntry> queue;
		private ACEntry next;
		private ACEntry current = null;
		
		public EntryIterator(final boolean depthFirst) {
			queue = new AMemory<ItACEntry>(depthFirst);
			queue.add(new ItACEntry(new ArrayByteBuffer(), ac.root));
			next = next0();
		}
		
		private ItACEntry next0() {
			while (!queue.isEmpty()) {
				final ItACEntry se = queue.remove();
				
				final byte[] keys = se.node.getKeys();
				for (int i=1; i<keys.length; i++)
					queue.add(se.nextClone(keys[i]));
				queue.add(se.next(keys[0]));
				
				if (se.node.getFirstValue() != null)
					return se;
			}
			return null;
		}
		
		public boolean hasNext() {
			return next != null;
		}
		
		public Map.Entry<byte[],E> next() {
			if (next == null)
				throw new NoSuchElementException();
			current = next;
			next = next0();
			return current;
		}
		
		public void remove() {
			if (current == null)
				throw new IllegalStateException("element already removed or next() not called yet");
			ac.removePattern(current.key);
			current = null;
		}
	}
	
	public final class EntrySet extends AbstractSet<Map.Entry<byte[],E>> {
		
		// breadth-first
		@Override
		public Iterator<Map.Entry<byte[],E>> iterator() {
			return new EntryIterator(true);
		}
		
		public Iterator<Map.Entry<byte[],E>> iterator(final boolean breadthFirst) {
			return new EntryIterator(breadthFirst);
		}
		
		@Override
		public boolean add(Map.Entry<byte[],E> e) {
			return ac.addPattern(e.getKey(), e.getValue());
		}
		
		@Override
		public boolean remove(Object o) {
			if (o == null)
				throw new IllegalArgumentException("o is null");
			return remove((Map.Entry<?,?>)o);
		}
		
		public boolean remove(Map.Entry<?,?> e) {
			if (e.getKey() == null)
				throw new NullPointerException("no elements with null keys permitted");
			if (e.getValue() == null)
				throw new NullPointerException("no elements with null values permitted");
			return ac.removePattern((byte[])e.getKey()) != null;
		}
		
		@Override
		public boolean contains(Object o) {
			if (o == null)
				throw new IllegalArgumentException("o is null");
			return contains((Map.Entry<?,?>)o);
		}
		
		public boolean contains(Map.Entry<?,?> e) {
			if (e.getKey() == null)
				throw new NullPointerException("no elements with null keys permitted");
			if (e.getValue() == null)
				throw new NullPointerException("no elements with null values permitted");
			final byte[] key = (byte[])e.getKey();
			final AhoCorasick<E>.SearchNodeResult result = ac.matchingNode(key, ac.root, 0, key.length);
			if (result == null)
				return false;
			return e.equals(new ACEntry(key, result.node));
		}
		
		@Override
		public void clear() {
			ac.clear();
		}
		
		@Override
		public int size() {
			return ac.size();
		}
	}
}
