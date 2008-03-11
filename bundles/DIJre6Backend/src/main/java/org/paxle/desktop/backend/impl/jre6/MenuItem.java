package org.paxle.desktop.backend.impl.jre6;

import java.awt.HeadlessException;
import java.awt.MenuShortcut;
import java.awt.event.ActionListener;

import org.paxle.desktop.backend.tray.IMenuItem;

public class MenuItem extends java.awt.MenuItem implements IMenuItem {
	
	private static final long serialVersionUID = 1L;
	
	public MenuItem() throws HeadlessException {
	}
	
	public MenuItem(String label) throws HeadlessException {
		super(label);
	}
	
	public MenuItem(String label, MenuShortcut s) throws HeadlessException {
		super(label, s);
	}
	
	public String getText() {
		return super.getLabel();
	}
	
	public void init(String text, String actionCommand, ActionListener actionListener) {
		super.setLabel(text);
		super.setActionCommand(actionCommand);
		super.addActionListener(actionListener);
	}
	
	public void setText(String text) {
		super.setLabel(text);
	}
}
