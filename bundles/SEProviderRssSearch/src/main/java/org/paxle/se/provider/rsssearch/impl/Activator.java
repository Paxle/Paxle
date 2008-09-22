
package org.paxle.se.provider.rsssearch.impl;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.se.search.ISearchProvider;

public class Activator implements BundleActivator {

	/**
	 * A reference to the {@link BundleContext bundle-context}
	 */	
	public static BundleContext bc = null;

	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */		
	public void start(BundleContext context) throws Exception {
		bc = context;
		
		File rssList=new File("rssProviders.txt");
		if(!rssList.exists()){
			rssList.createNewFile();
			BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(rssList));
			os.write("http://del.icio.us/rss/tag/%s\n".getBytes());
			os.write("http://www.mister-wong.com/rss/tags/%s\n".getBytes());
			os.close();
		}
		BufferedReader is=new BufferedReader(new InputStreamReader(new FileInputStream(rssList)));
		String line;
		ArrayList<ISearchProvider> providers=new ArrayList<ISearchProvider>();
		while((line=is.readLine())!=null){
			providers.add(new RssSearchProvider(line));
			bc.registerService(ISearchProvider.class.getName(), providers.get(providers.size()-1), new Hashtable<String,String>());
		}
		is.close();
        
	}

	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */		
	public void stop(BundleContext context) throws Exception {
		bc = null;
	}
}
