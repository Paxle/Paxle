package org.paxle.core.queue.impl;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.paxle.core.filter.CommandFilterEvent;
import org.paxle.core.queue.CommandEvent;
import org.paxle.core.queue.ICommand;
import org.paxle.core.queue.ICommandTracker;

public class CommandTracker extends Thread implements ICommandTracker, EventHandler {
	/**
	 * Maximum time in ms an already destroyed {@link ICommand} is kept in the
	 * {@link #destroyedCommandMap}.
	 */
	private static final Long MAX_HOLDBACK_TIME = Long.valueOf(1*60*1000);

	/**
	 * Max delay between cleanup thread loops
	 * @see #run()
	 */
	private static final Long CLEANUP_DELAY = Long.valueOf(1*60*1000);

	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * A special logger that will be redirected via logging configuration into
	 * an extra file
	 */
	private Log cmdEventLogger = LogFactory.getLog(CommandEvent.class.getName());

	/**
	 * The OSGi event-admin service. We use it to send events via
	 * {@link #commandCreated(String, ICommand)} and {@link #commandDestroyed(String, ICommand)}.
	 */
	private final EventAdmin eventService;

	/**
	 * A queue containing  {@link WeakReference weak-references} to 
	 * {@link ICommand commands} that are not referenced from anywhere in 
	 * the runtime. 
	 * 
	 * {@link ICommand Commands} that are receive via this queue were not 
	 * released properly via a call of {@link #commandDestroyed(String, ICommand)}.
	 */
	private final ReferenceQueue<ICommand> refQueue;

	/**
	 * A command-lookup table. Keys are the {@Link ICommand#getOID() command-IDs},
	 * values are {@link WeakReference weak-references} to all {@link ICommand} that
	 * are known to the {@link ICommandTracker command-tracker}.
	 * 
	 * We are using {@link WeakReference}s here to avoid memory leaks if components forget
	 * to call {@link #commandDestroyed(String, ICommand)} after the processing of a
	 * {@link ICommand} has finished.
	 */
	private final Hashtable<Long, WeakReference<ICommand>> commandIDTable;

	/**
	 * A command-lookup table. Keys are the {@Link ICommand#getLocation() command-locations},
	 * values are {@link WeakReference weak-references} to all {@link ICommand} that
	 * are known to the {@link ICommandTracker command-tracker}.
	 * 
	 * We are using {@link WeakReference}s here to avoid memory leaks if components forget
	 * to call {@link #commandDestroyed(String, ICommand)} after the processing of a
	 * {@link ICommand} has finished.
	 */
	private final Hashtable<URI, WeakReference<ICommand>> commandLocationTable;

	/**
	 * This list is used to ensure that a reference is kept to a {@link ICommand} to avoid
	 * garbage collection.
	 * 
	 * A list containing {@link ICommand commands} with timestamps. The list is sorted
	 * sorted by the timestamps. Commands that are older than {@link #MAX_HOLDBACK_TIME} 
	 * were removed from this list allowing the gc to free the object.
	 */
	private final LinkedList<DestroyedCommandData> destroyedCommandMap;

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();	

	/**
	 * @param eventService the OSGi event-admin service. This is required to send out events 
	 * when calling {@link #commandCreated(String, ICommand)} and {@link #commandDestroyed(String, ICommand)}
	 */
	public CommandTracker(EventAdmin eventService) {
		if (eventService == null) throw new NullPointerException("The event-service is null.");

		this.eventService = eventService;
		this.refQueue = new ReferenceQueue<ICommand>();

		this.commandIDTable = new Hashtable<Long, WeakReference<ICommand>>();
		this.commandLocationTable = new Hashtable<URI, WeakReference<ICommand>>();
		this.destroyedCommandMap = new LinkedList<DestroyedCommandData>();

		this.setName("CommandTracker");
		this.start();
	}

