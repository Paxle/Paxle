package org.paxle.core.io;

import java.net.URL;
import java.util.List;
import java.util.Locale;

public interface IResourceBundleTool {
	
	public List<URL> getLocaleURL(String resourceBundleBase);
	public List<String> getLocaleList(String resourceBundleBase, Locale defaultLocale);
	public String[] getLocaleArray(String resourceBundleBase, Locale defaultLocale);
}
