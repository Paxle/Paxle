/*
This file is part of the Paxle project.
Visit http://www.paxle.net for more information.
Copyright 2007-2008 the original author or authors.

Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
or in the file LICENSE.txt in the root directory of the Paxle distribution.

Unless required by applicable law or agreed to in writing, this software is distributed
on an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*/

package org.paxle.parser.lha.impl;

import java.util.Hashtable;
import java.util.List;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.parser.ISubParser;
import org.paxle.parser.lha.ILhaParser;

public class Activator implements BundleActivator {
	
	public void start(BundleContext context) throws Exception {
		final ILhaParser sp = new LhaParser();
		Hashtable<String,String[]> props = new Hashtable<String,String[]>();
		List<String> mimeTypes = sp.getMimeTypes();
		props.put(ISubParser.PROP_MIMETYPES, mimeTypes.toArray(new String[mimeTypes.size()]));
		context.registerService(new String[]{ISubParser.class.getName(),ILhaParser.class.getName()}, sp, props);
	}
	
	public void stop(BundleContext context) throws Exception {
	}
}
