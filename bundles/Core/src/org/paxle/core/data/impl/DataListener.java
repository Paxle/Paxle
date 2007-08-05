package org.paxle.core.data.impl;

import java.util.Arrays;
import java.util.HashSet;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.paxle.core.data.IDataConsumer;
import org.paxle.core.data.IDataProvider;
import org.paxle.core.data.IDataSink;
import org.paxle.core.data.IDataSource;
import org.paxle.core.filter.IFilter;

public class DataListener implements ServiceListener {
	public static String DATASOURCE_CLASS = IDataSource.class.getName();
	public static String DATASINK_CLASS = IDataSink.class.getName();
	public static String DATAPROVIDER_CLASS = IDataProvider.class.getName();
	public static String DATACONSUMER_CLASS = IDataConsumer.class.getName();
	public static HashSet<String> CLASSES = new HashSet<String>(Arrays.asList(new String[]{
		DATASOURCE_CLASS,
		DATASINK_CLASS,
		DATAPROVIDER_CLASS,
		DATACONSUMER_CLASS
	}));
	
	public static String DATASOURCE_FILTER   = "(" + Constants.OBJECTCLASS + "=" + DATASOURCE_CLASS   +")";
	public static String DATASINK_FILTER     = "(" + Constants.OBJECTCLASS + "=" + DATASINK_CLASS     +")";
	public static String DATAPROVIDER_FILTER = "(" + Constants.OBJECTCLASS + "=" + DATAPROVIDER_CLASS +")";
	public static String DATACONSUMER_FILTER = "(" + Constants.OBJECTCLASS + "=" + DATACONSUMER_CLASS +")";
	public static String[] FILTERS = new String[]{
		DATASOURCE_FILTER,
		DATASINK_FILTER,
		DATAPROVIDER_FILTER,
		DATACONSUMER_FILTER
	};
	
	private DataManager manager = null;
	
	/**
	 * The {@link BundleContext osgi-bundle-context} of this bundle
	 */
	private BundleContext context = null;	
	
	public DataListener(DataManager dataManager, BundleContext context) {
		this.manager = dataManager;
		this.context = context;
		for (String className : FILTERS) this.detect(className);
	}
	
	private void detect(String className) {
		try {
			ServiceReference[] refs = context.getServiceReferences(className,"()");
			if (refs == null) return;
			for (ServiceReference ref : refs) {
				
				// get the filter
				IFilter filter = (IFilter) this.context.getService(ref);	
				System.out.println("New filter '" + filter.getClass().getName() + "' registered.");
				
				// TODO: what to do with this filters?
			}
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}			
	}
	
	public void serviceChanged(ServiceEvent event) {
		// get the reference to the service 
		ServiceReference reference = event.getServiceReference();
		
		// get the names of the registered interfaces 
		String[] interfaceNames = ((String[])reference.getProperty(Constants.OBJECTCLASS));
		
		// loop through the interfaces
		for (String interfaceName : interfaceNames) {
			if (!CLASSES.contains(interfaceName)) continue;

			String id = (String) reference.getProperty(interfaceName + ".id");

			int eventType = event.getType();
			if (eventType == ServiceEvent.REGISTERED) {
				// get the filter
				Object service = this.context.getService(reference);
				this.manager.add(id, interfaceName, service);
			} else if (eventType == ServiceEvent.UNREGISTERING) {
				this.manager.remove(id);
			} else if (eventType == ServiceEvent.MODIFIED) {

			}	
		}
	}

}
