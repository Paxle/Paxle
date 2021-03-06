/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2010 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.core.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Constants;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.AttributeDefinition;
import org.osgi.service.metatype.MetaTypeProvider;
import org.osgi.service.metatype.ObjectClassDefinition;
import org.osgi.service.monitor.Monitorable;
import org.osgi.service.monitor.StatusVariable;
import org.paxle.core.IMWComponent;
import org.paxle.core.MWComponentEvent;
import org.paxle.core.data.IDataSink;
import org.paxle.core.data.IDataSource;
import org.paxle.core.queue.InputQueue;
import org.paxle.core.queue.OutputQueue;
import org.paxle.core.threading.IMaster;
import org.paxle.core.threading.IPool;
import org.paxle.core.threading.impl.Pool;

/**
 * @see IMWComponent
 */
public class MWComponent<Data> implements IMWComponent<Data>, ManagedService, MetaTypeProvider, Monitorable, EventHandler {
	/* ========================================================================
	 * MONITORABLE properties
	 * ======================================================================== */
	private static final String VAR_NAME_PPM = "ppm";	
	private static final String VAR_NAME_PAUSED = "status.paused";
	private static final String VAR_NAME_JOBS_ENQUEUED = "jobs.enqueued";
	private static final String VAR_NAME_JOBS_ACTIVE = "jobs.active";
	private static final String VAR_NAME_JOBS_MAX = "jobs.max";
	private static final String VAR_NAME_JOBS_TOTAL = "jobs.total";
	private static final String VAR_NAME_JOB_DELAY = "job.delay";
	private static final String VAR_NAME_STAT_CODE = "state.code";
	
	/**
	 * The names of all {@link StatusVariable status-variables} supported by this {@link Monitorable}
	 */
	@SuppressWarnings("serial")
	private static final HashSet<String> VAR_NAMES =  new HashSet<String>(){{
		add(VAR_NAME_PPM);
		add(VAR_NAME_PAUSED);
		add(VAR_NAME_JOBS_ENQUEUED);
		add(VAR_NAME_JOBS_ACTIVE);
		add(VAR_NAME_JOBS_MAX);
		add(VAR_NAME_JOBS_TOTAL);
		add(VAR_NAME_JOB_DELAY);
		add(VAR_NAME_STAT_CODE);
	}};
	
	/**
	 * Descriptions of all {@link StatusVariable status-variables} supported by this {@link Monitorable}
	 */
	private final ResourceBundle monitorableRb = ResourceBundle.getBundle("OSGI-INF/l10n/MWComponent");
	
	/* ========================================================================
	 * CM properties
	 * ======================================================================== */	
	public static final String PROP_POOL_MIN_IDLE = "pool.minIdle";
	public static final String PROP_POOL_MAX_IDLE = "pool.maxIdle";
	public static final String PROP_POOL_MAX_ACTIVE = "pool.maxActive";
	public static final String PROP_DELAY = "master.delay";
	public static final String PROP_DELAY_DELTA = "master.delay.delta";
	public static final String PROP_ACTIVATED = "master.activated";
	
	private static final String PROP_STATE_ACTIVE = "state.active";
	private static final String PROP_STATE_CODE = "state.code";
	
	/**
	 * Master thread
	 */
	private final IMaster<Data> master;
	
	/**
	 * Worker thread pool
	 */
	private final Pool<Data> pool;
	
	/**
	 * Component input-queue
	 */
	private final InputQueue<Data> inQueue;
	
	/**
	 * Component output-queue
	 */
	private final OutputQueue<Data> outQueue;
	
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
	
