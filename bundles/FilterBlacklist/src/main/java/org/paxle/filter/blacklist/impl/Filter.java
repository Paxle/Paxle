
package org.paxle.filter.blacklist.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.paxle.core.doc.IParserDocument;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.queue.ICommand;
import org.paxle.filter.blacklist.IBlacklistFilter;

public class Filter implements IBlacklistFilter {
	
	private static final class ListFilter implements FileFilter {
		
		public boolean accept(File pathname) {
			return pathname.isDirectory();
		}
	}
	
	private final HashMap<String,Blacklist> activeLists = new HashMap<String,Blacklist>();
	private final Log logger = LogFactory.getLog(Filter.class);
	private final File topdir;
	private final ReadLock rlock;
	private final WriteLock wlock;
	
	public Filter(final File topdir) throws IOException {
		this.topdir = topdir;
		final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		rlock = lock.readLock();
		wlock = lock.writeLock();
		
		// TODO: configuration file for active lists, currently all existing lists are active
		for (final File listdir : topdir.listFiles(new ListFilter()))
			activeLists.put(listdir.getName(), new Blacklist(listdir));
	}
	
	public void saveLists(final boolean force) {
		rlock.lock();
		try {
			for (final Blacklist blacklist : activeLists.values())
				try {
					blacklist.saveList(force);
				} catch (IOException e) {
					logger.error("I/O error saving blacklist " + blacklist, e);
				}
		} finally { rlock.unlock(); }
	}
	
	public List<String> getActiveLists() {
		rlock.lock();
		try {
			return new ArrayList<String>(activeLists.keySet());
		} finally { rlock.unlock(); }
	}
	
	public List<String> getLists() {
		rlock.lock();
		final File[] lists;
		try {
			lists = topdir.listFiles(new ListFilter());
		} finally { rlock.unlock(); }
		
		final ArrayList<String> ret = new ArrayList<String>(lists.length);
		for (int i=0; i<lists.length; i++)
			ret.add(lists[i].getName());
		return ret;
	}
	
	public void addPattern(final String pattern, final String list) {
		final Blacklist blacklist;
		rlock.lock();
		try {
			blacklist = activeLists.get(list);
		} finally { rlock.unlock(); }
		if (blacklist == null)
			throw new NoSuchElementException("list " + list + " does not exist");
		blacklist.addPattern(pattern);
	}
	
	public void removePattern(final String pattern, final String list) {
		final Blacklist blacklist;
		rlock.lock();
		try {
			blacklist = activeLists.get(list);
		} finally { rlock.unlock(); }
		if (blacklist == null)
			throw new NoSuchElementException("list " + list + " does not exist");
		blacklist.remove(pattern);
	}
	
	public void addList(final String name) {
		final File listdir = new File(topdir, name);
		if (listdir.exists())
			return;
		wlock.lock();
		try {
			listdir.mkdir();
		} finally { wlock.unlock(); }
	}
	
	public void removeList(final String name) {
		final File listdir = new File(topdir, name);
		if (!listdir.exists())
			return;
		wlock.lock();
		try {
			activeLists.remove(name);
			FileUtils.deleteQuietly(listdir);
		} finally { wlock.unlock(); }
	}
	
	public void setActive(final String name, final boolean yes) throws IOException {
		final File listdir = new File(topdir, name);
		if (!listdir.exists())
			return;
		wlock.lock();
		try {
			if (yes) {
				activeLists.put(name, new Blacklist(listdir));
			} else {
				activeLists.remove(name);
			}
		} finally { wlock.unlock(); }
	}
	
	public FilterResult isListed(final String url) {
		rlock.lock();
		try {
			for (final Blacklist blacklist : activeLists.values()) {
				final FilterResult res = blacklist.isListed(url);
				if (res.getStatus() == FilterResult.LOCATION_REJECTED)
					return res;
			}
		} finally { rlock.unlock(); }
		return FilterResult.LOCATION_OKAY_RESULT;
	}
	
	public void filter(ICommand command, IFilterContext filterContext) {
		FilterResult result = isListed(command.getLocation().toASCIIString());
		if(result.getStatus()==FilterResult.LOCATION_REJECTED) {
			command.setResult(ICommand.Result.Rejected, "rejected by blacklistentry: " + result.getRejectPattern());
			//System.out.println(command.getLocation() + " rejected by blacklistentry: " + result.getRejectPattern());
			logger.info(command.getLocation() + " rejected by blacklistentry: " + result.getRejectPattern());
			return;
		}
		// check the extracted links
		IParserDocument parserDoc = command.getParserDocument();
		this.checkBlacklist(parserDoc);
	}
	
	private void checkBlacklist(IParserDocument parserDoc) {
		if (parserDoc == null) return;
		
		// getting the link map
		Map<URI, String> linkMap = parserDoc.getLinks();
		if (linkMap != null) {
			this.checkBlacklist(linkMap);
		}
		
		// loop through sub-parser-docs
		Map<String,IParserDocument> subDocs = parserDoc.getSubDocs();
		if (subDocs != null) {
			for (IParserDocument subDoc : subDocs.values()) {
				this.checkBlacklist(subDoc);
			}
		}
	}
	
	private void checkBlacklist(Map<URI, String> linkMap) {
		if (linkMap == null || linkMap.size() == 0) return;
		
		Iterator<URI> refs = linkMap.keySet().iterator();
		while (refs.hasNext()) {
			URI location = refs.next();
			FilterResult result = isListed(location.toASCIIString());
			if (result.getStatus() == FilterResult.LOCATION_REJECTED) {
				refs.remove();
				//System.out.println(location + " rejected by blacklistentry: " + result.getRejectPattern());
				this.logger.info(location + " rejected by blacklistentry: " + result.getRejectPattern());
			}
		}
	}
}
