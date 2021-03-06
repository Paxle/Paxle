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

package org.paxle.core.queue;

import java.net.URI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.paxle.core.doc.ICommand;

/**
 * The purpose of this context object is to encapsulate the whole filtering process that is done
 * by an {@link ICommandFilterQueue filter-queue}.
 */
public interface ICommandFilteringContext<Cmd extends ICommand> {
	/**
	 * Function to dequeue a command from an input-queue
	 * @return the filtered command or <code>null</code> if the command was filtered by one of the applied command-filters
	 * @throws IllegalStateException if you call this method on an filtering-context created by an output-queue
	 */
	public @Nullable Cmd dequeue();
	
	/**
	 * Function to enqueue a command into an output-queue.
	 * @param command the command that should be filtered by all command-filters applied to the queue
	 * @throws InterruptedException
	 * @throws IllegalStateException if you call this method on an filtering-context created by an input-queue
	 */
	public void enqueue(@Nonnull Cmd command) throws InterruptedException;
	
	/**
	 * @return the location of the command that is filtered by filters within the filtering-context
	 */
	public URI getLocation();
	
	/**
	 * @return <code>true</code> if the filtering-process has already finished or <code>false</code> if filtering is still in progress or was not started so far.
	 */
	public boolean done();
	
	/**
	 * Returns the command that was passed to this filter-context before the command is actually filtered. 
	 * To ensure that the returned command can not be modified, it is decorated using a dynamic proxy.
	 * 
	 * Currently only a view method-calls are allowed:
	 * <ul>
	 * 	<li>{@link ICommand#getProfileOID()}</li>
	 *  <li>{@link ICommand#getDepth()}</li>
	 *  <li>{@link ICommand#getLocation()}</li>
	 *  <li>{@link ICommand#toString()}</li>
	 *  <li>{@link ICommand#getCrawlerDocument()}</li>
	 *  <li>{@link org.paxle.core.doc.ICrawlerDocument#getMimeType()}</li>
	 *  <li>{@link org.paxle.core.doc.ICrawlerDocument#getSize()}</li>
	 *  <li>{@link org.paxle.core.doc.ICrawlerDocument#toString()}</li>
	 * </ul>
	 *  
	 * @return a proxy object decorating the pre-filtered command or <code>null</code> if the command was already filtered by this context 
	 */
	public @Nullable Cmd getPreFilteredCmd();
}
