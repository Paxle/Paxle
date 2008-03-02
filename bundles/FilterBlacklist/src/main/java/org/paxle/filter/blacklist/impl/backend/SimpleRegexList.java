
package org.paxle.filter.blacklist.impl.backend;

import java.util.Iterator;

import org.paxle.filter.blacklist.impl.FilterResult;

public class SimpleRegexList implements IBlacklistBackend {
	
	public SimpleRegexList() {
	}
	
	public Iterator<String> iterator() {
		return null;
	}
	
	public boolean addPattern(String pattern) {
		return false;
	}
	
	public FilterResult isListed(String url) {
		return null;
	}
	
	public boolean remove(String pattern) {
		return false;
	}
}
