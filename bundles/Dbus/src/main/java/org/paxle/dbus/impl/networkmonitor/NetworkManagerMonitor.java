/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package org.paxle.dbus.impl.networkmonitor;

import java.util.ArrayList;
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
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.component.ComponentContext;
import org.paxle.core.IMWComponent;

/**
 * A class to receive signals from the {@link NetworkManager}
 * 
 * @scr.component immediate="true"
 */
public class NetworkManagerMonitor implements DBusSigHandler<DeviceSignal> {
	private static final String BUSNAME = "org.freedesktop.NetworkManager";
	private static final String OBJECTPATH = "/org/freedesktop/NetworkManager";
	
	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * Reference to the crawler component
	 * @scr.reference target="(component.ID=org.paxle.crawler)"
	 */
	protected IMWComponent<?> crawler;
	
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
	@SuppressWarnings({"unchecked","serial"})
	private static ArrayList<Class> signals = new ArrayList<Class>(){{
			add(NetworkManager.DeviceNoLongerActive.class);
			add(NetworkManager.DeviceNowActive.class);
			add(NetworkManager.DevicesChanged.class);
	}};
	
	protected void activate(ComponentContext context) throws DBusException, InvalidSyntaxException {
		try {
			// connecting to dbus
			this.logger.info("Connecting to dbus ...");
			this.conn = DBusConnection.getConnection(DBusConnection.SYSTEM);

			// getting the network-manager via dbus
			this.logger.info(String.format("Getting reference to %s ...", BUSNAME));
			NetworkManager nm = conn.getRemoteObject(BUSNAME, OBJECTPATH, NetworkManager.class);
			List<Path> deviceList = nm.getDevices();
			if (deviceList != null) {
				this.logger.debug(String.format("%d device(s) detected: %s", Integer.valueOf(deviceList.size()), deviceList.toString()));
				this.devices.addAll(deviceList);
			}
			
			this.registerSignalListener();
		} catch (DBusExecutionException e) {
			if (e instanceof NoReply) {
				this.logger.error(String.format("'%s' did not reply within specified time.",BUSNAME));
			} else {
				this.logger.warn(String.format(
					"Unexpected '%s' while trying to connect to '%s'.",
					e.getClass().getName(),
					BUSNAME
				),e);
			}

			// disabling this component
			context.disableComponent(this.getClass().getName());
			// XXX: should we re-throw the exceptoin here? 
			// throw e;
		}

	}
	
	protected void deactivate(ComponentContext context) {
		// unregistering signal listener
		this.unregisterSignalListener();
			
		// disconnecting from dbus
		this.conn.disconnect();
	}
	
	private void registerSignalListener() {
		try {
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
			this.logger.info("Unregister network-manager signal listeners ...");
			for (Class<DeviceSignal> signal : signals) {
				this.conn.removeSigHandler(signal,this);
			}
		} catch (DBusException e) {
			this.logger.error("Unable to unregister as dbus-signal-listener",e);
		}
	}
	
	/**
	 * @see DBusSigHandler#handle(DBusSignal)
	 */
	public void handle(DeviceSignal signal) {
		final Path device = signal.getDevice();
		
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
}
