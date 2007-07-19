package org.paxle.crawler.impl;

import java.io.InputStream;
import java.util.Date;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.paxle.core.IMWComponent;
import org.paxle.core.IMWComponentManager;
import org.paxle.core.data.IDataSink;
import org.paxle.core.data.IDataSource;
import org.paxle.core.filter.IFilter;
import org.paxle.core.queue.ICommand;
import org.paxle.core.threading.IMaster;
import org.paxle.core.threading.IWorker;
import org.paxle.core.threading.IWorkerFactory;
import org.paxle.crawler.ISubCrawler;
import org.paxle.crawler.ISubCrawlerManager;

public class Activator implements BundleActivator {

	/**
	 * A reference to the {@link BundleContext bundle-context}
	 */
	public static BundleContext bc;	
	
	/**
	 * A reference to the {@link IMWComponent master-worker-component} used
	 * by this bundle.
	 */
	public static IMWComponent mwComponent;
	
	/**
	 * A component to manage {@link ISubCrawler sub-crawlers}
	 */
	public static SubCrawlerManager subCrawlerManager = null;

	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */
	public void start(BundleContext context) throws Exception {		
		bc = context;
		subCrawlerManager = new SubCrawlerManager(); 
		
		/* ==========================================================
		 * Register Service Listeners
		 * ========================================================== */		
		// registering a service listener to notice if a new sub-crawler was (un)deployed
		bc.addServiceListener(new SubCrawlerListener(subCrawlerManager, bc),SubCrawlerListener.FILTER);		
		
		/* ==========================================================
		 * Get services provided by other bundles
		 * ========================================================== */			
		// getting a reference to the threadpack generator service
		ServiceReference reference = bc.getServiceReference(IMWComponentManager.class.getName());

		if (reference != null) {
			// getting the service class instance
			IMWComponentManager componentFactory = (IMWComponentManager)bc.getService(reference);
			IWorkerFactory<IWorker> workerFactory = new WorkerFactory(subCrawlerManager);
			mwComponent = componentFactory.createComponent(workerFactory, 5);
		}		
		
		/* ==========================================================
		 * Register Services provided by this bundle
		 * ========================================================== */
		// register the SubCrawler-Manager as service
		bc.registerService(ISubCrawlerManager.class.getName(), subCrawlerManager, null);
		
		// register the protocol filter as service
		// TODO: which properties should be set for the filter service?
		bc.registerService(IFilter.class.getName(), new ProtocolFilter(subCrawlerManager), null);
		
		// publish data-sink
		Hashtable<String,String> dataSinkProps = new Hashtable<String, String>();
		dataSinkProps.put(IDataSink.PROP_DATASINK_ID, bc.getBundle().getSymbolicName() + ".sink");		
		bc.registerService(IDataSink.class.getName(), mwComponent.getDataSink(), dataSinkProps);
		
		// publish data-source
		Hashtable<String,String> dataSourceProps = new Hashtable<String, String>();
		dataSourceProps.put(IDataSource.PROP_DATASOURCE_ID, bc.getBundle().getSymbolicName() + ".source");			
		bc.registerService(IDataSource.class.getName(), mwComponent.getDataSource(), dataSourceProps);
		
		/*
		 * TODO: just for debugging
		 */
		mwComponent.getDataSink().putData(new Command("http://www.test.at"));
	}

	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */	
	public void stop(BundleContext context) throws Exception {
		// shutdown the thread pool
		if (mwComponent != null) {
			IMaster master = mwComponent.getMaster();
			master.terminate();
		}
		
		// cleanup
		bc = null;
		subCrawlerManager = null;
	}
}

class Command implements ICommand {
	private String location = null;

	public Command(String location) {
		this.location = location;
	}
	
	public void addHeadline(String headline) {
		// TODO Auto-generated method stub
		
	}

	public void addKeyword(String keyword) {
		// TODO Auto-generated method stub
		
	}

	public void addReference(String ref, String name) {
		// TODO Auto-generated method stub
		
	}

	public void addReferenceImage(String ref, String name) {
		// TODO Auto-generated method stub
		
	}

	public void addText(CharSequence text) {
		// TODO Auto-generated method stub
		
	}

	public InputStream getBodyRaw() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getCharset() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getLocation() {
		return this.location;
	}

	public String getMimeType() {
		// TODO Auto-generated method stub
		return null;
	}

	public Result getResult() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean isCharsetSet() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isMimeTypeSet() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setAuthor(String author) {
		// TODO Auto-generated method stub
		
	}

	public void setLanguage(Language language) {
		// TODO Auto-generated method stub
		
	}

	public void setLastChanged(Date date) {
		// TODO Auto-generated method stub
		
	}

	public void setResult(Result status) {
		// TODO Auto-generated method stub
		
	}

	public void setSummary(String summary) {
		// TODO Auto-generated method stub
		
	}

	public void setTitle(String title) {
		// TODO Auto-generated method stub
		
	}

	public void setTopic(Topic topic) {
		// TODO Auto-generated method stub
		
	}
	
}