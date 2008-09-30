
package org.paxle.desktop.impl;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.InvalidSyntaxException;
import org.paxle.desktop.IDesktopServices.Dialogues;

public class DICommandProvider implements CommandProvider {
	
	private final DesktopServices services;
	
	public DICommandProvider(final DesktopServices services) {
		this.services = services;
	}
	
	public void _desktop(final CommandInterpreter ci) throws InvalidSyntaxException {
		handleDesktop(ci, ci.nextArgument());
	}
	
	private void handleDesktop(final CommandInterpreter ci, final String what) throws InvalidSyntaxException {
		if (what == null) {
			ci.println("No argument given!");
			return;
		} else if (what.equals("open")) {
			final String which = ci.nextArgument();
			if (which == null) {
				final Dialogues[] dialogues = Dialogues.values();
				for (int i=0; i<dialogues.length;) {
					ci.print(dialogues[i].name().toLowerCase());
					if (++i < dialogues.length)
						ci.print(", ");
				}
				ci.println();
			} else {
				final Dialogues dialogue;
				try {
					dialogue = Dialogues.valueOf(which.toUpperCase());
				} catch (RuntimeException e) {
					ci.println("cannot access dialogue '" + what + "': " + e.getMessage());
					return;
				}
				services.openDialogue(dialogue);
			}
		} else if (what.equals("tray")) {
			services.setTrayMenuVisible(!services.isTrayMenuVisible());
		} else {
			ci.println("parameter '" + what + "' not understood");
			return;
		}
	}
	
	public String getHelp() {
		final StringBuilder buf = new StringBuilder();
		buf.append("---Controlling the desktop bundle---\r\n")
		   .append("\tdesktop - DesktopIntegration-related commands\r\n")
		   .append("\t   open ... - open dialogues, when invoked without args lists available ones\r\n")
		   .append("\t   tray     - toggle tray menu visibility\r\n");
		return buf.toString();
	}
}
