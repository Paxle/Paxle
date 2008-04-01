package org.paxle.core.queue;

import java.net.URI;
import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.paxle.core.data.IDataConsumer;
import org.paxle.core.data.IDataProvider;

public class CommandEvent {
	/* ======================================================================
	 * Event Topics
	 * ====================================================================== */
	public static final String TOPIC_ = CommandEvent.class.getName().replace('.', '/') + "/";
	
	public static final String TOPIC_ALL = TOPIC_ + "*";
	
	/**
	 * The {@link ICommand} was produced by a {@link IDataProvider}.
	 * <p/>
	 * This topic should only be used by the {@link IDataProvider} that 
	 * initially produced the new {@link ICommand} but not by pipes
	 * that just forward already created commands. 
	 * <p/>
	 * {@link Event Events} with this topic must be send 
	 * {@link EventAdmin#sendEvent(Event) synchronous} 
	 */
	public static final String TOPIC_CREATED = TOPIC_ + "CREATED";
	
	/**
	 * The {@link ICommand} was enqueued into a {@link IInputQueue}
	 * <p/>
	 * {@link Event Events} with this topic must be send 
	 * {@link EventAdmin#postEvent(Event) asynchronous} to avoid blocking.
	 */
	public static final String TOPIC_ENQUEUED = TOPIC_ + "ENQUEUED";
	
	/**
	 * The {@link ICommand} was dequeued from a {@link IOutputQueue}
	 * <p/>
	 * {@link Event Events} with this topic must be send 
	 * {@link EventAdmin#postEvent(Event) asynchronous} to avoid blocking.
	 */	
	public static final String TOPIC_DEQUEUED = TOPIC_ + "DEQUEUED";
	
	/**
	 * The {@link ICommand} was consumed by a {@link IDataConsumer}.
	 * <p/>
	 * This topic should only be used by the {@link IDataConsumer} that 
	 * finally consumed a {@link ICommand} but not by pipes that just 
	 * forward commands.
	 * <p/>
	 * {@link Event Events} with this topic must be send 
	 * {@link EventAdmin#sendEvent(Event) synchronous} and the sender of the 
	 * event must keep a reference to the {@link ICommand}
	 * until the {@link Event event} was sent. Otherwise the garbage collection
	 * would cleanup the command before other components can access it.
	 */	
	public static final String TOPIC_DESTROYED = TOPIC_ + "DESTROYED";
	
	/* ======================================================================
	 * Event Properties
	 * ====================================================================== */
	/**
	 * The {@link ICommand#getOID() command-ID}
	 * <p/>
	 * This value must be of type {@link Long}.
	 */
	public static final String PROP_COMMAND_ID = "commandID";
	
	/**
	 * The {@link ICommand#getLocation() command-location}.
	 * <p/> 
	 * This value must be of type {@link URI}.
	 */
	public static final String PROP_COMMAND_LOCATION = "commandLocation";
	
	/**
	 * The {@link ICommand#getResult() command-status}.
	 * <p/>
	 * This value must be of type {@link String}.
	 */
	public static final String PROP_COMMAND_RESULT = "commandResult";
	
	/**
	 * The {@link ICommand#getResult() command-status}.
	 * <p/>
	 * This value must be of type {@link String}.
	 */
	public static final String PROP_COMMAND_RESULT_TEXT = "commandResultText";
	
	/**
	 * One of the following IDs:
	 * <table border="1">
	 * <tr><td>{@link org.paxle.core.filter.IFilterQueue#setFilterQueueID(String) FilterQueue-ID}</td>
	 *     <td>if the event-sender is a {@link org.paxle.core.filter.IFilterQueue}</td></tr>
	 * <tr><td>{@link Class#getName() class-name}</td>
	 *     <td>if the event-sender is a {@link org.paxle.core.data.IDataProvider} or {@link org.paxle.core.data.IDataConsumer},
	 *     	   without a unique systemwidth ID
	 *     </td>
	 * </tr>
	 * </table>
	 * <p/>
	 * This value must be of type {@link String}.
	 */
	public static final String PROP_STAGE_ID = "stageID";
	
	/**
	 * Function to extract the {@link #PROP_COMMAND_ID command-ID}, {@link #PROP_COMMAND_LOCATION command-location},
	 * {@link #PROP_COMMAND_RESULT command-status} and the {@link #PROP_COMMAND_RESULT_TEXT command-status-text}
	 * from a given {@link ICommand}
	 * 
	 * @param componentID unique ID of the component that will send the event
	 * @param command the command for which the event will be send
	 * @param properties {@link Event#getPropertyNames() event-properties}
	 */
	@SuppressWarnings("unchecked")
	private static void extractCommandProps(String componentID, ICommand command, Dictionary properties) {
		properties.put(PROP_COMMAND_ID, Long.valueOf(command.getOID()));
		properties.put(PROP_COMMAND_LOCATION, command.getLocation().toASCIIString());
		
		ICommand.Result result = command.getResult();
		properties.put(PROP_COMMAND_RESULT, result==null?ICommand.Result.Passed.name():result.name());
		
		String resultText = command.getResultText();
		properties.put(PROP_COMMAND_RESULT_TEXT, resultText==null?"":resultText);		
	}
	
	/**
	 * Function to generate an new {@link Event}.
	 * @param componentID unique ID of the component which will send the event
	 * @param topic the topic that should be used, e.g. {@link #TOPIC_CREATED}
	 * @param command the command for which the event should be send
	 * 
	 * @return an {@link Event} with the given {@link Event#getTopic() topic} and
	 * {@link Event#getPropertyNames() properties}.
	 */
	public static Event createEvent(String componentID, String topic, ICommand command) {
		return createEvent(componentID, topic, command,new Hashtable<String, Object>());
	}
	
	/**
	 * Function to generate an new {@link Event}.
	 * 
	 * @param componentID unique ID of the component which will send the event
	 * @param topic the topic that should be used, e.g. {@link #TOPIC_CREATED}
	 * @param command the command for which the event should be send
	 * @param properties some pre-defined properties
	 * 
	 * @return an {@link Event} with the given {@link Event#getTopic() topic} and
	 * {@link Event#getPropertyNames() properties}.
	 */
	@SuppressWarnings("unchecked")
	public static Event createEvent(String componentID, String topic, ICommand command, Dictionary properties) {
		if (componentID == null) throw new NullPointerException("The component-ID is null");
		if (topic == null) throw new NullPointerException("No topic specified.");
		if (command == null) throw new NullPointerException("No command specified.");
		if (properties == null) properties = new Hashtable<String, Object>();

		// adding the component-id
		properties.put(PROP_STAGE_ID, componentID);
		
		// adding timestamp
		properties.put(EventConstants.TIMESTAMP, Long.valueOf(System.currentTimeMillis()));
		
		// append command properties
		extractCommandProps(componentID, command, properties);
		
		// return an event object
		return new Event(topic, properties);
	}
}
