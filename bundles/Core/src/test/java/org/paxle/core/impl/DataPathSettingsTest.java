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
		System.setProperty(DataPathSettings.PROP_PAXLE_DATAPATH, "${user.home}/paxle");
		DataPathSettings.formatDataPath();
		
		String dataPath = System.getProperty(DataPathSettings.PROP_PAXLE_DATAPATH);
		assertNotNull(dataPath);
		assertEquals(System.getProperty("user.home")+"/paxle", dataPath);
		System.out.println("New datapath is: " + dataPath);
	}
	
	public void testConvertUserNamePath() {
		System.setProperty(DataPathSettings.PROP_PAXLE_DATAPATH, "/var/lib/paxle/${user.name}");
		DataPathSettings.formatDataPath();
		
		String dataPath = System.getProperty(DataPathSettings.PROP_PAXLE_DATAPATH);
		assertNotNull(dataPath);
		assertEquals("/var/lib/paxle/" + System.getProperty("user.name"), dataPath);
		System.out.println("New datapath is: " + dataPath);
	}
	
	public void testConvertMultiPropsPath() {
		System.setProperty("paxle.test1", "abc");
		System.setProperty("paxle.test2", "xyz");
		System.setProperty(DataPathSettings.PROP_PAXLE_DATAPATH, "/test/${paxle.test1}/${paxle.test2}");
		DataPathSettings.formatDataPath();
		
		String dataPath = System.getProperty(DataPathSettings.PROP_PAXLE_DATAPATH);
		assertNotNull(dataPath);
		assertEquals("/test/abc/xyz", dataPath);
		System.out.println("New datapath is: " + dataPath);
	}
	
	public void testConvertRelativePath() throws IOException {
		String relPath = "./data";
		System.setProperty(DataPathSettings.PROP_PAXLE_DATAPATH, relPath);
		DataPathSettings.formatDataPath();
		
		String dataPath = System.getProperty(DataPathSettings.PROP_PAXLE_DATAPATH);
		assertNotNull(dataPath);
		assertEquals(new File("").getCanonicalPath().toString() + "/data",dataPath);
		System.out.println("New datapath is: " + dataPath);
	}
	
	public void testConvertAbsolutePath() {
		String absPath = "/home/user/paxle/data";
		System.setProperty(DataPathSettings.PROP_PAXLE_DATAPATH, absPath);
		DataPathSettings.formatDataPath();
		
		String dataPath = System.getProperty(DataPathSettings.PROP_PAXLE_DATAPATH);
		assertNotNull(dataPath);
		assertEquals(absPath, dataPath);
		System.out.println("New datapath is: " + dataPath);
	}
}
