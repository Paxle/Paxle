package org.paxle.core.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.metatype.AttributeDefinition;
import org.osgi.service.metatype.MetaTypeProvider;
import org.osgi.service.metatype.ObjectClassDefinition;
import org.paxle.core.IMWComponent;
import org.paxle.core.data.IDataSink;
import org.paxle.core.data.IDataSource;
import org.paxle.core.queue.impl.InputQueue;
import org.paxle.core.queue.impl.OutputQueue;
import org.paxle.core.threading.IMaster;
import org.paxle.core.threading.IPool;
import org.paxle.core.threading.impl.Pool;

/**
 * @see IMWComponent
 */
public class MWComponent<Data> implements IMWComponent<Data>, ManagedService, MetaTypeProvider {
	public static final String PROP_POOL_MIN_IDLE = "pool.minIdle";
	public static final String PROP_POOL_MAX_IDLE = "pool.maxIdle";
	public static final String PROP_POOL_MAX_ACTIVE = "pool.maxActive";
	
	private IMaster master;
	private Pool<Data> pool;
	private InputQueue<Data> inQueue;
	private OutputQueue<Data> outQueue;
	
	/**
	 * The unique ID of this component.
	 * This is needed for CM
	 */
	private String componentID;
	
	/**
	 * The name of this component instance.
	 * This is needed for CM.
	 */
	private String componentName;
	
	/**
	 * A textual description of this component.
	 * This is needed by CM.
	 */
	private String componentDescription;
	
	public MWComponent(IMaster master, Pool<Data> pool, InputQueue<Data> inQueue, OutputQueue<Data> outQueue) {
		if (master == null) throw new NullPointerException("The master thread is null.");
		if (pool == null) throw new NullPointerException("The thread-pool is null");
		if (inQueue == null) throw new NullPointerException("The input-queue is null");
		if (outQueue == null) throw new NullPointerException("The output-queue is null");
		this.master = master;
		this.pool = pool;
		this.inQueue = inQueue;
		this.outQueue = outQueue;		
	}

	void setComponentID(String componentID) {
		this.componentID = componentID;
	}
	
	void setComponentName(String componentName) {
		this.componentName = componentName;
	}
	
	void setComponentDescription(String componentDescription) {
		this.componentDescription = componentDescription;
	}
	
	/**
	 * {@inheritDoc}
	 * @see IMWComponent#getDataSink()
	 */
	public IDataSink<Data> getDataSink() {
		return this.inQueue;
	}

	/**
	 * {@inheritDoc}
	 * @see IMWComponent#getDataSource()
	 */
	public IDataSource<Data> getDataSource() {
		return this.outQueue;
	}	

	/**
	 * {@inheritDoc}
	 * @see IMWComponent#getMaster()
	 */
	public IMaster getMaster() {
		return this.master;
	}
	
	/**
	 * {@inheritDoc}
	 * @see IMWComponent#getPool()
	 */
	public IPool<Data> getPool() {
		return this.pool;
	}

	/**
	 * {@inheritDoc}
	 * @see IMWComponent#terminate()
	 */
	public void terminate() {
		// terminating the master-thread (this automatically closes the worker-pool)
		this.master.terminate();
		
		// TODO: closing the queues
	}
	
	/**
	 * {@inheritDoc}
	 * @see IMWComponent#isPaused()
	 */
	public boolean isPaused() {
		return this.master.isPaused();
	}
	
	/**
	 * {@inheritDoc}
	 * @see IMWComponent#pause()
	 */	
	public void pause(){
		this.master.pauseMaster();
	}
	
	/**
	 * {@inheritDoc}
	 * @see IMWComponent#resume()
	 */	
	public void resume() {
		this.master.resumeMaster();
	}
	
	/**
	 * {@inheritDoc}
	 * @see IMWComponent#getPPM()
	 */
	public int getPPM() {
		return this.master.getPPM();
	}

