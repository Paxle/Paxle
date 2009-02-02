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
package org.paxle.parser.sitemap.impl;

import java.util.Iterator;

import javax.xml.stream.XMLStreamReader;

import org.paxle.parser.sitemap.api.Url;
import org.paxle.parser.sitemap.api.Urlset;

public class UrlsetImpl implements Urlset {

	private final XMLStreamReader parser;
	private UrlsetIteratorImpl iter;
	
	public UrlsetImpl(XMLStreamReader parser) {		
		if (parser == null) throw new NullPointerException("The stream-parser is null");
		this.parser = parser;
	}
	
	public Iterator<Url> iterator() {
		if (this.iter != null) return this.iter;
		this.iter = new UrlsetIteratorImpl(this.parser);		
		return this.iter;
	}

}
