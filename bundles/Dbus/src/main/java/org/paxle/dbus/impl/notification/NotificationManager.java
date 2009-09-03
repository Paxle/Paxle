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
package org.paxle.dbus.impl.notification;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.freedesktop.Notifications;
import org.freedesktop.DBus.Error.NoReply;
import org.freedesktop.Notifications.ServerInfo;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.UInt32;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.component.ComponentContext;


@Component
public class NotificationManager {
	private static final String BUSNAME = "org.freedesktop.Notifications";
	private static final String OBJECTPATH = "/org/freedesktop/Notifications";
	
	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * The connection to the dbus
	 */
	private DBusConnection conn = null; 
	
	protected void activate(ComponentContext context) throws DBusException, InvalidSyntaxException, IllegalArgumentException, IOException {
		try {
			// connecting to dbus
			this.logger.info("Connecting to dbus ...");
			this.conn = DBusConnection.getConnection(DBusConnection.SESSION);
			
			// getting the network-manager via dbus
			this.logger.info(String.format("Getting reference to %s ...", BUSNAME));
			final Notifications n = conn.getRemoteObject(BUSNAME, OBJECTPATH, Notifications.class);
			
			ServerInfo<String, String, String, String> info = n.GetServerInformation();
			List<String> capabilities = n.GetCapabilities();
			
			HashMap<String, Variant> hints = new HashMap<String, Variant>();
			hints.put("urgency", new Variant<Byte>(new Byte((byte) 0)));
			hints.put("category", new Variant<String>(""));
			hints.put("icon_data", new Variant<byte[]>(this.getIconData()));
			hints.put("image_data", new Variant<byte[]>(this.getIconData()));
			
			UInt32 id = n.Notify("HelloWorld",new UInt32(0),"","Info","Paxle started.",new String[0],hints,new Integer(-1));
			System.out.println(id);
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
		// disconnecting from dbus
		this.conn.disconnect();
	}	
	
	private byte[] getIconData() throws IOException {
		InputStream in = this.getClass().getResourceAsStream("/resources/icon.ico");		
		
		// loading date
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		IOUtils.copy(in, bout);
		bout.close();
		in.close();
		
		// trying to detect the mimetype of the image
		return bout.toByteArray();
	}
}
