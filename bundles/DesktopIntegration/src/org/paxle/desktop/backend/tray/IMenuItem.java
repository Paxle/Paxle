
package org.paxle.desktop.backend.tray;

import java.awt.event.ActionListener;

public interface IMenuItem {
	
	public void addActionListener(ActionListener al);
	public void removeActionListener(ActionListener al);
	public void setText(String text);
	public void setActionCommand(String actionCommand);
	public String getText();
	public String getActionCommand();
	public void init(String text, String actionCommand, ActionListener actionListener);
	public void setEnabled(boolean yes);
}
