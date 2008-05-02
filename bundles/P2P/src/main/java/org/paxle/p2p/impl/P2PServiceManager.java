package org.paxle.p2p.impl;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.paxle.p2p.services.impl.AService;

public class P2PServiceManager implements ServiceListener {
	private Hashtable<Filter, ArrayList<Class<? extends AService>>> services;
	
	private Hashtable<Class<? extends AService>,HashSet<Filter>> missingDeps;
	
	private Hashtable<Class<? extends AService>,HashMap<Filter,Object>> fulfilledDeps;
	
	private Hashtable<Class<? extends AService>,AService> startedServices;
	
	private Hashtable<Class<? extends AService>,ServiceRegistration> registeredServices;
	
	/**
	 * The {@link BundleContext osgi-bundle-context} of this bundle
	 */
	private BundleContext context = null;
	
	private P2PManager p2pManager = null;
	
	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());

	public P2PServiceManager(P2PManager p2pManager, BundleContext context) throws InvalidSyntaxException {
		this.p2pManager = p2pManager;
		this.context = context;
		
		this.services = new Hashtable<Filter, ArrayList<Class<? extends AService>>>();
		this.missingDeps = new Hashtable<Class<? extends AService>, HashSet<Filter>>();
		this.fulfilledDeps = new Hashtable<Class<? extends AService>, HashMap<Filter,Object>>();
		this.startedServices = new Hashtable<Class<? extends AService>, AService>();
		this.registeredServices = new Hashtable<Class<? extends AService>, ServiceRegistration>();
	}	
	
	public void serviceChanged(ServiceEvent event) {
		// get the reference to the service 
		ServiceReference reference = event.getServiceReference();
		this.serviceChanged(reference, event.getType());
	}

	private void serviceChanged(ServiceReference reference, int eventType) {
		if (reference == null) return;		

		for (Filter filter : services.keySet()) {
			if (!filter.match(reference)) continue;
			
			ArrayList<Class<? extends AService>> serviceClasses = services.get(filter);
			for (Class<? extends AService> serviceClass : serviceClasses) {
				HashSet<Filter> serviceMissingDeps = this.missingDeps.get(serviceClass);
				HashMap<Filter, Object> serviceFulfilledDeps = this.fulfilledDeps.get(serviceClass);
				
				if (eventType == ServiceEvent.REGISTERED) {
					Object serviceObject = this.context.getService(reference);
					serviceFulfilledDeps.put(filter, serviceObject);
					serviceMissingDeps.remove(filter);
					
					if (serviceMissingDeps.size() == 0) {
						try {
							// TODO: create and start service
							Constructor<? extends AService> c = serviceClass.getConstructor(new Class[]{P2PManager.class});
							AService s = c.newInstance(new Object[]{this.p2pManager});
							
							List<Object> deps = new ArrayList<Object>(serviceFulfilledDeps.values());
							s.start(deps);
							this.startedServices.put(serviceClass, s);
							
							// getting the interfaces to export to the framework
							String[] interfaces = s.getExportedInterfaces().toArray(new String[0]);
							
							// registering the service to the framework
							ServiceRegistration sReg = context.registerService(interfaces, s, null);
							this.registeredServices.put(serviceClass, sReg);
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}					
				} else if (eventType == ServiceEvent.UNREGISTERING) {
					serviceMissingDeps.add(filter);
					serviceFulfilledDeps.remove(filter);
					
					if (serviceMissingDeps.size() > 0) {
						if (this.registeredServices.containsKey(serviceClass)) {
							ServiceRegistration sReg = this.registeredServices.remove(serviceClass);
							sReg.unregister();
						}
						
						// TODO: stop service
						if (this.startedServices.containsKey(serviceClass)) {
							// terminate service
							AService p2pService = this.startedServices.remove(serviceClass);
							p2pService.terminate();
							
						}
					}
				} else if (eventType == ServiceEvent.MODIFIED) {
					// service properties have changed
				}	
			}
		}
	}
	
	public void addService(Filter[] filters, Class<? extends AService> serviceClass) {
		if (filters == null) throw new NullPointerException("The filter expression is null");
		if (serviceClass == null) throw new NullPointerException("The service class is null");

		for (Filter filter : filters) {
			ArrayList<Class<? extends AService>> serviceList = null;
			if (this.services.containsKey(filter)) {
				serviceList = this.services.get(filter);
			} else {
				serviceList = new ArrayList<Class<? extends AService>>();
			}
			this.services.put(filter, serviceList);
			
			serviceList.add(serviceClass);
		}

		HashSet<Filter> serviceMissingDeps = new HashSet<Filter>();
		HashMap<Filter, Object> serviceFulfilledDeps = new HashMap<Filter, Object>();

		for (Filter filter : filters) {
			serviceMissingDeps.add(filter);
		}

		this.missingDeps.put(serviceClass, serviceMissingDeps);
		this.fulfilledDeps.put(serviceClass, serviceFulfilledDeps);
	}
	
	public void stopAllServices() {
		for (AService service : this.startedServices.values()) {
			service.terminate();
		}
		
		this.startedServices.clear();
	}
}
