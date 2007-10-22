package org.paxle.p2p.services.search.impl;

public class SearchServiceConstants {
	/* ===================================================================
	 * Service properties
	 * =================================================================== */
	public static final String SERVICE_MOD_CLASS_NAME = "JXTAMOD:Paxle:SearchService";
	public static final String SERVICE_MOD_SPEC_NAME = "JXTASPEC:Paxle:SearchService";
	
	/* ===================================================================
	 * Request message properties
	 * =================================================================== */
	public static final String REQ_ID = "reqID";
	public static final String REQ_PIPE_ADV = "pipeAdv";
	public static final String REQ_QUERY = "query";
	public static final String REQ_MAX_RESULTS = "maxResults";
	public static final String REQ_TIMEOUT = "timeout";
	
	/* ===================================================================
	 * Response message properties
	 * =================================================================== */	
	public static final String RESP_SIZE = "size";
	public static final String RESP_RESULT = "result";
	
	/* ===================================================================
	 * Result properties
	 * =================================================================== */	
	public static final String RESULT_ROOT = "SearchResults";
	public static final String RESULT_ENTRY = "SearchResult";
	public static final String RESULT_ENTRY_ITEM = "item";
}
