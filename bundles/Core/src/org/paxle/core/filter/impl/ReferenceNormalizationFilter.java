package org.paxle.core.filter.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.queue.ICommand;

public class ReferenceNormalizationFilter implements IFilter {
	private static final Pattern PATH_PATTERN = Pattern.compile("(/[^/]+(?<!/\\.{1,2})/)[.]{2}(?=/|$)|/\\.(?=/)|/(?=/)");

	public void filter(ICommand command, IFilterContext filterContext) {
		// TODO Auto-generated method stub
	
	}
	
	public String normalizeLocation(String location) {
		// TODO:
		return location;
	}
	
    private String resolveBackpath(String path) {
    	if (path == null || path.length() == 0) return "/";
        if (path.length() == 0 || path.charAt(0) != '/') { path = "/" + path; }

        Matcher matcher = PATH_PATTERN.matcher(path);
        while (matcher.find()) {
            path = matcher.replaceAll("");
            matcher.reset(path);
        }
        
        return path.equals("")?"/":path;
    }

}
