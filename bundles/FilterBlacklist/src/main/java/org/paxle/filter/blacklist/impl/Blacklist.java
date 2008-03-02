
package org.paxle.filter.blacklist.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.paxle.filter.blacklist.impl.backend.IBlacklistBackend;
import org.paxle.filter.blacklist.impl.backend.RegexList;
import org.paxle.filter.blacklist.impl.backend.SimpleRegexList;

public class Blacklist {
	
	private static class LockedBB {
		
		final ReadLock rlock;
		final WriteLock wlock;
		final IBlacklistBackend list;
		final File file;
		
		public LockedBB(final IBlacklistBackend list, final File file) throws IOException {
			final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
			rlock = lock.readLock();
			wlock = lock.writeLock();
			this.list = list;
			this.file = file;
			
			if (!file.exists())
				file.createNewFile();
			String line;
			final BufferedReader in = new BufferedReader(new FileReader(file));
			try {
				while ((line = in.readLine()) != null)
					list.addPattern(line);
			} finally { in.close(); }
		}
		
		public void saveList() throws IOException {
			wlock.lock();
			try {
				final BufferedWriter out = new BufferedWriter(new FileWriter(file));
				try {
					for (final String entry : list) {
						out.write(entry);
						out.newLine();
					}
				} finally { out.close(); }
			} finally { wlock.unlock(); }
		}
	}
	
	private final LockedBB sr, fr;
	private final File source;
	
	private boolean changed = false;
	
	public Blacklist(final File source) throws IOException {
		this.source = source;
		sr = new LockedBB(new SimpleRegexList(), new File(source, "simple-regex"));
		fr = new LockedBB(new RegexList(), new File(source, "full-regex"));
	}
	
	public List<String> getEntries() {
		final ArrayList<String> ret = new ArrayList<String>();
		
		sr.rlock.lock();
		fr.rlock.lock();
		try {
			for (final String entry : sr.list)
				ret.add(entry);
			for (final String entry : fr.list)
				ret.add(entry);
		} finally {
			sr.rlock.unlock();
			fr.rlock.unlock();
		}
		
		return ret;
	}
	
	public void saveList(boolean force) throws IOException {
		if (!changed && !force)
			return;
		
		sr.saveList();
		fr.saveList();
		changed = false;
	}
	
	public boolean addPattern(String pattern) {
		sr.wlock.lock();
		try {
			if (sr.list.addPattern(pattern))
				return true;
		} finally { changed = true; sr.wlock.unlock(); }
		
		fr.wlock.lock();
		try {
			if (fr.list.addPattern(pattern))
				return true;
		} finally { changed = true; fr.wlock.unlock(); }
		
		return false;
	}
	
	public FilterResult isListed(String url) {
		FilterResult ret;
		sr.rlock.lock();
		try {
			ret = sr.list.isListed(url);
		} finally { sr.rlock.unlock(); }
		if (ret.getStatus() == FilterResult.LOCATION_REJECTED)
			return ret;
		
		fr.rlock.lock();
		try {
			ret = fr.list.isListed(url);
		} finally { fr.rlock.unlock(); }
		
		return ret;
	}
	
	public boolean remove(String pattern) {
		sr.wlock.lock();
		try {
			if (sr.list.remove(pattern))
				return true;
		} finally { changed = true; sr.wlock.unlock(); }
		
		fr.wlock.lock();
		try {
			if (fr.list.remove(pattern))
				return true;
		} finally { changed = true; fr.wlock.unlock(); }
		
		return false;
	}
	
	@Override
	public String toString() {
		return source.getName();
	}
}