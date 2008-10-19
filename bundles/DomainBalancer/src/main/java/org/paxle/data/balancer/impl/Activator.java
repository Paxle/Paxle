/*
This file is part of the Paxle project.
Visit http://www.paxle.net for more information.
Copyright 2007-2008 the original author or authors.

Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
or in the file LICENSE.txt in the root directory of the Paxle distribution.

Unless required by applicable law or agreed to in writing, this software is distributed
on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*/

package org.paxle.data.balancer.impl;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.paxle.core.data.IDataProvider;
import org.paxle.core.data.IDataSink;
import org.paxle.data.balancer.IDomainBalancer;

public class Activator implements BundleActivator {
	
	/*
	// db.from: passive (da db active)
	// db.to  : active (da db passive und daten hier "generiert" werden)
	// extractor.from: passive (da extractor active)
	// crawler.to: active (da crawler passive)
	
	// ->
	// org.paxle.crawler-queue.out: active
	// org.paxle.crawler-queue.in : passive
	// org.paxle.data.command-db.in: active
	// org.paxle.data.command-db.out: passive
	*/
	
	private ServiceRegistration dataHandlerReg = null;
	private ConfigListener listener = null;
	private DomainBalancer balancer = null;
	
	public void start(BundleContext context) throws Exception {
		final HostManager manager = new HostManager();
		listener = new ConfigListener(context, manager);
		context.addServiceListener(listener, ConfigListener.FILTER);
		
		balancer = new DomainBalancer(manager);
		
		final Hashtable<String,Object> props = new Hashtable<String,Object>();
		props.put(IDataSink.PROP_DATASINK_ID, "org.paxle.data.balancer.sink");
		props.put(IDataProvider.PROP_DATAPROVIDER_ID, "org.paxle.crawler.sink");
		
		dataHandlerReg = context.registerService(new String[] {
				IDataSink.class.getName(),
				IDataProvider.class.getName(),
				IDomainBalancer.class.getName()
		}, balancer, props);
	}
	
	public void stop(BundleContext context) throws Exception {
		if (listener != null) {
			context.removeServiceListener(listener);
			listener = null;
		}
		
		if (balancer != null) {
			balancer.terminate();
			balancer = null;
		}
		
		if (dataHandlerReg != null) {
			dataHandlerReg.unregister();
			dataHandlerReg = null;
		}
	}
}
