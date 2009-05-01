/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.desktop;

public interface IDialogueServices {
	
	/**
	 * A collection of constants of which each stands for a dialogue or panel, this bundle
	 * provides. These dialogues can be accessed using {@link IDesktopServices#openDialogue(Dialogues)}
	 * and {@link IDesktopServices#closeDialogue(Dialogues)}.
	 * However, interaction with the dialogues is restricted to these methods and no further
	 * access is provided to restrict possible interference and hide implementational details.
	 * If enhancements or more functionality is required, a {@link DIComponent} may be registered
	 * to this bundle.
	 */
	public static enum Dialogues {
		/**
		 * A settings-dialogue which provides access to all
		 * {@link org.osgi.service.cm.ManagedService managed-services} registered to the OSGi-framework.
		 */
		/*
		 * (non-Javadoc)
		 * @see org.paxle.desktop.impl.dialogues.settings.SettingsPanel
		 */
		SETTINGS,
		
		/**
		 * A dialogue presenting statistical information about Paxle's core-components like the
		 * PPM of CrawlerCore, ParserCore and Indexer, sizes of the DataLayers and IndexLucenes
		 * databases and memory.
		 */
		/*
		 * (non-Javadoc)
		 * @see org.paxle.desktop.impl.dialogues.stats.StatisticsPanel
		 */
		STATS,
		
		/**
		 * A debug-dialogue which provides access to the {@link org.paxle.core.queue.ICommand ICommands}
		 * currently processed by the core-components via {@link org.osgi.service.event.Event events}.
		 */
		/*
		 * (non-Javadoc)
		 * @see org.paxle.desktop.impl.dialogues.CrawlingConsole
		 */
		CCONSOLE,
		
		/**
		 * A dialogue providing access to meta-data for all bundles currently installed, like
		 * imported/exported services, the bundle's manifest- and runtime-information.
		 */
		/*
		 * (non-Javadoc)
		 * @see org.paxle.desktop.impl.dialogues.bundles.BundlePanel
		 */
		BUNDLES
	};
	
	/**
	 * Opens and displays a dialoge provided by this bundle. If the dialogue already is open,
	 * it is moved to the front.
	 * @see Dialogues
	 * @param d the {@link Dialogues dialogue} to be displayed.
	 */
	public void openDialogue(Dialogues d);
	
	/**
	 * Closes an open dialogue. If the dialogue is already closed, this method does nothing.
	 * @param d the {@link Dialogues dialogue} to close.
	 */
	public void closeDialogue(Dialogues d);
	
	public void addDIEventListener(final IDIEventListener listener);
	public void removeDIEventListener(final IDIEventListener listener);
}
