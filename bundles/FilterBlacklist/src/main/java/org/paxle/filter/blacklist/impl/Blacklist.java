package org.paxle.filter.blacklist.impl;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.regex.Matcher;
import java.io.FileWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.io.FileUtils;

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
	static 	List<String> getLists() {
    	List<String> fileNames = new LinkedList<String>();		
    	for (Enumeration<String> e = blacklists.keys() ; e.hasMoreElements() ;) {
	         fileNames.add(e.nextElement());
        }
        return fileNames;
    }
	
	/**
	 * gets the blacklist, please note that you have to call init first
	 * @param name the name of the list
	 * @return the blacklist
	 */
	static Blacklist getList(String name) {
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
		try {
			blacklist = new ConcurrentHashMap<String,Pattern>();
			Iterator<?> patternIterator = FileUtils.readLines(this.listFile).iterator();
			while (patternIterator.hasNext()) {
				String pattern = (String) patternIterator.next();
				blacklist.put(pattern, Pattern.compile(pattern));
			}
			this.store();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (PatternSyntaxException e) {
            e.printStackTrace();
		}
	}
	
	/**
	* Adds a new empty blacklist-file
	* @param name name of the new blacklistfile
	*/	
	static Blacklist create(String name) {
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
     * @return if the pattern was successfully removed
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
