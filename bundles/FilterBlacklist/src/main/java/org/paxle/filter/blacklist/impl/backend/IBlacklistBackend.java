
package org.paxle.filter.blacklist.impl.backend;

import org.paxle.filter.blacklist.impl.FilterResult;

public interface IBlacklistBackend extends Iterable<String> {
	
    /**
     * Adds a new blacklist-pattern to the selected blacklistfile
     * @param pattern blacklistpattern to be added 
     * @return whether the pattern could be added successfully / is supported or not 
     */
	public boolean addPattern(String pattern);
	
    /**
     * 
     * @param url URL to be checked against blacklist
     * @return returns a String containing the pattern which blacklists the url, returns null otherwise
     */
	public FilterResult isListed(String url);
	public boolean remove(String pattern);
}
