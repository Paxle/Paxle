
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
		SETTINGS, STATS, CCONSOLE, BUNDLES
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
}
