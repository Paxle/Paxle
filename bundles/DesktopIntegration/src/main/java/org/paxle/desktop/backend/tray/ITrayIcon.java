
package org.paxle.desktop.backend.tray;

import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;

public interface ITrayIcon {
	
	// from Java 1.6, java.awt.TrayIcon.MessageType
	/**
     * The message type determines which icon will be displayed in the
     * caption of the message, and a possible system sound a message
     * may generate upon showing.
     * @see ITrayIcon#displayBalloonMessage(String, String, ITrayIcon.MessageType)
     */
	public static enum MessageType {
        /** An error message */
        ERROR,
        /** A warning message */
        WARNING,
        /** An information message */
        INFO,
        /** Simple message */
        NONE
    };
	
    /**
     * Initializes a newly created {@link ITrayIcon} with an icon, a tooltip which is shown when
     * the user hovers over the icon and a popup-menu triggered by a click with the secondary
     * mouse-button on the icon. This method should only be called once.
     * @param icon the {@link ImageIcon} to display in the system tray
     * @param tooltip a short {@link String} informing the user of the application the icon
     *        belongs to
     * @param popupMenu the {@link IPopupMenu} to display when a click with the secondary
     *        mouse-button is performed on the tray-icon.
     * @see org.paxle.desktop.backend.IDIBackend#createPopupMenu(IMenuItem...)
     */
    public void init(ImageIcon icon, String tooltip, IPopupMenu popupMenu);
    
    /**
     * Sets the icon displayed in the system-tray for this {@link ITrayIcon}.
     * @param icon the {@link ImageIcon} to display
     */
	public void setIcon(ImageIcon icon);
	
	/**
	 * @return this {@link ITrayIcon}s currently active icon or <code>null</code> if not set yet
	 */
	public ImageIcon getIcon();
	
	/**
	 * Sets the popup-menu displayed when the user clicks on this tray-icon using the
	 * secondary mouse-button
	 * @param popupMenu the {@link IPopupMenu} to show when the user clicks on this icon
	 */
	public void setMenu(IPopupMenu popupMenu);
	
	/**
	 * @return this {@link ITrayIcon}s currently active popup-menu or <code>null</code> if not
	 *         set yet
	 */
	public IPopupMenu getMenu();
	
	/**
	 * Sets the tooltip displayed when the user hovers over this tray-icon. It should be a message
	 * informing about the application this icon is part of and potentially a short status description
	 * if applicable.
	 * @param tooltip the {@link String} to show when hovering this icon
	 */
	public void setToolTip(String tooltip);
	
	/**
	 * @return this {@link ITrayIcon}s tooltip message or <code>null</code> if not set yet
	 */
	public String getTooltip();
	
	/**
	 * In most operating-systems the height (or width respectively) of the bar containing the system-tray
	 * is adjustable by the user. In order to integrate this icon properly, the given icon can be
	 * auto-adjusted in height/width to fit the size of the system-tray. The aspect-ratio will be maintained.
	 * @param yes whether to use auto-adjustment to the space available for a tray-icon in the system-tray or not
	 * @see #setIcon(ImageIcon)
	 */
	public void setImageAutoSize(boolean yes);
	
	/**
	 * @return whether this {@link ITrayIcon}s icon is auto-adjusted to the space available for a tray-icon in the
	 *         the system-tray
	 * @see #setImageAutoSize(boolean)
	 */
	public boolean isImageAutoSize();
	
	/**
	 * Show an informative message to the user, placed next to or above/below the system-tray bar.
	 * @param caption the caption of the message window
	 * @param text the text to show as content of the message window
	 * @param messageType the {@link MessageType type of information} of the balloon message
	 * @see MessageType
	 */
	public void displayBalloonMessage(String caption, String text, MessageType messageType);
	
	/**
	 * <i>This method is not guaranteed to work equally on all backends</i>.
	 * @return the size of the displayed icon. Results depend on whether {@link #setImageAutoSize(boolean)} is set as
	 *         well as on the actual desktop-integration backend used.
	 */
	public Dimension getSize();
	
	/**
	 * Add an {@link ActionListener} to this icon. An {@link java.awt.event.ActionEvent} will be triggered when the user
	 * clicks on this icon.
	 * <p><i>This method is not guaranteed to work equally on all backends</i>.
	 * @param l an {@link ActionListener} which will receive the triggered events
	 */
	public void addActionListener(ActionListener l);
}
