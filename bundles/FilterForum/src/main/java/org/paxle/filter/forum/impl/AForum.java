package org.paxle.filter.forum.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import org.paxle.core.doc.IParserDocument;

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
		Map<String, String> linkMap = parserDoc.getLinks();
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

	public void rewriteLocation(Map<String, String> linkMap) {
		Map<String,String> rewrittenRefs = new HashMap<String, String>();
		
		Iterator<String> refIterator = linkMap.keySet().iterator();
		while (refIterator.hasNext()) {
			String newRef = null;
			String ref = refIterator.next();
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

	public String rewriteLocation(String location) throws MalformedURLException, BlockUrlException {
		URL locationURL = new URL(location);

		String path = locationURL.getPath();
		String query = locationURL.getQuery();
		String newQuery = null, newLocation = null;

		ForumPage page = this.getType(path);
		if (page != null) {
			if (!page.blockPage()) throw new BlockUrlException("Crawling disallowed by filter");
			if (query == null) return location;

			// rewrite query parameters
			newQuery = this.processQueryParameters(page, query);

			// rewrite location
			int idx = location.indexOf(query);
			newLocation = location.substring(0,idx) + newQuery;
			if (newLocation.endsWith("?")) newLocation = newLocation.substring(0,newLocation.length()-1);

			return newLocation;
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