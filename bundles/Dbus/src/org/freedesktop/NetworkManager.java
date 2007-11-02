package org.freedesktop;

import java.util.List;

import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.Path;
import org.freedesktop.dbus.exceptions.DBusException;

/**
 * @see <a href="http://people.redhat.com/dcbw/NetworkManager/NetworkManager%20DBUS%20API.txt">NetworkManager DBUS API.txt</a>
 * 
 * "The problem is, sometimes the 'DeviceNowActive' callback,
 * gc.nm_device_active_cb, gets called with 2 arguments, and sometimes with 3
 * arguments.  I think its 2 for wireline and 3 for wireless."
 * [http://osdir.com/ml/freedesktop.dbus/2006-04/msg00154.html]
 */
public interface NetworkManager extends DBusInterface {

	public static class DeviceSignal extends DBusSignal {
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
	 * 	Name:	DeviceNoLongerActive		Signals that a network device is no longer active
	 * 	Args:	1) Device object			(DBUS_TYPE_STRING)	- The deactivated network device
	 * </pre>
	 * @see <a href="http://people.redhat.com/dcbw/NetworkManager/NetworkManager%20DBUS%20API.txt">NetworkManager DBUS API.txt</a>
	 */
	public static class DeviceNoLongerActive extends DeviceSignal {
		public DeviceNoLongerActive(String path, Path device) throws DBusException {
			super(path, device);
		}
	}	
	
	/**
	 * <pre>
	 * 	Name:	DeviceNowActive			Signals that a network device is newly activated
	 * 	Args:	1) Device object			(DBUS_TYPE_STRING)	- The newly activated network device
	 * </pre>
	 * @see <a href="http://people.redhat.com/dcbw/NetworkManager/NetworkManager%20DBUS%20API.txt">NetworkManager DBUS API.txt</a>
	 */
	public static class DeviceNowActive extends DeviceSignal {
		public DeviceNowActive(String path, Path device) throws DBusException {
			super(path, device);
		}
	}	
	
	/**
	 * <pre>
	 * 	Name:	DevicesChanged				Signals that a device was either added or removed from the system
	 * 	Args:	1) Device object			(DBUS_TYPE_STRING)	- The device which was added or removed
	 * </pre>
	 * @see <a href="http://people.redhat.com/dcbw/NetworkManager/NetworkManager%20DBUS%20API.txt">NetworkManager DBUS API.txt</a>
	 */
	public static class DevicesChanged extends DeviceSignal {
		public DevicesChanged(String path, Path device) throws DBusException {
			super(path, device);
		}
	}	
	
	/**
	 * <pre>
	 * 	Name:	getDevices		Get the list of network devices NM knows about
	 * 	Args:	(none)
	 * 	Returns:	DBUS String Array			Each item in the array is the NM identifier of a Device object
	 * </pre>
	 * @see <a href="http://people.redhat.com/dcbw/NetworkManager/NetworkManager%20DBUS%20API.txt">NetworkManager DBUS API.txt</a>
	 */
	public List<Path> getDevices();
}
