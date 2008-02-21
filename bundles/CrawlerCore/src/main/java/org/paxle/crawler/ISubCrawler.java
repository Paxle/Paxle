package org.paxle.crawler;

import org.paxle.core.doc.ICrawlerDocument;


/**
 * The interface that must be implemented by all sub-crawlers
 */
public interface ISubCrawler {
	/*
	 * A list of service properties
	 */
	/** the network protocol supported by a sub-crawler */
	public static final String PROP_PROTOCOL = "Protocol";
	
	/**
	 * @return the network protocols supported by this sub-crawler
	 */
	public String[] getProtocols();
	
	public ICrawlerDocument request(String requestUrl);
}
