
package org.paxle.filter.blacklist.impl.backend;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.filter.blacklist.impl.FilterResult;

public class RegexList implements IBlacklistBackend {
	
	private final LinkedList<Pattern> blacklist = new LinkedList<Pattern>();
	private final Log logger = LogFactory.getLog(RegexList.class);
	
	public RegexList() {
	}
	
	public Iterator<String> iterator() {
		return new Iterator<String>() {
			private final Iterator<Pattern> it = blacklist.iterator();
			
			public boolean hasNext() {
				return it.hasNext();
			}
			
			public String next() {
				return it.next().pattern();
			}
			
			public void remove() {
				it.remove();
			}
		};
	}
	
	public boolean addPattern(String pattern) {
		try {
			Pattern p = Pattern.compile(pattern);
			blacklist.add(p);
			//System.out.println("Pattern from "+listFileName+" added to blacklist: "+pattern);
			/*
			List tempList = FileUtils.readLines(source);
			tempList.add(pattern);
			FileUtils.writeLines(source, tempList);
			*/
			return true;
		} catch (PatternSyntaxException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public FilterResult isListed(String url) {
		long time = System.currentTimeMillis();
		Iterator<Pattern> eter = blacklist.iterator();
		while(eter.hasNext()) {
			Pattern temp = eter.next();
			Matcher m = temp.matcher(url);
			if(m.matches()) {
				time = System.currentTimeMillis() - time;
				//System.out.println("Duration in 'isListed()' for blacklistcheck: "+ time + " ms . URL: " + url);
				logger.debug("Duration in 'isListed()' for blacklistcheck: "+ time + " ms . URL: " + url);
				return new FilterResult(FilterResult.LOCATION_REJECTED, temp.pattern());
			}
		}
		
		time = System.currentTimeMillis() - time;
		//System.out.println("Duration in 'isListed()' for blacklistcheck: "+ time + " ms . URL: " + url);
		logger.debug("Duration in 'isListed()' for blacklistcheck: "+ time + " ms . URL: " + url);
		// return new FilterResult(FilterResult.LOCATION_OKAY, null);
		return FilterResult.LOCATION_OKAY_RESULT;
	}
	
	public boolean remove(String pattern) {
		final Iterator<Pattern> it = blacklist.iterator();
		while (it.hasNext())
			if (it.next().pattern().equals(pattern)) {
				it.remove();
				return true;
			}
		return false;
	}
}