	/**
	 * {@inheritDoc}
	 * @see IMWComponent#getPPM()
	 */	
	public void processNext() {
		this.master.processNext();
	}

	/**
	 * @see IMWComponent#getActiveJobs()
	 */
	public List<Data> getActiveJobs() {
		return this.pool.getActiveJobs();
	}
	
	/**
	 * @see IMWComponent#getActiveJobCount()
	 */
	public int getActiveJobCount() {
		return this.pool.getActiveJobCount();
	}
	
	/**
	 * @see IMWComponent#getEnqueuedJobs()
	 */
	@SuppressWarnings("unchecked")
	public List<Data> getEnqueuedJobs() {
		return (List<Data>) Arrays.asList(this.inQueue.toArray());
	}
	
	/**
	 * @see IMWComponent#getEnqueuedJobCount()
	 */
	public int getEnqueuedJobCount() {
		return this.inQueue.size();
	}
	
	/**
	 * @return the default configuration of this service
	 */
	public Hashtable<String,Object> getDefaults() {
		Hashtable<String,Object> defaults = new Hashtable<String,Object>();
		
		defaults.put(Constants.SERVICE_PID, this.componentID);
		defaults.put(PROP_POOL_MIN_IDLE, new Integer(0));
		defaults.put(PROP_POOL_MAX_IDLE, new Integer(8));
		defaults.put(PROP_POOL_MAX_ACTIVE, new Integer(8));
		  
		return defaults;
	}

	public void updated(Dictionary configuration) throws ConfigurationException {
		if (configuration == null ) {
			/*
			 * Generate default configuration
			 */
			configuration = this.getDefaults();
		}
		
		this.pool.setMinIdle(((Integer)configuration.get(PROP_POOL_MIN_IDLE)).intValue());
		this.pool.setMaxIdle(((Integer)configuration.get(PROP_POOL_MIN_IDLE)).intValue());
		this.pool.setMaxActive(((Integer)configuration.get(PROP_POOL_MAX_ACTIVE)).intValue());
	}

	public String[] getLocales() {
		return new String[]{"en"};
	}

	public ObjectClassDefinition getObjectClassDefinition(String id, String locale) {
		final ArrayList<AD> ads = new ArrayList<AD>();
		ads.add(new AD(PROP_POOL_MIN_IDLE, "Min. idle threads","", new String[]{"0"}));
		ads.add(new AD(PROP_POOL_MAX_IDLE, "Max. idle threads","", new String[]{"8"}));
		ads.add(new AD(PROP_POOL_MAX_ACTIVE, "Max. active threads","", new String[]{"8"}));
		
		final String PID = this.componentID;
		final String mame = this.componentName;
		
		// create metadata
		ObjectClassDefinition ocd = new ObjectClassDefinition() {
			public AttributeDefinition[] getAttributeDefinitions(int filter) {
				return ads.toArray(new MWComponent.AD[ads.size()]);
			}

			public String getDescription() {
				return "Master/Worker thread pool";
			}

			public String getID() {
				return PID;
			}

			public InputStream getIcon(int size) throws IOException {
				return null;
			}

			public String getName() {
				return mame;
			}
		};

		return ocd;
	}
	
	public class AD implements AttributeDefinition {
		private String ID;
		private String name;
		private String description;		
		private String[] defaultValue = null;
		
		public AD(String ID, String name, String description, String[] defaultValues) {
			this.ID = ID;
			this.name = name;
			this.description = description;
			this.defaultValue = defaultValues;
		}
		
		public String[] getDefaultValue() { return this.defaultValue; }
		public String getDescription() { return this.description; }
		public String getID() { return ID; }
		public String getName() { return this.name; }
		public int getCardinality() { return 0; }
		public String[] getOptionLabels() { return null; }
		public String[] getOptionValues() { return null; }
		public int getType() { return AttributeDefinition.INTEGER; }
		public String validate(String value) { return null; }				
	}
	
}
