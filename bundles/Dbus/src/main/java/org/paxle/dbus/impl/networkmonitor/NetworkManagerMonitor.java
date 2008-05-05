package org.paxle.dbus.impl.networkmonitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freedesktop.NetworkManager;
import org.freedesktop.DBus.Error.NoReply;
import org.freedesktop.NetworkManager.DeviceSignal;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusSigHandler;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.Path;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.paxle.core.IMWComponent;
import org.paxle.dbus.IDbusService;

/**
 * A class to receive signals from the {@link NetworkManager}
 */
public class NetworkManagerMonitor implements DBusSigHandler<DeviceSignal>, IDbusService, ServiceTrackerCustomizer {
	
	private Log logger = LogFactory.getLog(this.getClass());
	
	private BundleContext context = null;
	
	/**
	 * An OSGi {@link ServiceTracker} to track the paxle crawler service.
	 */
	private ServiceTracker tracker = null; 
	
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
	
	public NetworkManagerMonitor(BundleContext context) throws DBusException, InvalidSyntaxException {
		try {
			// connecting to dbus
			this.logger.info("Connecting to dbus ...");
			this.conn = DBusConnection.getConnection(DBusConnection.SYSTEM);

			// registering an OSGi service tracker
			this.context = context;
			Filter filter = context.createFilter(String.format(
					"(&(%s=%s)(component.ID=org.paxle.crawler))",
					Constants.OBJECTCLASS, 
					IMWComponent.class.getName())
			);
			this.tracker = new ServiceTracker(this.context,filter,this);
			this.tracker.open();

			// getting the network-manager via dbus
			NetworkManager nm = (NetworkManager) conn.getRemoteObject("org.freedesktop.NetworkManager", "/org/freedesktop/NetworkManager", NetworkManager.class);
			List<Path> deviceList = nm.getDevices();
			if (deviceList != null) {
				this.devices.addAll(deviceList);
			}
		} catch (DBusExecutionException e) {
			if (e instanceof NoReply) {
				this.logger.error("'org.freedesktop.NetworkManager' did not reply within specified time.");
			} else {
				this.logger.warn(String.format(
						"Unexpected '%s' while trying to connect to 'org.freedesktop.NetworkManager'.",
						e.getClass().getName()
				),e);
			}

			// disconnecting from dbus
			this.terminate();		
			throw e;
		}

	}
	
	/**
	 * @see IDbusService#terminate()
	 */
	public void terminate() {
		// close tracker
		if (this.tracker != null) this.tracker.close();
		
		// disconnecting from dbus
		if (this.conn != null) this.conn.disconnect();
	}
	
	private void registerSignalListener(IMWComponent crawler) {
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
	
	private void unregisterSignalListener() {
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
			this.logger.info(String.format("Device %s was deactivated.",signal.getDevice()));
			this.devices.remove(device);
			if (this.devices.size() == 0) {
				this.logger.info("No network device left. Pausing crawler ...");
				this.crawler.pause();
			}
		} else if (signal instanceof NetworkManager.DeviceNowActive) {
			this.logger.info(String.format("Device %s was activated.",signal.getDevice()));
			this.devices.add(device);
			if (this.devices.size() > 0) {
				this.logger.info("Network device found. Resuming crawler ...");
				this.crawler.resume();
			}
		} else if (signal instanceof NetworkManager.DevicesChanged) {
			this.logger.info("Device was changed: " + signal.toString());
			// TODO: what todo here?
		}
	}

	/**
	 * @see ServiceTrackerCustomizer#addingService(ServiceReference)
	 */
	public Object addingService(ServiceReference reference) {
		// a reference to the service
		IMWComponent crawler = (IMWComponent) this.context.getService(reference);			
		
		// new service was installed
		this.registerSignalListener(crawler);
		return crawler;
	}

	/**
	 * @see ServiceTrackerCustomizer#removedService(ServiceReference, Object)
	 */
	public void removedService(ServiceReference reference, Object service) {
		// service was uninstalled
		this.unregisterSignalListener();
	}

	/**
	 * @see ServiceTrackerCustomizer#modifiedService(ServiceReference, Object)
	 */
	public void modifiedService(ServiceReference reference, Object service) {
		// nothing todo here
	}
}
