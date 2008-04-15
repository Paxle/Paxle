package org.paxle.crawler.http.impl;

import javax.net.ssl.X509TrustManager;


public class AllX509TrustManager implements X509TrustManager {

	/**
	 * @see X509TrustManager#getAcceptedIssuers()
	 */
	public java.security.cert.X509Certificate[] getAcceptedIssuers() {    	
		return null;
	}

	/**
	 * @see X509TrustManager#checkClientTrusted(java.security.cert.X509Certificate[], String)
	 */
	public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
		// nothing todo-here
	}

	/**
	 * @see X509TrustManager#checkServerTrusted(java.security.cert.X509Certificate[], String)
	 */
	public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
		// nothing todo-here
	}
}