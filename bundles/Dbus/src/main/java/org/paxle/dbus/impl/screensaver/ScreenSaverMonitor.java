package org.paxle.dbus.impl.screensaver;

import org.freedesktop.dbus.DBusSigHandler;
import org.freedesktop.dbus.DBusSignal;
import org.gnome.ScreenSaver;

public class ScreenSaverMonitor implements DBusSigHandler<DBusSignal> {

	public void handle(DBusSignal signal) {
		if (signal instanceof ScreenSaver.ActiveChanged) {
			System.out.println("ActiveChanged to " + signal.toString());
		} else if (signal instanceof ScreenSaver.SessionIdleChanged) {
			System.out.println("IdleChanged to " + signal.toString());
		}
	}

}
