package org.paxle.desktop.backend.impl.jdic;

import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenuItem;

import org.paxle.desktop.backend.tray.IMenuItem;

public class MenuItem extends JMenuItem implements IMenuItem {
	
	private static final long serialVersionUID = 1L;
	
	public MenuItem() {
	}
	
	public MenuItem(Icon icon) {
		super(icon);
	}
	
	public MenuItem(String text) {
		super(text);
	}
	
	public MenuItem(Action a) {
		super(a);
	}
	
	public MenuItem(String text, Icon icon) {
		super(text, icon);
	}
	
	public MenuItem(String text, int mnemonic) {
		super(text, mnemonic);
	}
	
	public void init(String text, String actionCommand, ActionListener actionListener) {
		super.setText(text);
		super.setActionCommand(actionCommand);
		super.addActionListener(actionListener);
	}
}
