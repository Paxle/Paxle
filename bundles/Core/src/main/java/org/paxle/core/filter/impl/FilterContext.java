package org.paxle.core.filter.impl;

import java.net.URI;
import java.util.Properties;

import org.osgi.framework.Constants;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.filter.IFilterManager;
import org.paxle.core.filter.IFilterQueue;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.core.norm.IReferenceNormalizer;
import org.paxle.core.queue.ICommand;


public class FilterContext implements Comparable<FilterContext>, IFilterContext {
	private ITempFileManager tempFileManager = null;
	
	private IReferenceNormalizer referenceNormalizer;
	
	/**
	 * OSGi {@link Constants#SERVICE_ID} of a filter. This is required by the
	 * {@link IFilterManager} to remove a previously installed filter.
	 * 
	 * @see Constants#SERVICE_ID
	 */
	private Long servicID = null;
	
	/**
	 * Properties that were specified during the registration of the filter via the
	 * {@link IFilter#PROP_FILTER_TARGET} parameter, e.g.:<br/>
	 * <pre>
	 * Hashtable<String, String[]> filterProps = new Hashtable<String, String[]>();
	 * filterProps.put(IFilter.PROP_FILTER_TARGET, new String[]{"org.paxle.parser.out; myParam1=x; myParam2=y"});
	 * bc.registerService(IFilter.class.getName(), new MyFilter(), filterProps);	
	 * </pre>
	 */
	private Properties props = null;
	
	/**
	 * The registered filter itself.
	 */
	private IFilter<ICommand> filterImpl = null;
	
	/**
	 * The {@link IFilterQueue target} for which the {@link IFilter} was registered. 
	 * This parameter can be used by a filter to determine it's context, if a single
	 * {@link IFilter} was appended to multiple {@link IFilterQueue queues}.
	 */
	private String targetID = null;
	
	/**
	 * The position of the filter within the filter-list of a {@link IFilterQueue}.
	 * @see IFilter#PROP_FILTER_TARGET_POSITION
	 */
	private int pos = 0;
	
	
	public FilterContext(Long serviceID, IFilter<ICommand> filterImpl, String targetID, int filterPos, Properties props) {
		if (serviceID == null) throw new NullPointerException("The serviceID must not be null");
		if (filterImpl == null) throw new NullPointerException("Filter class is null");
		if (targetID == null || targetID.length() == 0) throw new IllegalArgumentException("Filter targetID is not set");
		
		this.servicID = serviceID;
		this.filterImpl = filterImpl;
		this.targetID = targetID;
		this.pos = filterPos;
		this.props = (props == null) ? new Properties() : props;
	}
	
	/**
	 * @return the OSGi {@link Constants#SERVICE_ID} of the filter. This property is required by the
	 * {@link IFilterManager} to unregister a {@link IFilter}.
	 */
	Long getServiceID() {
		return this.servicID;
	}
	
	/**
	 * A reference to the registered {@link IFilter}
	 */
	public IFilter<ICommand> getFilter() {
		return this.filterImpl;
	}
	
	/**
	 * The ID of the {@link IFilterQueue} for which the {@link IFilter} was registered
	 */
	public String getTargetID() {
		return this.targetID;
	}
	
	/**
	 * The position of the {@link IFilter} within the filter-list of the {@link IFilterQueue target}.
	 */
	public int getFilterPosition(){
		return this.pos;
	}

	/**
	 * Context-specific parameters defined during the filter-registration
	 * 
	 * @see IFilter#PROP_FILTER_TARGET
	 */
	public Properties getFilterProperties() {
		return (this.props==null)?new Properties():this.props;
	}
	
	void setTempFileManager(ITempFileManager tempFileManager) {
		this.tempFileManager = tempFileManager;
	}

	/**
	 * @return the temp-file-manager that should be used by the filter
	 * to handle temp-files.
	 */
	public ITempFileManager getTempFileManager() {
		return this.tempFileManager;
	}
	
	void setReferenceNormalizer(IReferenceNormalizer referenceNormalizer) {
		this.referenceNormalizer = referenceNormalizer;
	}
	
	/**
	 * @return a component to normalize {@link URI URIs}.
	 * @see IReferenceNormalizer#normalizeReference(String)
	 */
	public IReferenceNormalizer getReferenceNormalizer() {
		return this.referenceNormalizer;
	}
	
	/**
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(FilterContext o) {
		if (this == o) return 0;
		
		// order based on position 
		int comp = Integer.valueOf(this.pos).compareTo(Integer.valueOf(o.pos));
		if (comp != 0) return comp;
		
		// order based on filter-impl class-name
		comp = this.filterImpl.getClass().getName().compareTo(o.filterImpl.getClass().getName());
		if (comp != 0) return comp;
		
		// filter based on properties
		comp = Integer.valueOf(this.props.size()).compareTo(o.props.size());
		if (comp != 0) return comp;
		
		// TODO: is this enough or should we even compare the property values?
		
		return 0;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append(this.filterImpl.getClass().getName())
			   .append(" [SID=")
			   .append((this.servicID==null)?"":this.servicID.toString())
			   .append("]: ")
			   .append("target=")
			   .append(this.targetID)
			   .append(" [").append(this.pos).append("] ")
			   .append(this.props.toString());
		
		return builder.toString();
	}
}
