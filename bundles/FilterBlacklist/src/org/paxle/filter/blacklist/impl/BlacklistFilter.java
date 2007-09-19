package org.paxle.filter.blacklist.impl;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.io.FileUtils;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.queue.ICommand;
import org.paxle.filter.blacklist.IRegexpBlacklistFilter;

/**
 * This is a RegExp-based Blacklistfilter
 * @author Matthias Soehnholz
 *
 */
public class BlacklistFilter implements IRegexpBlacklistFilter {
    
    private static File blacklistDir;
    private static LinkedList<Pattern> blacklist = new LinkedList<Pattern>();
    private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    public BlacklistFilter(File list) {
        blacklistDir = list;
        List lines = null;
        Iterator eter = FileUtils.iterateFiles(blacklistDir, null, false);
        while(eter.hasNext())
        {
            File listFile = (File) eter.next();
            try {
                lines = FileUtils.readLines(listFile);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Iterator eter2 = lines.iterator();
            while(eter2.hasNext()) {
                String temp = (String) eter2.next();
                try {
                    Pattern p = Pattern.compile(temp);
                    blacklist.add(p);
                    System.out.println("Pattern from "+listFile.getName()+" added to blacklist: "+temp);
                } catch (PatternSyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void filter(ICommand command, IFilterContext filterContext) {
        FilterResult result = isListed(command.getLocation());
        if(result.getStatus()==FilterResult.LOCATION_REJECTED) {
            command.setResult(ICommand.Result.Rejected, "rejected by blacklistentry: " + result.getRejectPattern());
            //System.out.println(command.getLocation() + " rejected by blacklistentry: " + result.getRejectPattern());
        }
    }
    
    /**
     * 
     * @param url URL to be checked against blacklist
     * @return returns a String containing the pattern which blacklists the url, returns null otherwise
     */
    private FilterResult isListed(String url) {
        lock.readLock().lock();
        try {
            Iterator<Pattern> eter = blacklist.iterator();
            while(eter.hasNext()) {
                Pattern temp = eter.next();
                Matcher m = temp.matcher(url);
                if(m.matches()) {
                    return new FilterResult(FilterResult.LOCATION_REJECTED, temp.pattern());
                }
            }
        } finally {
            lock.readLock().unlock();            
        }
        return new FilterResult(FilterResult.LOCATION_OKAY, null);
    }
    
    /**
     * Adds a new blacklist-pattern to the selected blacklistfile
     * @param pattern blacklistpattern to be added 
     * @param listFileName name of the target blacklistfile
     */
    public static void addPattern(String pattern, String listFileName) {
        lock.writeLock().lock();
        try {
            Pattern p = Pattern.compile(pattern);
            blacklist.add(p);
            System.out.println("Pattern from "+listFileName+" added to blacklist: "+pattern);
            List tempList = FileUtils.readLines(new File(blacklistDir, listFileName));
            FileUtils.writeLines(new File(blacklistDir, listFileName), tempList);
        } catch (PatternSyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Adds a new empty blacklist-file
     * @param listName name of the new blacklistfile
     */
    public static void addList(String listName) {
        try {
            FileUtils.touch(new File(blacklistDir, listName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    class FilterResult {
        
        final static int LOCATION_OKAY = 0;
        final static int LOCATION_REJECTED = 1;
        
        private int status;
        private String rejectPattern = null;
        
        FilterResult(int resultStatus, String pattern) {
            setStatus(resultStatus);
            setRejectPattern(pattern);
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public int getStatus() {
            return status;
        }

        public void setRejectPattern(String rejectPattern) {
            this.rejectPattern = rejectPattern;
        }

        public String getRejectPattern() {
            return rejectPattern;
        }
    }
}