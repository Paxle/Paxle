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

/**
 * A service to access desktop-integration-related functions like opening a browser and
 * redirect it to a specific URL, to control the visibility state of the tray-icon or to
 * manage the dialogues provided by this bundle.
 */
public interface IDesktopServices {
	
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
	}
	
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
	
	/**
	 * @see #browseUrl(String)
	 * @return whether attempts to open the user's default browser can be made by the underlying
	 *         implementation or - if previous attempts have already been made - whether those
	 *         were successful.
	 */
	public boolean isBrowserOpenable();
	
	/**
	 * Attempts to open the user's default browser and to direct it to the URL given by the parameter.
	 * Opening a browser may not be supported by the system or installed programs on it or fail due to
	 * various other reasons. Even if this method indicates success, the user's browser may not have been
	 * touched at all. No further tracing is available through this interface because the methods used to
	 * call the browser are highly implementation-dependant.
	 * <p>
	 * <i>Please be aware, that this method may use JNI as well as libraries not present on each system
	 *    which supports a JRE</i>
	 * @param url the URL-string to be opened to the browser-program
	 * @return whether the underlying implementation indicates success in opening the user's browser
	 */
	public boolean browseUrl(String url);
	
	/**
	 * A convenience method which transforms the given path-element(s) into an absolute URL-string
	 * pointing to the web-interface of the Paxle GUI.
	 * @see the bundle "GUI"
	 * @param path the path-element(s) of a servlet a URL to shall be generated
	 * @return the absolute URL
	 */
	public String getPaxleUrl(String... path);
	
	/**
	 * Shows the tray-icon, associates a menu-list to it which is displayed on a right-click by the
	 * user on the icon.
	 * <i>Please be aware, that this method may use JNI as well as libraries not present on each system</i>
	 */
	public void setTrayMenuVisible(boolean yes);
	
	/**
	 * @return whether the system tray is currently visible or not
	 */
	public boolean isTrayMenuVisible();
	
	public void addDIEventListener(final IDIEventListener listener);
	public void removeDIEventListener(final IDIEventListener listener);
}