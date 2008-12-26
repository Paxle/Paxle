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
package org.paxle.core.monitorable.observer.impl;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.osgi.framework.internal.core.FilterImpl;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceListener;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.monitor.MonitorAdmin;
import org.osgi.service.monitor.Monitorable;
import org.paxle.core.monitorable.observer.impl.MonitorableObserver;

public class MonitorableObserverTest extends MockObjectTestCase {
	private BundleContext bc;
	
	private EventAdmin eventAdmin;
	
	private MonitorAdmin monitorAdmin;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// mocking an OSGi bundle context
		bc = mock(BundleContext.class);
		checking(new Expectations(){{
			// creating the ldap styled filter
			allowing(bc).createFilter(with(any(String.class)));
			will(new Action(){
				public void describeTo(Description arg0) {}
				public Object invoke(Invocation invocation) throws Throwable {					
					return new FilterImpl((String)invocation.getParameter(0));
				}				
			});
			
			allowing(bc).addServiceListener(with(any(ServiceListener.class)), with(any(String.class)));
			allowing(bc).registerService(with(any(String.class)), with(anything()), with(any(Dictionary.class)));
			allowing(bc).getServiceReferences(Monitorable.class.getName(), null); will(returnValue(null));
		}});
		
		// mocking event- and monitor-admin services
		this.eventAdmin = mock(EventAdmin.class);
		this.monitorAdmin = mock(MonitorAdmin.class);
	}
	
	public void testExtractMonitorables() {
		// extracting the list of used monitorable-variables from the filter-expression
		Map<String,Set<String>> variables = MonitorableObserver.extractMonitorables(
				"(|(java.lang.runtime/memory.free <= 10240)(os.disk/disk.space.free<=256))"
		);
		
		assertNotNull(variables);
		assertEquals(2, variables.size());
		
		assertTrue(variables.containsKey("java.lang.runtime"));
		assertTrue(variables.get("java.lang.runtime").contains("memory.free"));
		
		assertTrue(variables.containsKey("os.disk"));
		assertTrue(variables.get("os.disk").contains("disk.space.free"));
	}
	
	public void testFilter() throws InvalidSyntaxException {
		Filter f = new FilterImpl("(|(java.lang.runtime/memory.free <= 10240)(os.disk/disk.space.free<=256))");
		
		Dictionary<String, Object> values = new Hashtable<String, Object>();
		assertFalse(f.match(values));
		
		values.put("java.lang.runtime/memory.free", new Integer(20000));
		assertFalse(f.match(values));
		
		values.put("java.lang.runtime/memory.free", new Integer(10000));
		assertTrue(f.match(values));
		
		f = new FilterImpl("(os.usage.cpu/cpu.usage.total >= 0.8)");
		values = new Hashtable<String, Object>();
		values.put("os.usage.cpu/cpu.usage.total", new Float(0.7));
		assertFalse(f.match(values));
		
		values.put("os.usage.cpu/cpu.usage.total", new Float(0.8));
		assertTrue(f.match(values));
	}
	
	public void testMonitorableObserver() throws InvalidSyntaxException {
		// creating an observer
		MonitorableObserver o = new MonitorableObserver(
				this.bc,
				this.eventAdmin,
				this.monitorAdmin,
				"(|(java.lang.runtime/memory.free <= 10240)(os.disk/disk.space.free<=256))",
				null
		);
		
		System.out.println("finished");
	}
}
