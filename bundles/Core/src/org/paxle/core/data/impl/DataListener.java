package org.paxle.core.data.impl;

import java.util.Arrays;
import java.util.HashSet;

import org.osgi.framework.BundleContext;
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
	
	public static String DATASOURCE_FILTER = "(objectClass=" + DATASOURCE_CLASS +")";
	public static String DATASINK_FILTER = "(objectClass=" + DATASINK_CLASS +")";
	public static String DATAPROVIDER_FILTER = "(objectClass=" + DATAPROVIDER_CLASS +")";
	public static String DATACONSUMER_FILTER = "(objectClass=" + DATACONSUMER_CLASS +")";
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
		ServiceReference reference = event.getServiceReference();
		String interfaceName = ((String[])reference.getProperty("objectClass"))[0];
		if (!CLASSES.contains(interfaceName)) return;
		
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
