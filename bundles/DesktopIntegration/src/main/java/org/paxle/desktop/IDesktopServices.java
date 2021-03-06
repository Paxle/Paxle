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

package org.paxle.desktop;

/**
 * A service to access desktop-integration-related functions like opening a browser and
 * redirect it to a specific URL, to control the visibility state of the tray-icon or to
 * manage the dialogues provided by this bundle.
 */
public interface IDesktopServices {
	
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
}
