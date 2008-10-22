/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 * 
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.filter.forum.impl;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import org.paxle.core.doc.IParserDocument;
import org.paxle.core.doc.LinkInfo;

public abstract class AForum {	
	protected static Map<String,Pattern> arrayToMap(String... strings) {
		HashMap<String, Pattern> map = new HashMap<String, Pattern>();
		for (String string : strings) {
			String paramName = string, paramValue = "";
			int idx = string.indexOf("=");
			if (idx != -1) {
				paramValue = paramName.substring(idx+1).trim();
				paramName = paramName.substring(0,idx).trim();
			}
			map.put(paramName, paramValue.length()==0?null:Pattern.compile(paramValue));
		}		
		return map;
	}
		
	protected HashMap<String, ForumPage> pages = new HashMap<String, ForumPage>();
	
	protected void addForumPage(ForumPage page) {
		this.pages.put(page.getName(), page);
	}
	
	protected void ignoreForumPages(String... pagenames) {
		if (pagenames == null) return;
		for (String pageName : pagenames) {
			this.addForumPage(new ForumPage(pageName, false));
		}		
	}
	
	public void rewriteLocation(IParserDocument parserDoc) {
		if (parserDoc == null) return;

		// getting the link map
		Map<URI, LinkInfo> linkMap = parserDoc.getLinks();
		if (linkMap != null) {
			this.rewriteLocation(linkMap);
		}

		Map<String,IParserDocument> subDocs = parserDoc.getSubDocs();
		if (subDocs != null) {
			for (IParserDocument subDoc : subDocs.values()) {
				this.rewriteLocation(subDoc);
			}
		}
	}

	public void rewriteLocation(Map<URI, LinkInfo> linkMap) {
		Map<URI,LinkInfo> rewrittenRefs = new HashMap<URI,LinkInfo>();
		
		Iterator<URI> refIterator = linkMap.keySet().iterator();
		while (refIterator.hasNext()) {
			URI newRef = null;
			URI ref = refIterator.next();
			try {
				newRef = this.rewriteLocation(ref);
			} catch (MalformedURLException e) {
				newRef = null;
			} catch (BlockUrlException e) {
				newRef = null;
			}
			
			if (newRef == null) {
				// remove the URL from the map
				refIterator.remove();
			} else if (!ref.equals(newRef)) {
				// URL was rewritten
				rewrittenRefs.put(newRef, linkMap.get(ref));
				refIterator.remove();
			}			
		}
		
		// append rewrittenRefs to linkMap
		if (rewrittenRefs.size() > 0) {
			linkMap.putAll(rewrittenRefs);
		}
	}	

	public URI rewriteLocation(URI location) throws MalformedURLException, BlockUrlException {

		final String path = location.getPath();
		final String query = location.getQuery();

		ForumPage page = this.getType(path);
		if (page != null) {
			if (!page.blockPage()) throw new BlockUrlException("Crawling disallowed by filter");
			if (query == null) return location;

			// rewrite query parameters
			final String newQuery = this.processQueryParameters(page, query);

			// rewrite location
			try {
				return new URI(
						location.getScheme(),
						location.getUserInfo(),
						location.getHost(),
						location.getPort(),
						location.getPath(),
						newQuery,
						location.getFragment());
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		return location;
	}

	protected ForumPage getType(String path) {
		int idx = path.lastIndexOf("/");		
		if (idx == -1) return null; 
		path = path.substring(idx+1);

		if (this.pages.containsKey(path)) {
			return this.pages.get(path);
		}
		return null;
	}

	protected String processQueryParameters(ForumPage page, String query) throws BlockUrlException {		
		StringBuilder newQuery = new StringBuilder();

		int idx = query.lastIndexOf("#");
		if (idx != -1) query = query.substring(0,idx);

		String[] params = query.split("&");
		for (String param : params) {
			String[] paramParts = param.trim().split("=");
			String name = paramParts[0].trim();
			String value = (paramParts.length==1)?"":paramParts[1].trim();

			boolean keepParam = page.keepParam(name, value);	
			boolean pageBlockingParam = page.pageBlockingParam(name, value);
			
			if (!keepParam && pageBlockingParam) {
				throw new BlockUrlException(String.format("Crawling blocked because of query parameter '%s'.",param));
			}
			
			if (keepParam) {
				if (newQuery.length() > 0) newQuery.append("&");
				newQuery.append(param);
			}
		}

		return newQuery.toString();
	}
}
