package org.paxle.filter.languageidentification.impl;

import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.core.filter.IFilter;

public class Activator implements BundleActivator {

	private Log logger = LogFactory.getLog(this.getClass());
	
	public void start(BundleContext context) throws Exception {		
		final Hashtable<String, String[]> filterProps = new Hashtable<String, String[]>();
		filterProps.put(IFilter.PROP_FILTER_TARGET, new String[] {
				"org.paxle.parser.out; " + IFilter.PROP_FILTER_TARGET_POSITION + "=" + (Integer.MAX_VALUE-1000)
		});
		
		LanguageManager lngmanager = new LanguageManager();
		
		@SuppressWarnings("unchecked")
		Enumeration<URL> profiles = context.getBundle().findEntries("/profiles/", "*.txt", false);
		
		logger.info("Loading language profiles...");
		while (profiles.hasMoreElements()) {
			URL currdef = profiles.nextElement();
			lngmanager.loadNewLanguage(currdef);
		}
		logger.info("Loaded " + lngmanager.getNumberOfRegisteredProfile() + " language profiles");
		
		context.registerService(IFilter.class.getName(), lngmanager, filterProps);
	}

	public void stop(BundleContext context) throws Exception {
		logger.info("Bundle gestoppt");
	}

}