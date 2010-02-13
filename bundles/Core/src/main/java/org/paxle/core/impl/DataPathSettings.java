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

package org.paxle.core.impl;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * For now this class is just used to convert a relative paxle-data-path
 * into an absolute path and to replace variables contained in the path.
 * 
 * TODO: Later this class should work as CM-config interceptor and should
 * replace CM-properties containing paxle-data-path placeholders with the
 * specific path.
 */
public class DataPathSettings {
	static final String PROP_PAXLE_DATAPATH = "paxle.data";
	
	/**
	 * TODO: Currently the default data-path is the paxle-directory.
	 * We'll change this in a next step.
	 */
	static final String VAL_PAXLE_DATAPATH_DEFAULT = "."; 
	
	public static void validateDataPathSettings() {
		try {
			final File dataPathDir = formatDataPath();
			
			// creating data-dir if necessary 
			if (!dataPathDir.exists()) {
				boolean success = dataPathDir.mkdirs();
				if (!success) throw new RuntimeException("Unable to create data-directory " + dataPathDir);
			}
			
			// testing if data-dir is read- and writable
			if (!dataPathDir.canRead()) {
				throw new RuntimeException("Paxle data-directory not readable: " + dataPathDir);
			} else if (!dataPathDir.canWrite()) {
				throw new RuntimeException("Paxle data-directory not writable: " + dataPathDir);
			}
		} catch (Throwable e) {
			if (e instanceof ExceptionInInitializerError) throw (ExceptionInInitializerError) e;
			throw new ExceptionInInitializerError(e);
		}
	}
	
	static File formatDataPath() {
		try {
			// getting path specified by the user
			String dataPath = System.getProperty(PROP_PAXLE_DATAPATH, VAL_PAXLE_DATAPATH_DEFAULT);
			
			// converting path, replacing placeholders
			if (dataPath.equals(VAL_PAXLE_DATAPATH_DEFAULT)) {
				dataPath = new File(VAL_PAXLE_DATAPATH_DEFAULT).getCanonicalPath().toString();
			} else {			
				StringBuffer buf = new StringBuffer();
				Pattern pattern = Pattern.compile("\\$\\{[^\\}]*}");
				Matcher matcher = pattern.matcher(dataPath);

				while (matcher.find()) {
					String placeHolder = matcher.group();
					String propName = placeHolder.substring(2,placeHolder.length()-1);					
					String propValue = System.getProperty(propName, placeHolder);
					if (propValue != null) propValue = propValue.replaceAll("\\\\", "\\\\\\\\");
					matcher.appendReplacement(buf, propValue);
				}
				matcher.appendTail(buf);
				
				dataPath = buf.toString();
			}
			
			// converting rel. path to absolute path
			File dataPathDir = new File(dataPath);
			if (!dataPathDir.isAbsolute()) {
				dataPath = dataPathDir.getCanonicalPath().toString();
			}			
		
			// set converted path to sysprops
			System.setProperty(PROP_PAXLE_DATAPATH, dataPath);
			return dataPathDir;
		} catch (Throwable e) {
			throw new ExceptionInInitializerError(e);
		}
	}
}