	/**
	 * @see ICommandTracker#commandCreated(String, ICommand)
	 */
	public void commandCreated(String componentID, ICommand command) {
		if (componentID == null) throw new NullPointerException("The component-id is null.");
		if (command == null) throw new NullPointerException("The command is null.");

		// add command into datastructures
		Long commandID = Long.valueOf(command.getOID());
		WeakReference<ICommand> commandRef = new WeakReference<ICommand>(command, this.refQueue);
		if (commandID.intValue() <= 0) {
			// store the command based on the alreacy known location into the mapping-table so that
			// the ORM can fetch it via #getCommandByLocation(URI commandLocation)
			try {
				w.lock();
				this.commandLocationTable.put(command.getLocation(), commandRef);
			} finally {
				w.unlock();
			}
			
			// fire a synchronous event to get the ORM-mapping-tool a chance to set the OID properly
			this.eventService.sendEvent(CommandEvent.createEvent(ICommandTracker.class.getName(), CommandEvent.TOPIC_OID_REQUIRED, command));
			
			// now the command should have a valid OID
			commandID = Long.valueOf(command.getOID());
			if (commandID.intValue() <= 0) {
				this.logger.warn(String.format("The command-ID invalid: '%d'. Maybe a problem in the ORM-mapping?",commandID));
			}
		}
		
		/*
		 * Regularely insert the command into the
		 * - OID to command
		 * - Location to command
		 * mapping tables
		 */
		try {
			w.lock();
			this.commandIDTable.put(commandID, commandRef);
			this.commandLocationTable.put(command.getLocation(), commandRef);
		} finally {
			w.unlock();
		}

		// send out a CommandEvent.TOPIC_CREATED event
		this.eventService.sendEvent(CommandEvent.createEvent(componentID, CommandEvent.TOPIC_CREATED, command));		
	}

	/**
	 * @see ICommandTracker#commandDestroyed(String, ICommand)
	 */
	public void commandDestroyed(String componentID, ICommand command) {
		if (componentID == null) throw new NullPointerException("The component-id is null.");
		if (command == null) throw new NullPointerException("The command is null.");

		// send out a CommandEvent.TOPIC_DESTROYED event (this _must_ be send synchronous)
		this.eventService.sendEvent(CommandEvent.createEvent(componentID, CommandEvent.TOPIC_DESTROYED, command));
	}	

	/**
	 * A new {@link CommandEvent} was received.
	 * @see EventHandler#handleEvent(Event)
	 */
	public void handleEvent(Event event) {
		// command and profile IDs
		Long commandID = (Long) event.getProperty(CommandEvent.PROP_COMMAND_ID);
		Long commandProfileID = (Long) event.getProperty(CommandEvent.PROP_PROFILE_ID);
		
		// Topic info
		String fqTopic = (String)event.getProperty(EventConstants.EVENT_TOPIC);
		String topic = fqTopic.substring(fqTopic.lastIndexOf('/')+1);

		// component/filter info
		String component = (String) event.getProperty(CommandEvent.PROP_COMPONENT_ID);
		String filter = (String) event.getProperty(CommandFilterEvent.PROP_FILTER_NAME);
		String stageInfo = component;
		if (filter != null) stageInfo += ", " + filter;
		
		/* Command-Event-Logging, e.g.
		 * 2008-06-30 06:47:22 - P:131072 C:0163840 PRE_FILTER   (org.paxle.crawler.in, org.paxle.data.db.impl.CommandProfileFilter): http://mg4j.dsi.unimi.it/ 
		 */
		this.cmdEventLogger.debug(String.format("P:%05d C:%07d %-12s (%s): %s",
				commandProfileID,
				commandID,
				topic,
				stageInfo,
				event.getProperty(CommandEvent.PROP_COMMAND_LOCATION)
		));
		
		
		if (fqTopic.equalsIgnoreCase(CommandEvent.TOPIC_DESTROYED)) {
			ICommand command = this.getCommandByID(commandID);
			if (command != null) {
				/* 
				 * Move the command into the destroyed command-map to avoid gc.
				 *  
				 * Components listening to the CommandEvent.TOPIC_DESTROYED can access 
				 * it via this map within a given time-span.
				 */
				synchronized (this.destroyedCommandMap) {
					this.destroyedCommandMap.add(new DestroyedCommandData(Long.valueOf(System.currentTimeMillis()), command));
				}
			}
		}
	}

	/**
	 * Terminates the {@link #run() cleanup-thread}
	 * @throws InterruptedException
	 */
	public void terminate() throws InterruptedException {
		this.interrupt();
		this.join(2000);
	}

