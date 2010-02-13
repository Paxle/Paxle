/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2010 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.gnome;

import org.freedesktop.DBus;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.exceptions.DBusException;

/**
 * @see "http://lists.freedesktop.org/archives/xdg/2006-June/006523.html"
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
			super(path, Boolean.valueOf(isIdle));
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
			super(path, Boolean.valueOf(isActive));
			this.isActive = isActive;
		}
	}	
	
	public boolean GetSessionIdle();
	
	@DBus.Method.NoReply
	public void Lock();
}
