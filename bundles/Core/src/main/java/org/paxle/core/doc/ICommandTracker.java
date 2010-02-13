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

import java.net.URI;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
	public void commandCreated(@Nonnull String componendID, @Nonnull ICommand command);
	
	/**
	 * Notifies the {@link ICommandTracker command-tracker} about the destruction of a 
	 * {@link ICommand command}. Calling this function also triggers posting an 
	 * {@link CommandEvent#TOPIC_DESTROYED command-destruction} {@link org.osgi.service.event.Event}
	 * via {@link EventAdmin#postEvent(org.osgi.service.event.Event)}.
	 *  
	 * @param componendID unique ID of the component that consumed the {@link ICommand command}
	 * @param command
	 */
	public void commandDestroyed(@Nonnull String componendID, @Nonnull ICommand command);
	
	/**
	 * Lookup a {@link ICommand} by {@link ICommand#getLocation() location}.
	 * @param commandLocation the {@link ICommand#getLocation() command-location}
	 * @return the {@link ICommand} or <code>null</code> if unknown
	 */
	public @Nullable ICommand getCommandByLocation(@Nonnull URI commandLocation);
	
	/**
	 * Lookup a {@link ICommand} by {@link ICommand#getOID() ID}.
	 * @param commandID the  {@link ICommand#getOID() command-ID}
	 * @return the {@link ICommand} or <code>null</code> if unknown
	 */
	public @Nullable ICommand getCommandByID(@Nonnull @Nonnegative Long commandID);
	
	/**
	 * @return the amount of {@link ICommand commands} tracked by this component
	 */
	public long getTrackingSize();
}
