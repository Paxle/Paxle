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
package org.paxle.se.provider.rsssearch.impl.gui;

import org.htmlparser.Tag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.visitors.NodeVisitor;

public class OpenSearchLinkCollector  extends NodeVisitor{
	private String url="";
	private boolean has_found=false;
	/**
	 * called everytime a tag is visited. 
	 * In {@link OpenSearchLinkCollector} it just checks if the Tag is a &lt;link&gt; tag
	 * with rel="search" and type="application/opensearchdescription+xml" and extracts the url
	 */
	public void visitTag(Tag tag) {
		if(tag instanceof LinkTag){
			if(tag.getAttribute("rel")!=null && tag.getAttribute("rel").toLowerCase().equals("search") &&
					tag.getAttribute("type")!=null && tag.getAttribute("type").toLowerCase().equals("application/opensearchdescription+xml")&&
					tag.getAttribute("href")!=null){
				has_found=true;
				url=((LinkTag)tag).extractLink();
			}
				
		}
	}
	/**
	 * has a url been found?
	 * @return true, if a url was found
	 */
	public boolean found(){
		return has_found;
	}
	/**
	 * returns the Opensearch-XML URL
	 * @return the Opensearch-XML URL
	 */
	public String getURL(){
		return url;
	}
}
