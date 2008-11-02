/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 * 
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.data.db.impl;

import java.util.Properties;

import org.hibernate.cfg.Configuration;

public class ConnectionUrlTool {
	
	public static void postProcessProperties(Configuration configuration) {
		// getting db-config-properties
		Properties configProps = configuration.getProperties();
		
		String connectionURL = configProps.getProperty("connection.url");
		if (connectionURL != null) {
			configuration.setProperty("connection.url", processProperty(connectionURL));
		}
		
		connectionURL = configProps.getProperty("hibernate.connection.url");
		if (connectionURL != null) {
			configuration.setProperty("hibernate.connection.url", processProperty(connectionURL));
		}
	}	
	
	public static String processProperty(String property) {
		// the paxle data path
		String paxleDataPath = System.getProperty("paxle.data");
		
		// file seperator
		String pathSep = System.getProperty("file.separator");
		
		// replace properties
		if (property.contains("${paxle.data}"))
			property = property.replace("${paxle.data}", paxleDataPath);
		if (property.contains("${file.separator}"))
			property = property.replace("${file.separator}", pathSep);
		
		return property;
	}
}
