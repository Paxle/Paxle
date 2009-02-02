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
package org.paxle.desktop.backend.impl.jre6;

import java.awt.PopupMenu;

import javax.swing.ImageIcon;

import org.paxle.desktop.backend.tray.IPopupMenu;
import org.paxle.desktop.backend.tray.ITrayIcon;

public class TrayIcon extends java.awt.TrayIcon implements ITrayIcon {
	
	public TrayIcon(ImageIcon image) {
		super(image.getImage());
	}
	
	public TrayIcon(ImageIcon image, String tooltip) {
		super(image.getImage(), tooltip);
	}
	
	public TrayIcon(ImageIcon image, String tooltip, IPopupMenu popup) {
		super(image.getImage(), tooltip, (PopupMenu)popup);
	}
	
	public void init(ImageIcon icon, String tooltip, IPopupMenu popupMenu) {
		setIcon(icon);
		setToolTip(tooltip);
		setMenu(popupMenu);
	}
	
	public void displayBalloonMessage(String caption, String text, org.paxle.desktop.backend.tray.ITrayIcon.MessageType messageType) {
		final java.awt.TrayIcon.MessageType type;
		switch (messageType) {
			case ERROR: type = java.awt.TrayIcon.MessageType.ERROR; break;
			case INFO: type = java.awt.TrayIcon.MessageType.INFO; break;
			case NONE: type = java.awt.TrayIcon.MessageType.NONE; break;
			case WARNING: type = java.awt.TrayIcon.MessageType.WARNING; break;
			default:
				throw new RuntimeException("incomplete switch statement over enum type");
		}
		super.displayMessage(caption, text, type);
	}
	
	public String getTooltip() {
		return super.getToolTip();
	}
	
	public void setIcon(ImageIcon icon) {
		super.setImage(icon.getImage());
	}
	
	public ImageIcon getIcon() {
		return new ImageIcon(super.getImage());
	}
	
	public void setMenu(IPopupMenu popupMenu) {
		super.setPopupMenu((PopupMenu)popupMenu);
	}
	
	public IPopupMenu getMenu() {
		return (IPopupMenu)super.getPopupMenu();
	}
}
