
package org.paxle.util.ac;

public class SearchResult<E> {
	
	private final int start;
	private final int end;
	private final E value;
	
	public SearchResult(final int start, final int end, final E value) {
		this.start = start;
		this.end = end;
		this.value = value;
	}
	
	public int getMatchBegin() {
		return start;
	}
	
	public int getMatchEnd() {
		return end;
	}
	
	public E getValue() {
		return value;
	}
}
