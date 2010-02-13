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

package org.freedesktop;

import java.util.List;

import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.Path;
import org.freedesktop.dbus.UInt32;
import org.freedesktop.dbus.exceptions.DBusException;

/**
 * Use the following code to get a reference to the {@link NetworkManager} objects:
 * <code><pre>
 * NetworkManager nm = (NetworkManager) conn.getRemoteObject(
 * 	"org.freedesktop.NetworkManager", 
 * 	"/org/freedesktop/NetworkManager", 
 * 	NetworkManager.class
 * );
 * 
 * "The problem is, sometimes the 'DeviceNowActive' callback,
 * gc.nm_device_active_cb, gets called with 2 arguments, and sometimes with 3
 * arguments.  I think its 2 for wireline and 3 for wireless."
 * [http://osdir.com/ml/freedesktop.dbus/2006-04/msg00154.html]
 * 
 * @see <a href="http://people.redhat.com/dcbw/NetworkManager/NetworkManager%20DBUS%20API.txt">NetworkManager DBUS API.txt</a>
 * @see <a href="http://mail.gnome.org/archives/networkmanager-list/2006-October/msg00233.html">List of DBus methods for talking to NetworkManager</a>
 */
public interface NetworkManager extends DBusInterface {

	/* ========================================================================
	 * NETWORK-MANAGER SIGNALS
	 * ======================================================================== */

	/**
	 * Abstract class, inherited by the signals listed below
	 */
	public abstract static class DeviceSignal extends DBusSignal {
		private Path device = null;

		public DeviceSignal(String path, Path device) throws DBusException {
			super(path, device);
			this.device = device;
		}

		public Path getDevice() {
			return this.device;
		}
	}

	/**
	 * <pre>
	 * Name:    DeviceNoLongerActive    Signals that a network device is no longer active
	 * Args:    1) Device object        (DBUS_TYPE_STRING) - The deactivated network device
	 * </pre>
	 * @see <a href="http://people.redhat.com/dcbw/NetworkManager/NetworkManager%20DBUS%20API.txt">NetworkManager DBUS API.txt</a>
	 * @deprecated use {@link DeviceRemoved} instead
	 */
	public static class DeviceNoLongerActive extends DeviceSignal {
		public DeviceNoLongerActive(String path, Path device) throws DBusException {
			super(path, device);
		}
	}	
	
	public static class DeviceRemoved extends DBusSignal {
		protected DeviceRemoved(String arg0, Object[] arg1) throws DBusException {
			super(arg0, arg1);
		}
	}

	/**
	 * <pre>
	 * Name:    DeviceNowActive         Signals that a network device is newly activated
	 * Args:    1) Device object        (DBUS_TYPE_STRING) - The newly activated network device
	 * </pre>
	 * @see <a href="http://people.redhat.com/dcbw/NetworkManager/NetworkManager%20DBUS%20API.txt">NetworkManager DBUS API.txt</a>
	 * @deprecated use {@link DeviceAdded} instead
	 */
	public static class DeviceNowActive extends DeviceSignal {
		public DeviceNowActive(String path, Path device) throws DBusException {
			super(path, device);
		}
	}	

	public static class DeviceAdded extends DBusSignal {
		protected DeviceAdded(String arg0, Object[] arg1) throws DBusException {
			super(arg0, arg1);
		}
	}
	
	/**
	 * <pre>
	 * Name:    DevicesChanged          Signals that a device was either added or removed from the system
	 * Args:    1) Device object        (DBUS_TYPE_STRING) - The device which was added or removed
	 * </pre>
	 * @see <a href="http://people.redhat.com/dcbw/NetworkManager/NetworkManager%20DBUS%20API.txt">NetworkManager DBUS API.txt</a>
	 * @deprecated use {@link StateChanged} instead
	 */
	public static class DevicesChanged extends DeviceSignal {
		public DevicesChanged(String path, Path device) throws DBusException {
			super(path, device);
		}
	}	
	

