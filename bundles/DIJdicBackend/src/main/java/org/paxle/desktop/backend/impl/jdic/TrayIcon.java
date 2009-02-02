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
package org.paxle.desktop.backend.impl.jdic;

import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;

import org.paxle.desktop.backend.tray.IPopupMenu;
import org.paxle.desktop.backend.tray.ITrayIcon;

public class TrayIcon extends org.jdesktop.jdic.tray.TrayIcon implements ITrayIcon {
	
	private ImageIcon icon = null;
	private IPopupMenu menu = null;
	private String tooltip = null;
	private String caption = null;
	private boolean iconAutoSize = false;
	
	public TrayIcon(Icon icon) {
		super(null, null, null);
		setIcon(icon);
	}
	
	public TrayIcon(Icon icon, String caption) {
		this(icon);
		setCaption(caption);
	}
	
	public TrayIcon(Icon icon, String caption, IPopupMenu popupMenu) {
		this(icon, caption);
		setMenu(popupMenu);
	}
	
	@Override
	public void addActionListener(ActionListener arg0) {
		super.addActionListener(arg0);
	}
	
	public void displayBalloonMessage(String caption, String text, MessageType messageType) {
		final int type;
		switch (messageType) {
			case ERROR: type = ERROR_MESSAGE_TYPE; break;
			case INFO: type = INFO_MESSAGE_TYPE; break;
			case NONE: type = NONE_MESSAGE_TYPE; break;
			case WARNING: type = WARNING_MESSAGE_TYPE; break;
			default:
				throw new RuntimeException("incomplete switch statement over enum type");
		}
		super.displayMessage(caption, text, type);
	}
	
	public String getCaption() {
		return this.caption;
	}
	
	@Override
	public void setCaption(String caption) {
		super.setCaption(caption);
		this.caption = caption;
	}
	
	public void setIcon(ImageIcon icon) {
		super.setIcon(icon);
		this.icon = icon;
	}
	
	public ImageIcon getIcon() {
		return this.icon;
	}
	
	public IPopupMenu getMenu() {
		return this.menu;
	}
	
	public Dimension getSize() {
		if (this.icon == null)
			return null;
		return new Dimension(this.icon.getIconWidth(), this.icon.getIconHeight());
	}
	
	public String getTooltip() {
		return this.tooltip;
	}
	
	public void init(ImageIcon icon, String tooltip, IPopupMenu popupMenu) {
		setIcon(icon);
		setImageAutoSize(true);
		setToolTip(tooltip);
		setMenu(popupMenu);
	}
	
	public boolean isImageAutoSize() {
		return this.iconAutoSize;
	}
	
	public void setImageAutoSize(boolean yes) {
		super.setIconAutoSize(yes);
		this.iconAutoSize = yes;
	}
	
	public void setMenu(IPopupMenu popupMenu) {
		super.setPopupMenu((JPopupMenu)popupMenu);
		this.menu = popupMenu;
	}
	
	@Override
	public void setToolTip(String tooltip) {
		super.setToolTip(tooltip);
		this.tooltip = tooltip;
	}
}
