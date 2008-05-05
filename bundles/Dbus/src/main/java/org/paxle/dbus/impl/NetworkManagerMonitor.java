package org.paxle.dbus.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freedesktop.NetworkManager;
import org.freedesktop.NetworkManager.DeviceSignal;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusSigHandler;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.Path;
import org.freedesktop.dbus.exceptions.DBusException;
import org.paxle.core.IMWComponent;
import org.paxle.dbus.IDbusService;

/**
 * A class to receive signals from the {@link NetworkManager}
 */
public class NetworkManagerMonitor implements DBusSigHandler<DeviceSignal>, IDbusService {
	
	private Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * Reference to the crawler component
	 */
	private IMWComponent crawler = null;
	
	/**
	 * The connection to the dbus
	 */
	private DBusConnection conn = null; 
	
	/**
	 * A list of network devices installed on this computer
	 */
	private HashSet<Path> devices = new HashSet<Path>();
	
	/**
	 * The list of {@link DBusSignal signals} to (un)register.
	 */
	private static ArrayList<Class> signals = new ArrayList<Class>(Arrays.asList(new Class[]{
			NetworkManager.DeviceNoLongerActive.class,
			NetworkManager.DeviceNowActive.class,
			NetworkManager.DevicesChanged.class
	}));
	
	public NetworkManagerMonitor() throws DBusException {		
		this.logger.info("Connecting to dbus ...");
		this.conn = DBusConnection.getConnection(DBusConnection.SYSTEM);
		
		NetworkManager nm = (NetworkManager) conn.getRemoteObject("org.freedesktop.NetworkManager", "/org/freedesktop/NetworkManager", NetworkManager.class);
		List<Path> deviceList = nm.getDevices();
		if (deviceList != null) {
			this.devices.addAll(deviceList);
		}
	}
	
	/**
	 * @see IDbusService#disconnect()
	 */
	public void disconnect() {
		this.conn.disconnect();
	}
	
	public void registerSignalListener(IMWComponent crawler) {
		try {
			this.crawler = crawler;
			
			this.logger.info("Register network-manager signal listeners ...");
			for (Class<DeviceSignal> signal : signals) {
				conn.addSigHandler(signal, this);
			}
		} catch (DBusException e) {
			this.logger.error("Unable to register as dbus-signal-listener",e);
		}
	}
	
	public void unregisterSignalListener() {
		try {
			this.crawler = null;
			
			this.logger.info("Unregister network-manager signal listeners ...");
			for (Class<DeviceSignal> signal : signals) {
				conn.removeSigHandler(signal,this);
			}
		} catch (DBusException e) {
			this.logger.error("Unable to unregister as dbus-signal-listener",e);
		}
	}
	
	/**
	 * @see DBusSigHandler#handle(DBusSignal)
	 */
	public void handle(DeviceSignal signal) {
		Path device = signal.getDevice();
		
		if (signal instanceof NetworkManager.DeviceNoLongerActive) {
			System.out.println(String.format("Device %s was deactivated.",signal.getDevice()));
			this.devices.remove(device);
			if (this.devices.size() == 0) {
				System.out.println("No network device left. Pausing crawler ...");
				this.crawler.pause();
			}
		} else if (signal instanceof NetworkManager.DeviceNowActive) {
			System.out.println(String.format("Device %s was activated.",signal.getDevice()));
			this.devices.add(device);
			if (this.devices.size() > 0) {
				System.out.println("Network device found. Resuming crawler ...");
				this.crawler.resume();
			}
		} else if (signal instanceof NetworkManager.DevicesChanged) {
			System.out.println("Device was changed: " + signal.toString());
			// TODO: what todo here?
		}
	}
}
