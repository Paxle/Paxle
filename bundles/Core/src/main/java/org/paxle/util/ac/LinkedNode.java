
package org.paxle.util.ac;

public class LinkedNode<E> extends ANode<E> {
	
	private final class Entry {
		public byte b;
		public Entry next;
		public ANode<E> node;
		
		public Entry(final byte b, final Entry next, final ANode<E> node) {
			this.b = b;
			this.next = next;
			this.node = node;
		}
	}
	
	private final Entry head = new Entry((byte)0, null, null);
	
	public LinkedNode() {
	}
	
	public byte[] getKeys() {
		Entry e = head;
		int num = 0;
		while ((e = e.next) != null)
			num++;
		final byte[] r = new byte[num];
		num = 0;
		e = head;
		while ((e = e.next) != null)
			r[num++] = e.b;
		return r;
	}
	
	public ANode<E> funcFail() {
		return head.node;
	}
	
	public ANode<E> funcGoto(byte b) {
		Entry e = head;
		while ((e = e.next) != null)
			if (e.b == b)
				return e.node;
		return null;
	}
	
	public void setFail(ANode<E> node) {
		head.node = node;
	}
	
	private void removeGoto(final byte b) {
		Entry e = head, enext;
		while ((enext = e.next) != null) {
			if (enext.b == b) {
				e.next = enext.next;
				return;
			}
		}
	}
	
	public void setGoto(byte b, ANode<E> node) {
		if (node == null) {
			removeGoto(b);
			return;
		}
		
		Entry e = head.next;
		
		if (e == null) {
			head.next = new Entry(b, null, node);
			return;
		}
		
		do {
			if (e.b == b) {
				e.node = node;
				return;
			}
			
			final Entry enext = e.next;
			if (enext == null) {
				e.next = new Entry(b, null, node);
				return;
			}
			e = enext;
		} while (true);
	}
}
