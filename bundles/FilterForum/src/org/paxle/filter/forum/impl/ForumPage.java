package org.paxle.filter.forum.impl;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

public class ForumPage {
	private String name;
	
	/**
	 * Params that should not be removed from the query string
	 */
	private final Map<String,Pattern> paramsToKeep;
	
	/**
	 * Params that cause the whole URL to be blocked from crawling
	 */
	private final Map<String,Pattern> blockingParams; 
	
	/**
	 * specifies if the given page should be crawled
	 */
	private final boolean block; 

	@SuppressWarnings("unchecked")
	public ForumPage(String name, boolean crawl) {
		this(name, crawl, Collections.EMPTY_MAP);
	}
	
	@SuppressWarnings("unchecked")
	public ForumPage(String name, boolean block, Map<String,Pattern> paramsToKeep) {
		this(name, block,paramsToKeep,Collections.EMPTY_MAP);
	}

	public ForumPage(String name, boolean block, Map<String,Pattern> paramsToKeep, Map<String,Pattern> params) {
		this.name = name;
		this.block = block;
		this.paramsToKeep = paramsToKeep;
		this.blockingParams = params;
	}
	
	public String getName() {
		return this.name;
	}
	
	public boolean blockPage() {
		return this.block;
	}
	
	public boolean keepParam(String name, String value) {
		if (this.paramsToKeep.containsKey(name)) {
			Pattern expectedValue = this.paramsToKeep.get(name);
			if (expectedValue != null && !expectedValue.matcher(value).find()) return false;
			return true;
		}
		return false;
	}
	
	public boolean pageBlockingParam(String name, String value) {
		if (this.blockingParams.containsKey(name)) {
			Pattern expectedValue = this.blockingParams.get(name);
			if (expectedValue == null || expectedValue.matcher(value).find()) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
}
