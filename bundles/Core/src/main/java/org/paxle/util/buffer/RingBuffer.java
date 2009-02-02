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
package org.paxle.util.buffer;

public class RingBuffer<E> {
	
	private final E[] buf;
	private final int size;
	private int head;			// points to next free index
	private int tail;			// points to last full index
	private int num = 0;
	
	@SuppressWarnings("unchecked")
	public RingBuffer(final int size) {
		this.size = size;
		buf = (E[])new Object[size];
		tail = size - 1;
	}
	
	public RingBuffer<E> copyToNew(final int size) {
		final RingBuffer<E> rb = new RingBuffer<E>(size);
		System.arraycopy(buf, 0, rb.buf, 0, Math.min(this.size, size));
		rb.head = head;
		rb.tail = tail;
		rb.num = num;
		return rb;
	}
	
	public E push(final E x) {
		final E r = (head == tail) ? pop() : null;
		buf[head++] = x;
		if (head == size)
			head = 0;
		num++;
		return r;
	}
	
	public E pop() {
		final E r = buf[tail++];
		if (tail == size)
			tail = 0;
		num--;
		return r;
	}
	
	public E top() {
		return buf[tail];
	}
	
	public void clear() {
		head = tail + 1;
		if (head == size)
			head = 0;
		num = 0;
	}
	
	public boolean isEmpty() {
		return num == 0;
	}
}
