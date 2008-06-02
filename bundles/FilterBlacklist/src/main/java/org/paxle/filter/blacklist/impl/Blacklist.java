package org.paxle.filter.blacklist.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is a RegExp-based Blacklist
 * @author Matthias Soehnholz
 * @author Michael Hamann
 *
 */
public class Blacklist {
	private static File blacklistDir;
	private File listFile;
	private ConcurrentHashMap<String,Pattern> blacklist;
	public String name;
	private Log logger = LogFactory.getLog(this.getClass());
	private static ConcurrentHashMap<String,Blacklist> blacklists = new ConcurrentHashMap<String,Blacklist>();

	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	/**
	 * initializes the blacklist
	 * @param dir the directory where all blacklistfiles are stored
	 */
	static void init(File dir) {
		blacklistDir = dir;
		Iterator<?> eter = FileUtils.iterateFiles(blacklistDir, null, false);
		while(eter.hasNext()) {
			new Blacklist(((File) eter.next()).getName());
		}
	}

	/**
	 * Gets all blacklistnames
	 * @return all blacklistnames
	 */
	static	List<String> getLists() {
		return new ArrayList<String>(blacklists.keySet());
	}

	/**
	 * gets the blacklist, please note that you have to call init first
	 * @param name the name of the list
	 * @return the blacklist
	 * @throws InvalidFilenameException 
	 */
	static Blacklist getList(String name) throws InvalidFilenameException {
		validateBlacklistname(name);
		return blacklists.get(name);
	}