	/**
	 * A class to send {@link MWComponentEvent}s
	 */
	private MWComponentEventSender eventSender;
	
	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());
	
	private Configuration configuration;
	
	private final String[] locales;
	
	private String stateCode = "OK";
	
	public MWComponent(IMaster<Data> master, Pool<Data> pool, InputQueue<Data> inQueue, OutputQueue<Data> outQueue, final String[] locales) {
		if (master == null) throw new NullPointerException("The master thread is null.");
		if (pool == null) throw new NullPointerException("The thread-pool is null");
		if (inQueue == null) throw new NullPointerException("The input-queue is null");
		if (outQueue == null) throw new NullPointerException("The output-queue is null");
		
		this.master = master;
		this.pool = pool;
		this.inQueue = inQueue;
		this.outQueue = outQueue;
		this.locales = locales==null?null:locales.clone();
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
	
	void setEventSender(MWComponentEventSender eventSender) {
		this.eventSender = eventSender;
	}
	
	void setConfiguration(final Configuration configuration) {
		this.configuration = configuration;
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
	public IMaster<Data> getMaster() {
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
	@SuppressWarnings("unchecked")
	public void pause(){
		try {
			final Dictionary props = configuration.getProperties();
			props.put(getFullPropertyName(PROP_STATE_ACTIVE), Boolean.FALSE);
			this.configuration.update(props);
		} catch (IOException e) { 
			this.logger.error(e);
		}
	}
	
	/**
	 * {@inheritDoc}
	 * @see IMWComponent#resume()
	 */
	@SuppressWarnings("unchecked")
	public void resume() {
		try {
			final Dictionary props = configuration.getProperties();
			props.put(getFullPropertyName(PROP_STATE_ACTIVE), Boolean.TRUE);
			this.configuration.update(props);
		} catch (IOException e) { 
			this.logger.error(e);
		}

	}
	
	private void setActiveState(Boolean active) {
		if (active.booleanValue()) {
			this.master.resumeMaster();
			if (this.eventSender != null) {
				this.eventSender.sendResumedEvent(this.componentID);
			}			
		} else {
			this.master.pauseMaster();
			if (this.eventSender != null) {
				this.eventSender.sendPausedEvent(this.componentID);
			}
		}
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
	 * @see IMWComponent#processNext()
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
	 * @see IMWComponent#process(Object)
	 */
	public void process(Data cmd) throws Exception {
		this.master.process(cmd);
	};
	
	private String getFullPropertyName(final String prop) {
		return this.componentID + '.' + prop;
	}
	
	/**
	 * @return the default configuration of this service
	 */
	public Hashtable<String,Object> getDefaults() {
		Hashtable<String,Object> defaults = new Hashtable<String,Object>();
		
		defaults.put(Constants.SERVICE_PID, this.componentID);
		defaults.put(getFullPropertyName(PROP_POOL_MIN_IDLE), Integer.valueOf(0));
		defaults.put(getFullPropertyName(PROP_POOL_MAX_IDLE), Integer.valueOf(8));
		defaults.put(getFullPropertyName(PROP_POOL_MAX_ACTIVE), Integer.valueOf(8));
		defaults.put(getFullPropertyName(PROP_DELAY), Integer.valueOf(-1));
		defaults.put(getFullPropertyName(PROP_STATE_ACTIVE), Boolean.TRUE);
		
		return defaults;
	}
	
	@SuppressWarnings("unchecked")
	public void updated(Dictionary configuration) throws ConfigurationException {
		if (configuration == null ) {
			/*
			 * Generate default configuration
			 */
			configuration = this.getDefaults();
		}
		
		/* 
		 * Thread pool configuration
		 */
		Integer minIdle = (Integer)configuration.get(getFullPropertyName(PROP_POOL_MIN_IDLE));
		this.pool.setMinIdle((minIdle == null) ? 0 : minIdle.intValue());
		
		Integer maxIdle = (Integer)configuration.get(getFullPropertyName(PROP_POOL_MAX_IDLE));
		this.pool.setMaxIdle((maxIdle == null) ? 8 : maxIdle.intValue());
		
		Integer maxActive = (Integer)configuration.get(getFullPropertyName(PROP_POOL_MAX_ACTIVE));
		this.pool.setMaxActive((maxActive == null) ? 8 : maxActive.intValue());
		
		/*
		 * Delay between job execution
		 */
		Integer delay = (Integer)configuration.get(getFullPropertyName(PROP_DELAY));
		this.master.setDelay((delay == null) ? -1 : delay.intValue());
		
		/*
		 * Component actiontion status
		 */
		final Boolean active = (Boolean)configuration.get(getFullPropertyName(PROP_STATE_ACTIVE));
		if (active != null) {
			this.stateCode = "OK";
			this.setActiveState(active);
		}
	}
	
	/**
	 * @see MetaTypeProvider#getLocales()
	 */
	public String[] getLocales() {
		return this.locales==null?null:this.locales.clone();
	}
	
	/**
	 * @see MetaTypeProvider#getObjectClassDefinition(String, String)
	 */
	public ObjectClassDefinition getObjectClassDefinition(String id, String localeStr) {
		final ArrayList<AttributeDefinition> ads = new ArrayList<AttributeDefinition>();
		final Locale locale = (localeStr == null) ? Locale.ENGLISH : new Locale(localeStr);
		final ResourceBundle rb = ResourceBundle.getBundle("OSGI-INF/l10n/" + MWComponent.class.getSimpleName(), locale);
		
		ads.add(new AD(
				getFullPropertyName(PROP_POOL_MIN_IDLE),
				rb.getString("threads.idle.min.name"),
				rb.getString("threads.idle.min.desc"),
				new String[] { Integer.toString(0) }));
		ads.add(new AD(
				getFullPropertyName(PROP_POOL_MAX_IDLE),
				rb.getString("threads.idle.max.name"),
				rb.getString("threads.idle.max.desc"),
				new String[] { Integer.toString(8) }));
		ads.add(new AD(
				getFullPropertyName(PROP_POOL_MAX_ACTIVE),
				rb.getString("threads.active.max.name"),
				rb.getString("threads.active.max.desc"),
				new String[] { Integer.toString(8) }));
		ads.add(new AD(
				getFullPropertyName(PROP_DELAY),
				rb.getString("threads.active.delay.name"),
				rb.getString("threads.active.delay.desc"),
				new String[] { Integer.toString(-1) }));
		ads.add(new AttributeDefinition() {
			public int getCardinality() { return 0; }
			public String[] getDefaultValue() { return new String[] { Boolean.TRUE.toString() }; }
			public String getDescription() { return rb.getString("state.active.desc"); }
			public String getID() { return getFullPropertyName(PROP_STATE_ACTIVE); }
			public String getName() { return rb.getString("state.active.name"); }
			public String[] getOptionLabels() { return new String[] { rb.getString("state.active.running"), rb.getString("state.active.paused") }; }
			public String[] getOptionValues() { return new String[] { Boolean.TRUE.toString(), Boolean.FALSE.toString() }; }
			public int getType() { return AttributeDefinition.BOOLEAN; }
			public String validate(String value) { return null; }
		});
		
		final String PID = this.componentID;
		final String mame = this.componentName;
		final String descr = this.componentDescription;
		
		// create metadata
		ObjectClassDefinition ocd = new ObjectClassDefinition() {
			public AttributeDefinition[] getAttributeDefinitions(int filter) {
				return ads.toArray(new AttributeDefinition[ads.size()]);
			}

			public String getDescription() {
				return descr;
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
		public String getID() { return this.ID; }
		public String getName() { return this.name; }
		public int getCardinality() { return 0; }
		public String[] getOptionLabels() { return null; }
		public String[] getOptionValues() { return null; }
		public int getType() { return AttributeDefinition.INTEGER; }
		public String validate(String value) { return null; }				
	}


	/**
	 * @see Monitorable#getStatusVariableNames()
	 */
	public String[] getStatusVariableNames() {
		return VAR_NAMES.toArray(new String[VAR_NAMES.size()]);
	}	
	
	/**
	 * @see Monitorable#getDescription(String)
	 */
	public String getDescription(String name) throws IllegalArgumentException {
		if (!VAR_NAMES.contains(name)) {
			throw new IllegalArgumentException("Invalid Status Variable name " + name);
		}
		
		return this.monitorableRb.getString(name);
	}

	/**
	 * @see Monitorable#getStatusVariable(String)
	 */
	public StatusVariable getStatusVariable(String name) throws IllegalArgumentException {
		if (!VAR_NAMES.contains(name)) {
			throw new IllegalArgumentException("Invalid Status Variable name " + name);
		}
		
		if (name.equals(VAR_NAME_PPM)) {
			return new StatusVariable(name, StatusVariable.CM_GAUGE, this.getPPM());
		} else if (name.equals(VAR_NAME_PAUSED)) {
			return new StatusVariable(name, StatusVariable.CM_GAUGE, this.isPaused());
		} else if (name.equals(VAR_NAME_JOBS_ACTIVE)) {
			return new StatusVariable(name, StatusVariable.CM_GAUGE, this.getActiveJobCount());
		} else if (name.equals(VAR_NAME_JOBS_ENQUEUED)) {
			return new StatusVariable(name, StatusVariable.CM_GAUGE, this.getEnqueuedJobCount());
		} else if (name.equals(VAR_NAME_JOBS_MAX)) {
			return new StatusVariable(name, StatusVariable.CM_GAUGE, this.getPool().getMaxActiveJobCount());
		} else if (name.equals(VAR_NAME_JOBS_TOTAL)) {
			return new StatusVariable(name, StatusVariable.CM_GAUGE, this.getMaster().processedCount());
		} else if (name.equals(VAR_NAME_JOB_DELAY)) {
			return new StatusVariable(name, StatusVariable.CM_GAUGE, this.getMaster().getDelay());
		} else if (name.equals(VAR_NAME_STAT_CODE)) {
			return new StatusVariable(name, StatusVariable.CM_SI, this.stateCode);
		}
		return null;
	}

	/**
	 * @see Monitorable#notifiesOnChange(String)
	 */
	public boolean notifiesOnChange(String name) throws IllegalArgumentException {
		return false;
	}

	/**
	 * @see Monitorable#resetStatusVariable(String)
	 */
	public boolean resetStatusVariable(String name) throws IllegalArgumentException {
		return false;
	}

	/**
	 * @see EventHandler#handleEvent(Event)
	 */
	public void handleEvent(Event event) {
		final String topic = event.getTopic();
		if (topic.equals("org/paxle/monitorable/observer")) {
			final Boolean active = (Boolean)event.getProperty(this.getFullPropertyName(PROP_STATE_ACTIVE));
			final String stateCode = (String)event.getProperty(this.getFullPropertyName(PROP_STATE_CODE));
			
			if (stateCode != null) {
				this.stateCode = stateCode;
			}
			
			if (active != null) {
				this.setActiveState(active);
			}
			
			Integer deltaDelay = (Integer) event.getProperty(this.getFullPropertyName(PROP_DELAY_DELTA));
			if (deltaDelay != null) {
				int delay = this.getMaster().getDelay();
				if (delay < 0) delay = 0;
				delay += deltaDelay.intValue();
				this.master.setDelay(delay);
			}
		}
	}
	
}
