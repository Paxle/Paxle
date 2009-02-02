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
package org.paxle.desktop.backend.impl.jdic;

import org.jdesktop.jdic.desktop.internal.DesktopServiceManagerInit;
import org.jdesktop.jdic.init.JdicManager;
import org.jdesktop.jdic.tray.internal.TrayServiceManagerInit;

public class JdicInit {
	public static void init() {
		try {
			ClassLoader cl = TrayServiceManagerInit.class.getClassLoader();
			String plattformSuffix = JdicManager.getPlatformSuffix();
			
			// loading libs
			JdicManager.loadLibrary("jdic");
			JdicManager.loadLibrary("tray");			
			
			// init desktop service manager
			DesktopServiceManagerInit.initServiceManager(cl, plattformSuffix);
			
			// init tray service manager
			TrayServiceManagerInit.initServiceManager(cl, plattformSuffix);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}				
	}
}
