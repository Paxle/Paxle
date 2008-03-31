package org.paxle.core.queue;

import java.net.URI;
import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.service.event.Event;
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
	 * This topic should only be used by the {@link IDataProvider} that 
	 * initially produced the new {@link ICommand} but not by pipes
	 * that just forward already created commands. 
	 */
	public static final String TOPIC_CREATED = TOPIC_ + "CREATED";
	
	/**
	 * The {@link ICommand} was enqueued into a {@link IInputQueue}
	 */
	public static final String TOPIC_ENQUEUED = TOPIC_ + "ENQUEUED";
	
	/**
	 * The {@link ICommand} was dequeued from a {@link IOutputQueue}
	 */	
	public static final String TOPIC_DEQUEUED = TOPIC_ + "DEQUEUED";
	
	/**
	 * The {@link ICommand} was consumed by a {@link IDataConsumer}.
	 * This topic should only be used by the {@link IDataConsumer} that 
	 * finally consumed a {@link ICommand} but not by pipes that just 
	 * forward commands.
	 */	
	public static final String TOPIC_DESTROYED = TOPIC_ + "DESTROYED";
	
	/* ======================================================================
	 * Event Properties
	 * ====================================================================== */
	/**
	 * The {@link ICommand#getOID() command-ID}
	 * This value must be of type {@link Long}.
	 */
	public static final String PROP_COMMAND_ID = "commandID";
	
	/**
	 * The {@link ICommand#getLocation() command-location}. 
	 * This value must be of type {@link URI}.
	 */
	public static final String PROP_COMMAND_LOCATION = "commandLocation";
	
	/**
	 * The {@link ICommand#getResult() command-status}.
	 * This value must be of type {@link String}.
	 */
	public static final String PROP_COMMAND_RESULT = "commandResult";
	
	/**
	 * The {@link ICommand#getResult() command-status}.
	 * This value must be of type {@link String}.
	 */
	public static final String PROP_COMMAND_RESULT_TEXT = "commandResultText";
	
	/**
	 * One of the following IDs:
	 * TODO
	 * 
	 * This value must be of type {@link String}.
	 */
	public static final String PROP_STAGE_ID = "stageID";
	
	@SuppressWarnings("unchecked")
	private static void extractCommandProps(String stageID, ICommand command, Dictionary properties) {
		properties.put(PROP_COMMAND_ID, Long.valueOf(command.getOID()));
		properties.put(PROP_COMMAND_LOCATION, command.getLocation().toASCIIString());
		
		ICommand.Result result = command.getResult();
		properties.put(PROP_COMMAND_RESULT, result==null?ICommand.Result.Passed.name():result.name());
		
		String resultText = command.getResultText();
		properties.put(PROP_COMMAND_RESULT_TEXT, resultText==null?"":resultText);		
	}
	
	public static Event createEvent(String stageID, String topic, ICommand command) {
		return createEvent(stageID, topic, command,new Hashtable<String, Object>());
	}
	
	@SuppressWarnings("unchecked")
	public static Event createEvent(String stageID, String topic, ICommand command, Dictionary properties) {
		if (topic == null) throw new NullPointerException("No topic specified.");
		if (command == null) throw new NullPointerException("No command specified.");
		if (properties == null) properties = new Hashtable<String, Object>();

		// adding the component-id
		properties.put(PROP_STAGE_ID, stageID);
		
		// adding timestamp
		properties.put(EventConstants.TIMESTAMP, Long.valueOf(System.currentTimeMillis()));
		
		// append command properties
		extractCommandProps(stageID, command, properties);
		
		// return an event object
		return new Event(topic, properties);
	}
}
