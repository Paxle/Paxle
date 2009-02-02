/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package org.paxle.core.norm;

import java.net.URI;
import java.nio.charset.Charset;

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
	public URI normalizeReference(String reference, Charset charset);
	
	public int getDefaultPort(String protocol);
}
