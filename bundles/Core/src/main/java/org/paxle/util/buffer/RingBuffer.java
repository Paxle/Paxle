
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
