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

package org.paxle.core.doc;

import java.util.Dictionary;
import java.util.Hashtable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.paxle.core.data.IDataConsumer;
import org.paxle.core.data.IDataProvider;
import org.paxle.core.queue.IInputQueue;
import org.paxle.core.queue.IOutputQueue;

public class CommandEvent {
	/* ======================================================================
	 * Event Topics
	 * ====================================================================== */
	public static final String TOPIC_ = "org/paxle/core/doc/CommandEvent/";
	
	public static final String TOPIC_ALL = TOPIC_ + "*";
	
	/**
	 * A very special topic that is used by the {@link ICommandTracker} if a
	 * command is passed to {@link ICommandTracker#commandCreated(String, ICommand)}
	 * that does not have a valid {@link ICommand#getOID() OID}.
	 * 
	 * Events with this topic should be catched by the ORM layer to allow
	 * to persist the newly created {@link ICommand command}
	 */
	public static final String TOPIC_OID_REQUIRED = TOPIC_ + "OID_REQUIRED";
	
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
	 * The {@link ICommandProfile#getOID() profile-ID} fot he
	 * {@link ICommandProfile profile} the command belongs to.
	 * <p/>
	 * This value must be of type {@link Long}.
	 */
	public static final String PROP_PROFILE_ID = "profileID";
	
	/**
	 * The {@link ICommand#getLocation() command-location}.
	 * <p/> 
	 * This value must be of type {@link String}.
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
	public static final String PROP_COMPONENT_ID = "componentID";
	
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
		properties.put(PROP_PROFILE_ID, Long.valueOf(command.getProfileOID()));
		properties.put(PROP_COMMAND_LOCATION, command.getLocation().toString());
		
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
	public static Event createEvent(@Nonnull String componentID, @Nonnull String topic, @Nonnull ICommand command) {
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
	public static Event createEvent(@Nonnull String componentID, @Nonnull String topic, @Nonnull ICommand command, @Nullable Dictionary properties) {
		if (componentID == null) throw new NullPointerException("The component-ID is null");
		if (topic == null) throw new NullPointerException("No topic specified.");
		if (command == null) throw new NullPointerException("No command specified.");
		if (properties == null) properties = new Hashtable<String, Object>();

		// adding the component-id
		properties.put(PROP_COMPONENT_ID, componentID);
		
		// adding timestamp
		properties.put(EventConstants.TIMESTAMP, Long.valueOf(System.currentTimeMillis()));
		
		// append command properties
		extractCommandProps(componentID, command, properties);
		
		// return an event object
		return new Event(topic, properties);
	}
	
	/**
	 * Direct instantiation of this class not allowed, use function <code>createEvent</code> instead.
	 * 
	 * @see #createEvent(String, String, ICommand)
	 * @see #createEvent(String, String, ICommand, Dictionary)
	 */
	protected CommandEvent() {
	}
}
