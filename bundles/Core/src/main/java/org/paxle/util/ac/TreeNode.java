/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2010 the original author or authors.
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

public class TreeNode<E> extends ANode<E> {
	
	private final class Entry {
		public byte b;
		public Entry left, right;
		public ANode<E> node;
		
		public Entry(final byte b, final ANode<E> node) {
			this.b = b;
			this.node = node;
			n++;
		}
	}
	
	private Entry root = null;
	private ANode<E> fail = null;
	private int n = 0;
	
	@Override
	public ANode<E> funcFail() {
		return fail;
	}
	
	@Override
	public ANode<E> funcGoto(byte b) {
		Entry e = root;
		while (e != null) {
			if (b < e.b) {
				e = e.left;
			} else if (b > e.b) {
				e = e.right;
			} else {
				return e.node;
			}
		}
		return null;
	}
	
	@Override
	public void setGoto(byte b, ANode<E> node) {
		if (root == null) {
			root = new Entry(b, node);
		} else {
			Entry p = root;
			while (p.b != b) {
				if (b < p.b) {
					if (p.left == null)
						p.left = new Entry(b, node);
					p = p.left;
				} else if (b > p.b) {
					if (p.right == null)
						p.right = new Entry(b, node);
					p = p.right;
				}
			}
			p.node = node;
		}
	}
	
	private int insert(final byte[] r, final int i, final Entry e) {
		if (e == null) return i;
		int j = insert(r, i, e.left);
		r[j++] = e.b;
		j = insert(r, i, e.right);
		return j;
	}
	
	@Override
	public byte[] getKeys() {
		final byte[] r = new byte[n];
		insert(r, 0, root);
		return r;
	}
	
	@Override
	public void setFail(ANode<E> node) {
		fail = node;
	}
}
