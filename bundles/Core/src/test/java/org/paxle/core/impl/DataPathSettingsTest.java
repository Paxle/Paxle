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
import java.io.IOException;

import junit.framework.TestCase;

public class DataPathSettingsTest extends TestCase {
	public void testConvertDefaultPath() throws IOException {
		assertNull(System.getProperty(DataPathSettings.PROP_PAXLE_DATAPATH));
		DataPathSettings.formatDataPath();
		
		String dataPath = System.getProperty(DataPathSettings.PROP_PAXLE_DATAPATH);
		assertNotNull(dataPath);
		assertEquals(new File(DataPathSettings.VAL_PAXLE_DATAPATH_DEFAULT).getCanonicalPath().toString(), dataPath);
		System.out.println("New datapath is: " + dataPath);
	}
	
	public void testConvertUserHomePath() {
		System.setProperty(DataPathSettings.PROP_PAXLE_DATAPATH, "${user.home}" + File.separatorChar + "paxle");
		DataPathSettings.formatDataPath();
		
		String dataPath = System.getProperty(DataPathSettings.PROP_PAXLE_DATAPATH);
		assertNotNull(dataPath);
		assertEquals(System.getProperty("user.home") + File.separatorChar + "paxle", dataPath);
		System.out.println("New datapath is: " + dataPath);
	}
	
	public void testConvertUserNamePath() throws IOException {
		// Path containing user-name, e.g.: /var/lib/paxle/theli
		System.setProperty(DataPathSettings.PROP_PAXLE_DATAPATH, 
				File.listRoots()[0].getCanonicalPath() + "var" + 
				File.separatorChar + "lib" + 
				File.separatorChar + "paxle" + 
				File.separatorChar + "${user.name}"
		);
		DataPathSettings.formatDataPath();
		
		String dataPath = System.getProperty(DataPathSettings.PROP_PAXLE_DATAPATH);
		assertNotNull(dataPath);
		assertEquals(
				File.listRoots()[0].getCanonicalPath() + "var" + 
				File.separatorChar + "lib" + 
				File.separatorChar + "paxle" + 
				File.separatorChar + System.getProperty("user.name"), dataPath);
		System.out.println("New datapath is: " + dataPath);
	}
	
	public void testConvertMultiPropsPath() throws IOException {
		System.setProperty("paxle.test1", "abc");
		System.setProperty("paxle.test2", "xyz");
		
		// Path containing properties, e.g.: /test/${paxle.test1}/{paxle.test2}
		System.setProperty(DataPathSettings.PROP_PAXLE_DATAPATH, 
				File.listRoots()[0].getCanonicalPath() + "test" + 
				File.separatorChar + "${paxle.test1}" + 
				File.separatorChar + "${paxle.test2}"
		);
		DataPathSettings.formatDataPath();
		
		String dataPath = System.getProperty(DataPathSettings.PROP_PAXLE_DATAPATH);
		assertNotNull(dataPath);
		assertEquals(File.listRoots()[0].getCanonicalPath() + "test" + File.separatorChar + "abc" + File.separatorChar + "xyz", dataPath);
		System.out.println("New datapath is: " + dataPath);
	}
	
	public void testConvertRelativePath() throws IOException {
		// relative path, e.g.: ./data
		String relPath = "." + File.separatorChar + "data";
		System.setProperty(DataPathSettings.PROP_PAXLE_DATAPATH, relPath);
		DataPathSettings.formatDataPath();
		
		String dataPath = System.getProperty(DataPathSettings.PROP_PAXLE_DATAPATH);
		assertNotNull(dataPath);
		assertEquals(new File("").getCanonicalPath().toString()  + File.separatorChar +  "data",dataPath);
		System.out.println("New datapath is: " + dataPath);
	}
	
	public void testConvertAbsolutePath() throws IOException {
		// Abs.Path, e.g.: /home/user/paxle/data
		String absPath = 
			File.listRoots()[0].getCanonicalPath() + "home" + 
			File.separatorChar + "user" + 
			File.separatorChar + "paxle" + 
			File.separatorChar + "data";
		System.setProperty(DataPathSettings.PROP_PAXLE_DATAPATH, absPath);
		DataPathSettings.formatDataPath();
		
		String dataPath = System.getProperty(DataPathSettings.PROP_PAXLE_DATAPATH);
		assertNotNull(dataPath);
		assertEquals(absPath, dataPath);
		System.out.println("New datapath is: " + dataPath);
	}
}
