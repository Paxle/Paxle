package org.paxle.core.queue;

import java.net.URI;

import org.osgi.service.event.EventAdmin;

public interface ICommandTracker {
	/**
	 * Notifies the {@link ICommandTracker command-tracker} about the creation of a new
	 * {@link ICommand command}. Calling this function also triggers posting an 
	 * {@link CommandEvent#TOPIC_CREATED command-creation} {@link org.osgi.service.event.Event}
	 * via {@link EventAdmin#postEvent(org.osgi.service.event.Event)}.
	 *  
	 * @param componendID unique ID of the component that created the {@link ICommand command}
	 * @param command
	 */
	public void commandCreated(String componendID, ICommand command);
	
	/**
	 * Notifies the {@link ICommandTracker command-tracker} about the destruction of a 
	 * {@link ICommand command}. Calling this function also triggers posting an 
	 * {@link CommandEvent#TOPIC_DESTROYED command-destruction} {@link org.osgi.service.event.Event}
	 * via {@link EventAdmin#postEvent(org.osgi.service.event.Event)}.
	 *  
	 * @param componendID unique ID of the component that consumed the {@link ICommand command}
	 * @param command
	 */
	public void commandDestroyed(String componendID, ICommand command);
	
	/**
	 * Lookup a {@link ICommand} by {@link ICommand#getLocation() location}.
	 * @param commandLocation the {@link ICommand#getLocation() command-location}
	 * @return the {@link ICommand} or <code>null</code> if unknown
	 */
	public ICommand getCommandByLocation(URI commandLocation);
	
	/**
	 * Lookup a {@link ICommand} by {@link ICommand#getOID() ID}.
	 * @param commandID the  {@link ICommand#getOID() command-ID}
	 * @return the {@link ICommand} or <code>null</code> if unknown
	 */
	public ICommand getCommandByID(Long commandID);
	
	/**
	 * @return the amount of {@link ICommand commands} tracked by this component
	 */
	public long getTrackingSize();
}
