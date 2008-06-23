package org.paxle.filter.plaintextdumper.impl;

import java.io.File;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.core.filter.IFilter;

public class Activator implements BundleActivator {
	private static String PATH = "plaintext-dumper";	

	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */	
	public void start(BundleContext bc) throws Exception {
		// create data-dir if required
		final File dir = new File(PATH);
		if (!dir.exists()) dir.mkdirs();
		
		/* ==========================================================
		 * Register Services provided by this bundle
		 * ========================================================== */		
		final Hashtable<String, String[]> filterProps = new Hashtable<String, String[]>();
		filterProps.put(IFilter.PROP_FILTER_TARGET, new String[] {
				// apply filter to the parser-output-queue at MAX-Position
				String.format("org.paxle.parser.out; %s=%d", IFilter.PROP_FILTER_TARGET_POSITION,Integer.MAX_VALUE)
		});
		bc.registerService(IFilter.class.getName(), new PlaintextDumperFilter(dir), filterProps);
	}

	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */	
	public void stop(BundleContext context) throws Exception {
		// nothing todo here
	}
}