	/* ========================================================================
	 * NETWORK-MANAGER DEVICE OBJECTS
	 * ======================================================================== */
	/**
	 * <pre>
	 * The Device object is the NM representation of a network device.  To refer to a NM Device, 
	 * you must use the following constants when creating your DBUS message:
	 * 
	 * DBUS Service:        "org.freedesktop.NetworkManager"
	 * DBUS Interface:      "org.freedesktop.NetworkManager.Devices"
	 * </pre>
	 * @see <a href="http://people.redhat.com/dcbw/NetworkManager/NetworkManager%20DBUS%20API.txt">NetworkManager DBUS API.txt</a>
	 * @deprecated
	 */
	public static interface Devices extends DBusInterface {
		/**
		 * <pre>
		 * Name:    getName             Returns the system device name of the Device object (i.e. eth0)
		 * Args:    (none)
		 * Returns: DBUS_TYPE_STRING    The system device name
		 * </pre>
		 * @see <a href="http://people.redhat.com/dcbw/NetworkManager/NetworkManager%20DBUS%20API.txt">NetworkManager DBUS API.txt</a>
		 */
		public String getName();

		/**
		 * <pre>
		 * Name:    getType             Returns the type of the device (ie wired, wireless, isdn, bluetooth, etc)
		 * Args:    (none)
		 * Returns: DBUS_TYPE_INT32     0 - unknown type
		 *                              1 - Wired ethernet
		 *                              2 - Wireless (802.11a/b/g)
		 * </pre>
		 * @see <a href="http://people.redhat.com/dcbw/NetworkManager/NetworkManager%20DBUS%20API.txt">NetworkManager DBUS API.txt</a>
		 */
		public int getType();

		/**
		 * <pre>
		 * Name:    getHalUdi           Returns the HAL UDI of the device
		 * Args:    (none)
		 * Returns: DBUS_TYPE_STRING
		 * </pre>
		 * @see <a href="http://people.redhat.com/dcbw/NetworkManager/NetworkManager%20DBUS%20API.txt">NetworkManager DBUS API.txt</a>
		 */
		public String getHalUdi();

		/**
		 * <pre>
		 * Name:    getIP4Address       Returns the IPv4 address of the device
		 * Args:    (none)
		 * Returns: DBUS_TYPE_UINT32    The IPv4 address in network byte order
		 * </pre>
		 * @see <a href="http://people.redhat.com/dcbw/NetworkManager/NetworkManager%20DBUS%20API.txt">NetworkManager DBUS API.txt</a>
		 */
		public UInt32 getIP4Address();

		/**
		 * <pre>
		 * Name:    getLinkActive       Returns the link state of the device
		 * Args:    (none)
		 * Returns: DBUS_TYPE_BOOLEAN   TRUE - the device has a valid network link
		 *                                  Wired: cable is plugged in
		 *                                  Wireless: good link to a base station
		 *                              FALSE - the device has no network link
		 *                                  Wired: no cable plugged in
		 *                                  Wireless: no base station, or bad encryption key
		 * </pre>
		 * @see <a href="http://people.redhat.com/dcbw/NetworkManager/NetworkManager%20DBUS%20API.txt">NetworkManager DBUS API.txt</a>
		 */
		public boolean getLinkActive();
	}	
	
	public static interface Device extends DBusInterface {
		
	}

	/**
	 * <pre>
	 * Name:    getDevices          Get the list of network devices NM knows about
	 * Args:    (none)
	 * Returns: DBUS String Array   Each item in the array is the NM identifier of a Device object
	 * </pre>
	 * 
	 * Use the following code to get a reference to the corresponding {@link Devices} objects:
	 * <code><pre>
	 * NetworkManager nm = (NetworkManager) conn.getRemoteObject(
	 * 	"org.freedesktop.NetworkManager", 
	 * 	"/org/freedesktop/NetworkManager", 
	 * 	NetworkManager.class
	 * );
	 * List<Path> devices = nm.getDevices();
	 * 
	 * NetworkManager.Devices d = conn.getRemoteObject(
	 * 	"org.freedesktop.NetworkManager",
	 * 	devices.get(0).toString(),
	 * 	NetworkManager.Devices.class
	 * );
	 * </pre></code>
	 * <p />
	 * 
	 * @see <a href="http://people.redhat.com/dcbw/NetworkManager/NetworkManager%20DBUS%20API.txt">NetworkManager DBUS API.txt</a>
	 * @deprecated use {@link #GetDevices()} instead
	 */
	public List<Path> getDevices();
	
	/**
	 * @since DBus 0.7.0
	 * @return
	 */
	public List<Path> GetDevices();
}