	/**
	 * @see Thread#run()
	 */
	@Override
	public void run() {
		try {
			while(!this.isInterrupted()) {
				try {
					/* Check for unreferenced ICommands.
					 * 
					 * Command that are accessible via this queue are not referenced 
					 * anywhere in the runtime.
					 * 
					 * This should only occur if a component has forgotten to call
					 * ICommandTracker.commandDestroyed(...)
					 */
					Reference<? extends ICommand> commandRef = this.refQueue.remove(CLEANUP_DELAY.longValue());
					if (commandRef != null) {
						this.logger.error("Command was destroyed without calling ICommandTracker.commandDestroyed(...)");

						ICommand command = commandRef.get();
						if (command != null) {
							// we should never get in here ...
							this.logger.error("Unexpected stat. commandRef.get() returned not null");
						} else {
							Long commandID = null;
							URI commandURI = null;
							try {
								w.lock();
								Iterator<Map.Entry<Long, WeakReference<ICommand>>> commandIDIter = this.commandIDTable.entrySet().iterator();
								while (commandIDIter.hasNext()) {
									Map.Entry<Long, WeakReference<ICommand>> entry = commandIDIter.next();
									if (entry.getValue().equals(commandRef)) {
										commandID = entry.getKey();
										commandIDIter.remove();
										break;
									}
								}

								Iterator<Map.Entry<URI, WeakReference<ICommand>>> commandURIIter = this.commandLocationTable.entrySet().iterator();
								while (commandIDIter.hasNext()) {
									Map.Entry<URI, WeakReference<ICommand>> entry = commandURIIter.next();
									if (entry.getValue().equals(commandRef)) {
										commandURI = entry.getKey();
										commandURIIter.remove();
										break;
									}
								}
							} finally {
								w.unlock();
							}

							this.logger.warn(String.format("Command [%06d] removed without calling destroy: %s",
									commandID,
									(commandURI==null)?"unknown":commandURI.toASCIIString()
							));
						}
					}

					/* Check for to old destroyed ICommands.
					 * 
					 */
					if (!this.destroyedCommandMap.isEmpty()) {
						long maxage = System.currentTimeMillis() - MAX_HOLDBACK_TIME.longValue();

						synchronized (this.destroyedCommandMap) {
							
							while (!this.destroyedCommandMap.isEmpty() && this.destroyedCommandMap.getFirst().destroyedTime.longValue() < maxage) {
								// remove the next command
								DestroyedCommandData destoryedCommand = this.destroyedCommandMap.removeFirst();

								ICommand command = destoryedCommand.command;
								Long commandID = Long.valueOf(command.getOID());
								URI commandURI = command.getLocation();

								// removing the outdated command from all lists
								WeakReference<ICommand> cmdRef = null;
								this.commandLocationTable.remove(commandURI);
								cmdRef = this.commandIDTable.remove(commandID);
								cmdRef.clear();

								this.logger.debug(String.format("Command [%06d] removed from destroyed map: %s",
										commandID,
										(commandURI==null)?"unknown":commandURI.toASCIIString()
								));							
							}
						}
					}
				} catch (Throwable e) {
					if (e instanceof InterruptedException) throw (InterruptedException)e;
					this.logger.error(String.format(
							"Unexpected '%s' while cleaning up destroyed commands.",
							e.getClass().getName()
					),e);
				}
			}
		} catch (InterruptedException e) {
			this.logger.info("Thread was interrupted");
		}
	}

	/**
	 * @see ICommandTracker#getCommandByID(Long)
	 */
	public ICommand getCommandByID(Long commandID) {		
		try {
			if (commandID == null) throw new NullPointerException("The command-ID is null.");
			else if (commandID.longValue() < 0) throw new IllegalArgumentException("The command-ID must be equal or greater than 0.");

			// getting the command
			r.lock();
			WeakReference<ICommand> commandRef = this.commandIDTable.get(commandID);
			if (commandRef == null) return null;

			ICommand command = commandRef.get();
			return command;
		} finally {
			r.unlock();
		}
	}

	/**
	 * @see ICommandTracker#getCommandByLocation(URI)
	 */
	public ICommand getCommandByLocation(URI commandLocation) {
		try {
			if (commandLocation == null) throw new NullPointerException("The command-location is null.");

			// getting the command
			r.lock();
			WeakReference<ICommand> commandRef = this.commandLocationTable.get(commandLocation);
			if (commandRef == null) return null;

			ICommand command = commandRef.get();
			return command;
		} finally {
			r.unlock();
		}
	}

	/**
	 * @see ICommandTracker#getTrackingSize()
	 */
	public long getTrackingSize() {
		return this.commandIDTable.size();
	}
	
	/**
	 * This function is just used for junit testing.
	 * Do not use it for other purposes.
	 */
	boolean isInDestroyedList(ICommand cmd) {
		if (this.destroyedCommandMap != null) {
			for (DestroyedCommandData data : this.destroyedCommandMap) {
				if (data.command == cmd) return true;
			}
		}
		return false;
	}
}

/**
 * A class to hold information about already {@link CommandTracker#commandDestroyed(String, ICommand) destoryed}
 * commands that should be kept accessible, otherwise the garbage collection would clean them up.
 */
class DestroyedCommandData {
	public ICommand command;
	public Long destroyedTime;

	DestroyedCommandData(Long destroyedTime, ICommand command) {
		this.command = command;
		this.destroyedTime = destroyedTime;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();

		buf.append(new Date(this.destroyedTime.longValue()))
		.append(": ")
		.append(this.command.getLocation());

		return buf.toString();
	}
}
