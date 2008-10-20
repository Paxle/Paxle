/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 * 
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.parser.msoffice.impl;

import java.util.Hashtable;
import java.util.List;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.parser.ISubParser;
import org.paxle.parser.msoffice.IMsExcelParser;
import org.paxle.parser.msoffice.IMsPowerpointParser;
import org.paxle.parser.msoffice.IMsVisioParser;
import org.paxle.parser.msoffice.IMsWordParser;

public class Activator implements BundleActivator {

	/**
	 * A reference to the {@link BundleContext bundle-context}
	 */
	public static BundleContext bc;	

	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */	
	public void start(BundleContext context) throws Exception {
		bc = context;
		
		Hashtable<String,String[]> props = new Hashtable<String,String[]>();
		List<String> mimeTypes = null;
		ISubParser parser = null;
		
		// MS Word parser
		parser = new MsWordParser();
		mimeTypes = parser.getMimeTypes();
		props.clear();
		props.put(ISubParser.PROP_MIMETYPES, mimeTypes.toArray(new String[mimeTypes.size()]));
		bc.registerService(new String[]{ISubParser.class.getName(),IMsWordParser.class.getName()}, parser, props);
		
		// MS Powerpoint parser
		parser = new MsPowerpointParser();
		mimeTypes = parser.getMimeTypes();
		props.clear();
		props.put(ISubParser.PROP_MIMETYPES, mimeTypes.toArray(new String[mimeTypes.size()]));
		bc.registerService(new String[]{ISubParser.class.getName(),IMsPowerpointParser.class.getName()}, parser, props);	
		
		// MS Visio parser
		parser = new MsVisioParser();
		mimeTypes = parser.getMimeTypes();
		props.clear();
		props.put(ISubParser.PROP_MIMETYPES, mimeTypes.toArray(new String[mimeTypes.size()]));
		bc.registerService(new String[]{ISubParser.class.getName(),IMsVisioParser.class.getName()}, parser, props);
		
		// MS Excel parser
		parser = new MsExcelParser();
		mimeTypes = parser.getMimeTypes();
		props.clear();
		props.put(ISubParser.PROP_MIMETYPES, mimeTypes.toArray(new String[mimeTypes.size()]));
		bc.registerService(new String[]{ISubParser.class.getName(),IMsExcelParser.class.getName()}, parser, props);
	}


	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */		
	public void stop(BundleContext context) throws Exception {
		bc = null;
	}
}