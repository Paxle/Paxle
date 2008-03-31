package org.paxle.core.filter;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.paxle.core.queue.CommandEvent;
import org.paxle.core.queue.ICommand;

public class CommandFilterEvent extends CommandEvent {
	/* ======================================================================
	 * Event Topics
	 * ====================================================================== */
	/**
	 * An event that is triggered before the command is passed to a {@link IFilter}
	 */	
	public static final String TOPIC_PRE_FILTER = TOPIC_ + "PRE_FILTER";
	
	/**
	 * An event that is triggered after the command was passed to a {@link IFilter}
	 */	
	public static final String TOPIC_POST_FILTER = TOPIC_ + "POST_FILTER";
	
	/* ======================================================================
	 * Event Properties
	 * ====================================================================== */
	/**
	 * @see org.paxle.core.filter.IFilter#PROP_FILTER_TARGET
	 */
	public static final String PROP_FILTER_TARGET = "filterTarget";	
	
	/**
	 * @see org.paxle.core.filter.IFilter#PROP_FILTER_TARGET_POSITION
	 */
	public static final String PROP_FILTER_TARGET_POSITION = "filterPos";
	
	/**
	 * The name of the class-filter
	 */
	public static final String PROP_FILTER_NAME = "filterClassName";
	
	
	@SuppressWarnings("unchecked")
	private static void extractFilterContextProps(IFilterContext context, Dictionary properties) {
		properties.put(PROP_FILTER_TARGET, context.getTargetID());
		properties.put(PROP_FILTER_TARGET_POSITION, Integer.valueOf(context.getFilterPosition()));
		properties.put(PROP_FILTER_NAME, context.getFilter().getClass().getName());
	}
	
	public static Event createEvent(String stageID, String topic, ICommand command, IFilterContext context) {
		return createEvent(stageID, topic, command, context, null);
	}
	
	public static Event createEvent(String stageID, String topic, ICommand command, IFilterContext context, Exception exception) {
		Hashtable<String, Object> props = new Hashtable<String, Object>();
		
		// extracting filter-context-props
		extractFilterContextProps(context, props);
		
		// add exception info (if any)
		if (exception != null) {
			props.put(EventConstants.EXCEPTION, exception);
			props.put(EventConstants.EXCEPTION_MESSAGE, exception.getMessage());
			props.put(EventConstants.EXECPTION_CLASS, exception.getClass().getName());
		}
		
		// create general and append general command-props 
		Event event = CommandEvent.createEvent(stageID, topic, command, props);
		return event;
	}
}
