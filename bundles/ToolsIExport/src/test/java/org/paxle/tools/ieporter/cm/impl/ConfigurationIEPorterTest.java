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
package org.paxle.tools.ieporter.cm.impl;
import java.io.File;
import java.lang.reflect.Array;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.jxpath.JXPathContext;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.paxle.tools.ieporter.cm.impl.ConfigurationIEPorter;
import org.w3c.dom.Document;


public class ConfigurationIEPorterTest extends MockObjectTestCase {
	
	private BundleContext bc;
	private ServiceReference cmRef;
	private ConfigurationAdmin cmService;
	
	private ConfigurationIEPorter ieporter;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// mock the configuration-admin service
		this.cmService = mock(ConfigurationAdmin.class);
		this.cmRef = mock(ServiceReference.class);
		
		// mock bundle context
		this.bc = mock(BundleContext.class);
		checking(new Expectations(){{
			// allow to fetch the cm-service reference
			allowing(bc).getServiceReference(ConfigurationAdmin.class.getName());
			will(returnValue(cmRef));
			
			// allowing to fetch the cm-service
			allowing(bc).getService(cmRef);
			will(returnValue(cmService));
		}});
		
		this.ieporter = new ConfigurationIEPorter(this.bc);
	}
	
	public void testExportNullConfiguration() throws ParserConfigurationException {
		Map<String, Document> configs = this.ieporter.exportConfiguration(null);
		assertNotNull(configs);
		assertEquals(0, configs.size());
	}
	
	public void testExportNullConfigurationPid() throws ParserConfigurationException {
		final Configuration config = mock(Configuration.class);
		checking(new Expectations(){{
			one(config).getPid(); will(returnValue(null));
			never(config);
		}});
		
		Map<String, Document> configs = this.ieporter.exportConfiguration(config);
		assertNotNull(configs);
		assertEquals(0, configs.size());
	}
	
	public void testExportNullConfigurationProperties() throws ParserConfigurationException {
		final Configuration config = mock(Configuration.class);
		checking(new Expectations(){{
			one(config).getPid(); will(returnValue("test"));
			one(config).getProperties(); will(returnValue(null));
			never(config);
		}});
		
		Map<String, Document> configs = this.ieporter.exportConfiguration(config);
		assertNotNull(configs);
		assertEquals(0, configs.size());
	}
	
	public void testExportConfiguration() throws ParserConfigurationException {
		final Dictionary<String, Object> props = new Hashtable<String, Object>();
		props.put("myProperty.Integer", new Integer(1));
		props.put("myProperty.intArray", new int[]{1,2,3,4});
		props.put("myProperty.String", "test");
		props.put("myProperty.StringArray", new String[]{"test1","test2","test3"});
		
		final Configuration config = mock(Configuration.class);
		checking(new Expectations(){{
			atLeast(1).of(config).getPid(); will(returnValue("testPid"));
			atLeast(1).of(config).getProperties(); will(returnValue(props));
			never(config);
		}});
		
		Map<String, Document> configs = this.ieporter.exportConfiguration(config);
		assertNotNull(configs);
		assertEquals(1, configs.size());
		assertTrue(configs.containsKey("testPid"));		
		
		Document doc = configs.get("testPid");
		assertNotNull(doc);
		JXPathContext objContext = JXPathContext.newContext(doc);
		assertEquals("testPid", objContext.getValue("//service.pid"));
		assertEquals(props.size(), ((Double)objContext.getValue("count(//property)")).intValue());
		assertEquals(props.get("myProperty.Integer").toString(),objContext.getValue("//property[@key='myProperty.Integer']/value"));
		assertEquals(Array.getLength(props.get("myProperty.intArray")),((Double)objContext.getValue("count(//property[@key='myProperty.intArray']/values/value)")).intValue());
		assertEquals(props.get("myProperty.String").toString(),objContext.getValue("//property[@key='myProperty.String']/value"));
		assertEquals(Array.getLength(props.get("myProperty.StringArray")),((Double)objContext.getValue("count(//property[@key='myProperty.StringArray']/values/value)")).intValue());
	}
	
	public void testImportConfiguration() throws Exception {
		File testFile = new File("src/test/resources/config01.zip");
		Map<String, Dictionary<String, Object>> configs = this.ieporter.importConfigurations(testFile);
		assertNotNull(configs);
		assertEquals(10, configs.size());
	}
}
