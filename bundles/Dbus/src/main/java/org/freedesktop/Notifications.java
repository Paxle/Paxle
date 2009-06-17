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
package org.freedesktop;

import java.util.List;
import java.util.Map;

import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.Position;
import org.freedesktop.dbus.Tuple;
import org.freedesktop.dbus.UInt32;
import org.freedesktop.dbus.Variant;

/**
 * 
 * @see <a href="http://www.galago-project.org/specs/notification/0.9/x408.html">Desktop Notifications Specification - D-BUS Protocol</a>
 *
 */
public interface Notifications extends DBusInterface {

	/**
	 * This message returns the information on the server. Specifically, the server name, vendor, and version number. 
	 */
	public ServerInfo<String,String,String,String> GetServerInformation();
	
	public List<String> GetCapabilities();
	
	public UInt32 Notify(String appName, UInt32 id, String icon, String summary, String body, String[] actions, Map<String,Variant> hints, Integer timeout); 
	
	public class ServerInfo<NAME,VENDOR,VER,SPEC> extends Tuple {
		/**
		 * The product name of the server.
		 */
        @Position(0)
        public final NAME name;
        
        /**
         * The vendor name. For example, "KDE," "GNOME," "freedesktop.org," or "Microsoft."
         */
        @Position(1)
        public final VENDOR vendor;
        
        /**
         * The server's version number.
         */
        @Position(2)
        public final VER version;
        
        /**
         * The specification version number
         */
        @Position(3)
        public final SPEC specVersion;
        
        public ServerInfo(NAME name, VENDOR vendor, VER version, SPEC specVersion) {
           this.name = name;
           this.vendor = vendor;
           this.version = version;
           this.specVersion = specVersion;
        }
	}
}
