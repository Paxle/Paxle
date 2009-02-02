/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
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
	
	@Override
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
	
	@Override
	public ANode<E> funcFail() {
		return head.node;
	}
	
	@Override
	public ANode<E> funcGoto(byte b) {
		Entry e = head;
		while ((e = e.next) != null)
			if (e.b == b)
				return e.node;
		return null;
	}
	
	@Override
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
	
	@Override
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
