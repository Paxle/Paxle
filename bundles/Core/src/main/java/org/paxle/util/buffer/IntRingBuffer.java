
package org.paxle.util.buffer;

public class IntRingBuffer {
	
	private final int[] buf;
	private final int size;
	private int head;			// points to next free index
	private int tail;			// points to last full index
	private int num = 0;
	
	public IntRingBuffer(final int size) {
		this.size = size;
		buf = new int[size];
		tail = size - 1;
	}
	
	public IntRingBuffer copyToNew(final int size) {
		final IntRingBuffer rb = new IntRingBuffer(size);
		System.arraycopy(buf, 0, rb.buf, 0, Math.min(this.size, size));
		rb.head = head;
		rb.tail = tail;
		rb.num = num;
		return rb;
	}
	
	public int push(final int x) {
		final int r = (head == tail) ? pop() : -1;
		buf[head++] = x;
		if (head == size)
			head = 0;
		num++;
		return r;
	}
	
	public int pop() {
		final int r = buf[tail++];
		if (tail == size)
			tail = 0;
		num--;
		return r;
	}
	
	public int top() {
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