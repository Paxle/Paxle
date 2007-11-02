package org.gnome;

import org.freedesktop.DBus;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.exceptions.DBusException;

/**
 * @see http://lists.freedesktop.org/archives/xdg/2006-June/006523.html
 */
public interface ScreenSaver extends DBusInterface {
	public static class SessionIdleChanged extends DBusSignal {
		public final boolean isIdle;
		
		/**
		 * @param path the object path 
		 * @param isIdle the value of the current state of activity
		 * @throws DBusException
		 */
		public SessionIdleChanged(String path, boolean isIdle) throws DBusException {
			super(path, isIdle);
			this.isIdle = isIdle;
		}
	}		
	
	public static class ActiveChanged extends DBusSignal {
		public final boolean isActive;
		
		/**
		 * 
		 * @param path the object path 
		 * @param isActive the value of the current state of activity
		 * @throws DBusException
		 */
		public ActiveChanged(String path, boolean isActive) throws DBusException {
			super(path, isActive);
			this.isActive = isActive;
		}
	}	
	
	public boolean GetSessionIdle();
	
	@DBus.Method.NoReply
	public void Lock();
}
