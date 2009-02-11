/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package org.paxle.se.provider.rsssearch.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.paxle.se.search.ISearchProvider;


public class RssSearchProviderManager {	
	/**
	 * All currently registered RSS-search providers
	 */
	public static List<ServiceRegistration> providers = new ArrayList<ServiceRegistration>();
	
	/**
	 * For logging
	 */
	private final Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * The bundle-context used to register new {@link RssSearchProvider providers}
	 */
	private final BundleContext bc;
	
	/**
	 * The data-file containing all known provider-URL
	 */
	private final File providerFile;
	
	public RssSearchProviderManager(BundleContext bc, File providerFile) throws IOException {
		this.bc = bc;
		this.providerFile = providerFile;
		
		ArrayList<String> urls = this.getUrls();
		this.registerSearchers(urls);	
	}
	
	/**
	 * read the list of RSS-URLs
	 * @return an ArrayList with URLs
	 * @throws IOException
	 */
	public ArrayList<String> getUrls() throws IOException{
		/*
		 * Creating the default provider list
		 */
		if(!this.providerFile.exists()){
			ArrayList<String> defaults=new ArrayList<String>();
			defaults.add("http://del.icio.us/rss/tag/%s");
			defaults.add("http://www.mister-wong.com/rss/tags/%s");
			setUrls(defaults);
		}
		
		// reading the providers from file
		ArrayList<String> list=new ArrayList<String>();
		BufferedReader is = null;
		try {
			is = new BufferedReader(new InputStreamReader(new FileInputStream(this.providerFile)));
			
			String line;			
			while((line=is.readLine())!=null){
				list.add(line);
			}
		} finally {
			if (is != null) try { is.close(); } catch (Exception e) {/* ignore this */}
		}
		return list;
	}
	


	public void setUrls(ArrayList<String> urls) throws IOException{
		if(!this.providerFile.exists()){
			this.providerFile.createNewFile();
		}
		
		PrintWriter pr = null;
		try {
			pr = new PrintWriter(this.providerFile);
			for (String url : urls) {
				pr.println(url);
			}
		} finally {
			if (pr != null) pr.close();
		}
	}
	

	public void registerSearchers(ArrayList<String> urls){
		Iterator<ServiceRegistration> regs = providers.iterator();
		while (regs.hasNext()) {
			ServiceRegistration sr = regs.next();
			try{
				sr.unregister();				
			} catch(IllegalStateException e) {
				this.logger.error(e);
			}
			regs.remove();
		}
		
		for (String url : urls) {
			// create a new provider
			RssSearchProvider provider = new RssSearchProvider(url);
			
			// register as a service to the framework
			ServiceRegistration registration = this.bc.registerService(
					ISearchProvider.class.getName(),
					provider,
					new Hashtable<String,String>()
			);
			
			// remember it in the internal list
			providers.add(registration);
		}
	}
}