	/**
	 * New blacklist object for existing blacklistfile
	 * @param name the name of the blacklist
	 */
	private Blacklist(String name) {
		this.name = name;
		this.listFile = new File(blacklistDir, this.name);
		if (!(listFile.canRead() && listFile.canWrite())) throw new IllegalArgumentException("Unknown blacklist.");
		boolean incorrectPattern = false;
		try {
			blacklist = new ConcurrentHashMap<String,Pattern>();
			Iterator<?> patternIterator = FileUtils.readLines(this.listFile).iterator();
			while (patternIterator.hasNext()) {
				String pattern = (String) patternIterator.next();
				try {
					blacklist.put(pattern, Pattern.compile(pattern));
				} catch (PatternSyntaxException e) {
					logger.warn("Invalid blacklistpattern " + pattern + " in file " + name + ", it will be ignored and a version without this pattern will be saved");
					incorrectPattern = true;
					e.printStackTrace();
				}
			}
			this.store();
			if (incorrectPattern) // store a version of the list that does not contain invalid patterns anymore
				FileUtils.writeLines(this.listFile, this.getPatternList());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Adds a new empty blacklist-file
	 * @param name name of the new blacklistfile
	 * @throws InvalidFilenameException 
	 */	
	static Blacklist create(String name) throws InvalidFilenameException {

		validateBlacklistname(name);

		if (getList(name) != null)
			return getList(name);
		else {
			try {
				FileUtils.touch(new File(blacklistDir, name));
				return new Blacklist(name);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
	}
	
	static final int NAME_OK = -1;
	static final int LENGTH_ZERO = -2;
	
	/**
	 * Checks whether the given name is a valid blacklist name. First, all whitespace is removed, then the length
	 * of the result is tested. If it is zero, {@link #LENGTH_ZERO} is returned. If the remaining characters are
	 * valid, this method returns {@link #NAME_OK}, otherwise the first invalid character is returned.
	 *   
	 * @param name the blacklist name to check
	 * @return {@link #NAME_OK} if the given name is a valid name for a blacklist, {@link #LENGTH_ZERO} if the
	 *         name only consists of whitespace or is an empty string, the first invalid character otherwise.
	 */
	static int offendingChar(final String name) {
		final String others = "+-_.&()=";
		
		final String nn = name.replace("\\s", "").toLowerCase();
		if (nn.length() == 0)
			return LENGTH_ZERO;
		
		for (int i=0; i<nn.length(); i++) {
			final char c = nn.charAt(i);
			if (!(c >= 'a' && c <= 'z' || c >= '0' && c <= '9') || others.indexOf(c) != -1)
				return c;
		}
		return NAME_OK;
	}
	
	/**
	 * Uses {@link #offendingChar(String)} to test whether the name is a valid identifier for a blacklist.
	 * @param name the name to check
	 * @return whether the given name is a valid name for a blacklist or not
	 */
	static boolean isValidBlacklistName(final String name) {
		return offendingChar(name) == NAME_OK;
	}
	
	/**
	 * This method checks a given name for attempts of a directory traversal, an empty name and for invalid characters
	 * @throws InvalidFilenameException 
	 */
	private static void validateBlacklistname(final String name) throws InvalidFilenameException {
		final int c = offendingChar(name);
		switch (c) {
			case NAME_OK: return;
			case LENGTH_ZERO: throw new InvalidFilenameException("The blacklist name is empty.");
			default:
				throw new InvalidFilenameException(
						"The name '" + name + "' is not a valid name for a blacklist. " +
						"Please remove all '" + (char)c + "' characters.");
		}
	}
	/*
	private static void validateBlacklistname(String name) throws InvalidFilenameException {
		String chars = "abcdefghijklmnopqrstuvwxyz";
		String numbers = "0123456789";
		String others = "+-_.&()=";
		final char[] allowedCharacters = (chars + numbers + others).toCharArray();
		
		//Reduce all blank spaces to none for check. This ensure a file with name "       " isn't valid.
		//A file named "              g" still is valid.
		//In this check it will be threaded as "g", but the created file will be "              g"
		name = name.replaceAll(" ", "");

		if (name.equals("")) {
			throw new InvalidFilenameException("The blacklist name is empty.");
		}
		
		name = name.toLowerCase();
		char temp [] = name.toCharArray();
		boolean test;
		int i = 0;
		for(i=0; i<temp.length;i++) {
			test = false;
			for(int j=0;j<allowedCharacters.length;j++) {
				if(temp[i]==allowedCharacters[j]) {
					test = true;
					break;
				}
			}
			if(!test)
				throw new InvalidFilenameException("The name '" + name + "' is not a valid name for a blacklist. Please remove all '" + temp[i] + "' characters.");
		}
		return;
	}*/

	/**
	 * Deletes the blacklist
	 * @return boolean if delete was successful
	 */
	public boolean destroy() {
		lock.writeLock().lock();
		if (listFile.delete()) {
			this.unstore();
			this.blacklist.clear();
			lock.writeLock().unlock();
			return true;
		} else {
			lock.writeLock().unlock();
			return false;
		}
	}

	/**
	 * store this blacklist so that it can be derived using getList
	 */
	private void store() {
		blacklists.put(this.name, this);
	}

	/**
	 * remove this blacklist from the store so that it can be longer accessed
	 * please note that this does not delete the blacklist
	 */
	private void unstore() {
		blacklists.remove(this.name);
	}

	/**
	 * The name of the blacklist
	 * @return
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * 
	 * @param url URL to be checked against blacklist
	 * @return returns a String containing the pattern which blacklists the url, returns null otherwise
	 */
	public FilterResult isListed(String url) {
		long time = System.currentTimeMillis();
		Enumeration<Pattern> eter = blacklist.elements();
		while(eter.hasMoreElements()) {
			Pattern temp = eter.nextElement();
			Matcher m = temp.matcher(url);
			if(m.matches()) {
				time = System.currentTimeMillis() - time;
				//System.out.println("Duration in 'isListed()' for blacklistcheck: "+ time + " ms . URL: " + url);
				logger.debug("Duration in 'isListed()' for blacklistcheck: "+ time + " ms . URL: " + url);
				return new FilterResult(FilterResult.LOCATION_REJECTED, temp.pattern());
			}
		}
		time = System.currentTimeMillis() - time;
		//System.out.println("Duration in 'isListed()' for blacklistcheck: "+ time + " ms . URL: " + url);
		logger.debug("Duration in 'isListed()' for blacklistcheck: "+ time + " ms . URL: " + url);
		return FilterResult.LOCATION_OKAY_RESULT;
	}

	/**
	 * Checks if an URL is listed in any blacklist
	 * @param url URL to be checked against blacklists
	 * @return returns the FilterResult which contains the pattern which blacklists the URL
	 */
	public static FilterResult isListedInAnyList(String url) {
		Iterator<Blacklist> allLists = blacklists.values().iterator();
		while (allLists.hasNext()) {
			FilterResult result = allLists.next().isListed(url);
			if (result.getStatus() == FilterResult.LOCATION_REJECTED)
				return result;
		}
		return FilterResult.LOCATION_OKAY_RESULT;
	}


	/**
	 * Returns all entries in the list as Strings
	 * @return
	 */
	public List<String> getPatternList() {
		return new ArrayList<String>(blacklist.keySet());
	}

	/**
	 * Adds a new blacklist-pattern to the selected blacklistfile
	 * @param pattern blacklistpattern to be added 
	 * @return if the blacklistpattern was successfully added
	 */
	public boolean addPattern(String pattern) {
		lock.writeLock().lock();
		try {
			Pattern p = Pattern.compile(pattern);
			blacklist.put(pattern, p);
			//System.out.println("Pattern from "+listFileName+" added to blacklist: "+pattern);
			FileWriter listWriter = new FileWriter(this.listFile, true);
			listWriter.write(pattern + "\n");
			listWriter.close();
			this.store(); // The blacklist might have been destroyed
			return true;
		} catch (PatternSyntaxException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Removes the pattern from the blacklistfile
	 * @param pattern blacklistpattern to be removed
	 * @return if the pattern was successfully removed, note that it is even true when the pattern wasn't included in the list
	 */
	public boolean removePattern(String pattern) {
		lock.writeLock().lock();
		try {
			blacklist.remove(pattern);
			FileUtils.writeLines(this.listFile, this.getPatternList());
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Edits a pattern of the blacklistfile
	 * @param fromPattern the blacklistpattern that shall be edited
	 * @param toPattern the new value of the blacklistpattern
	 * @return if the blacklistpattern was successfully edited
	 */
	public boolean editPattern(String fromPattern, String toPattern) {
		try {
			Pattern.compile(toPattern);
			return (this.removePattern(fromPattern) && this.addPattern(toPattern));
		} catch (PatternSyntaxException e) {
			e.printStackTrace();
			return false;
		}

	}
}