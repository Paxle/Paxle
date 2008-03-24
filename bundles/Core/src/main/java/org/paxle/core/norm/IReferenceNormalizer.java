
package org.paxle.core.norm;

import java.net.URI;

public interface IReferenceNormalizer {
	
	/**
	 * This method takes an URI-string as input and returns it in a normalized form. There should be no change in the functionality
	 * of the URI.
	 * @param reference the unnormalized URL
	 * @return the normalized URI consisting of protocol, username/password if given, hostname, port if it is not the default port for
	 *         its protocol, the path and all given query arguments. The fragment part is omitted. It also performs Punycode- and
	 *         URL-decoding.
	 */
	public URI normalizeReference(String reference);
}
