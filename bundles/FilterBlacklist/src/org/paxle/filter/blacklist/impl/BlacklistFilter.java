package org.paxle.filter.blacklist.impl;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.io.FileUtils;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.queue.ICommand;
import org.paxle.filter.blacklist.IRegexpBlacklistFilter;

public class BlacklistFilter implements IRegexpBlacklistFilter {
    
    private static File blacklistDir;
    private static LinkedList<Pattern> blacklist = new LinkedList<Pattern>();
    
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
        String reason = isListed(command.getLocation());
        if(!reason.equals("")) {
            command.setResult(ICommand.Result.Rejected, "rejected by blacklistentry: " + reason);
        }
    }
    
    private String isListed(String url) {
        Iterator<Pattern> eter = blacklist.iterator();
        while(eter.hasNext()) {
            Pattern temp = eter.next();
            Matcher m = temp.matcher(url);
            if(m.matches()) return temp.pattern();
        }
        return "";
    }
    
    public static void addPattern(String pattern, String listFileName) {
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
        }
    }
    
    public static void addList(String listName) {
        try {
            FileUtils.touch(new File(blacklistDir, listName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}