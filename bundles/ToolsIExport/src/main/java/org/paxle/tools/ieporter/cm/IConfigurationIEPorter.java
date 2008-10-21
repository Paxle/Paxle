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

package org.paxle.tools.ieporter.cm;

import java.io.File;
import java.util.Dictionary;
import java.util.Map;
import java.util.zip.ZipFile;

import org.osgi.framework.Bundle;

public interface IConfigurationIEPorter {
	public Map<String, Dictionary<String, Object>> importConfigurations(File xmlFile) throws Exception;
	
	/**
	 * @param a map containing the {@link org.osgi.framework.Constants#SERVICE_PID pid} of all registered {@link org.osgi.service.cm.ManagedService services} 
	 * 	 	  whose configuration should be exported as {@link Map.Entry#getKey() key}. 
	 * 		  The {@link Bundle#getLocation()} must be passed in as {@link Map.Entry#getValue() value}
	 * @return a {@link ZipFile} containing an XML-file for each {@link org.osgi.service.cm.ManagedService service} 
	 * @throws Exception
	 */
	public File exportConfigsAsZip(Map<String, String> pidBundleLocationTupel) throws Exception;
}
