
package org.paxle.desktop.backend.tray;

import java.awt.Dimension;

import javax.swing.ImageIcon;

public interface ITrayIcon {
	
	// from Java 1.6, java.awt.TrayIcon.MessageType
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
	
    public void init(ImageIcon icon, String tooltip, IPopupMenu popupMenu);
	public void setIcon(ImageIcon icon);
	public ImageIcon getIcon();
	public void setMenu(IPopupMenu popupMenu);
	public IPopupMenu getMenu();
	public void setToolTip(String tooltip);
	public String getTooltip();
	public void setImageAutoSize(boolean yes);
	public boolean isImageAutoSize();
	public void displayBalloonMessage(String caption, String text, MessageType messageType);
	public Dimension getSize();
}
