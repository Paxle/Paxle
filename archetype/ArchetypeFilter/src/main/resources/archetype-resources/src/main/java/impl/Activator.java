package ${package}.impl;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.core.filter.IFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Activator implements BundleActivator {

	private Log logger = LogFactory.getLog(this.getClass());	
	
	public void start(BundleContext bc) throws Exception {
		this.logger.info("Starting bundle " + bc.getBundle().getSymbolicName());
		
		// creating filter
		IFilter myFilter = new MyFilter();
		
		// specifying filter properties
		final Hashtable<String, String[]> filterProps = new Hashtable<String, String[]>();
		filterProps.put(IFilter.PROP_FILTER_TARGET, new String[] {
				"${targetQueue}#if($targetQueuePosition); pos=${targetQueuePosition}#end"
		});
		
		// registering filter
		bc.registerService(IFilter.class.getName(),myFilter, filterProps);
	}
	

	public void stop(BundleContext bc) throws Exception {
		this.logger.info("Stopping bundle " + bc.getBundle().getSymbolicName());
	}
}
