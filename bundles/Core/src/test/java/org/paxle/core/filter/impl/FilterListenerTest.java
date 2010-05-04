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

package org.paxle.core.filter.impl;

import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.paxle.core.doc.ICommand;
import org.paxle.core.doc.ICommandProfileManager;
import org.paxle.core.filter.FilterQueuePosition;
import org.paxle.core.filter.FilterTarget;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.core.norm.IReferenceNormalizer;


public class FilterListenerTest extends MockObjectTestCase {

	/**
	 * A mock of a {@link BundleContext}
	 */
	private BundleContext bc;
	
	/**
	 * A mock of a {@link FilterManager}
	 */
	private IFilterManagerInternal fm;
	
	/**
	 * The {@link FilterListener} to test
	 */
	private FilterListener fl;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();	
		
		// creating classes required by the filter-listener
		this.bc = mock(BundleContext.class);		
		this.fm = mock(IFilterManagerInternal.class); 		
		final ITempFileManager tempManager = mock(ITempFileManager.class);
		final IReferenceNormalizer refNormalizer = mock (IReferenceNormalizer.class);
		
		checking(new Expectations() {{
			// functions called by the Service-Tracker of the FilterListener. just ignore this
			allowing(bc).createFilter(with(any(String.class)));
			allowing(bc).addServiceListener(with(any(ServiceListener.class)), with(any(String.class)));
			allowing(bc).getServiceReferences(ICommandProfileManager.class.getName(),null); will(returnValue(null));
			
			// functions calles in fl constructor
			allowing(bc).getServiceReferences(null, FilterListener.FILTER); will(returnValue(new ServiceReference[0]));
		}});		
		
		// creating filter-listener
		this.fl = new FilterListener(this.fm, tempManager, refNormalizer, this.bc);
	}

	private Expectations generateExpectations(
			final ServiceReference sRef, 
			final Long serviceID, 
			final String servicePID, 
			final String[] filterTarget, 
			final IFilter<?> filter, 
			final AddFilterAction addFilterAction
	) {
		return new Expectations(){{
			// mandatory service-reference-properties
			allowing(sRef).getProperty(Constants.OBJECTCLASS); 
			will(returnValue(new String[]{IFilter.class.getName()}));
			
			allowing(sRef).getProperty(Constants.SERVICE_ID); 
			will(returnValue(serviceID));
			
			// filter properties
			atLeast(1).of(sRef).getProperty(IFilter.PROP_FILTER_TARGET);
			will(returnValue(filterTarget));
			
			// the filter object
			atLeast(1).of(bc).getService(sRef);
			will(returnValue(filter));
			
			// the filter PID
			atLeast(1).of(sRef).getProperty(Constants.SERVICE_PID);
			will(returnValue(servicePID));
			
			// addFilter must be called
			one(fm).addFilter(with(any(FilterContext.class)));
			will(addFilterAction);
		}};
	}
	
	public void testRegisterFilterViaProperties() {
		final String filterQueueID = "org.paxle.parser.out";
		final int filterQueuePos = 62;
		final Long serviceID = Long.valueOf(System.currentTimeMillis());
		final String servicePID = "FILTER_TEST_" + System.currentTimeMillis();
		
		// the filtering-queues where the filter should be applied to
		final String[] filterTarget = new String[]{
				filterQueueID + ";" + IFilter.PROP_FILTER_TARGET_POSITION + "=" + filterQueuePos
		};
		
		// a dummy filter
		final IFilter<?> filter = mock(IFilter.class);				
		
		final AddFilterAction addFilterAction = new AddFilterAction();
		final ServiceReference sRef = mock(ServiceReference.class);
		checking(this.generateExpectations(sRef, serviceID, servicePID, filterTarget, filter, addFilterAction));
		
		// creating service-registration event
		ServiceEvent event = new ServiceEvent(ServiceEvent.REGISTERED,sRef);
		
		// trigger filter-listener
		this.fl.serviceChanged(event);
		
		// getting the created Filtercontext
		FilterContext c = addFilterAction.fc;
		assertNotNull(c);
		assertEquals(filterQueueID, c.getTargetID());
		assertEquals(filterQueuePos, c.getFilterPosition());
		assertEquals(serviceID, c.getServiceID());
		assertSame(filter, c.getFilter());
		assertTrue(c.isEnabled());
		
		// unregister the filter
		checking(new Expectations() {{
			one(fm).removeFilter(serviceID, filterTarget[0]);
		}});
		ServiceEvent unregisterEvent = new ServiceEvent(ServiceEvent.UNREGISTERING, sRef);
		this.fl.serviceChanged(unregisterEvent);
	}
	
	public void testRegisterFilterViaAnnotations() {
		final String filterQueueID = "org.paxle.parser.out";
		final int filterQueuePos = 62;
		final Long serviceID = Long.valueOf(System.currentTimeMillis());
		final String servicePID = "FILTER_TEST_" + System.currentTimeMillis();		
				
		// a dummy filter class
		@FilterTarget(@FilterQueuePosition(queueId = filterQueueID, position = filterQueuePos))
		class MyTestFilter implements IFilter<ICommand> {
			public void filter(ICommand command, IFilterContext filterContext) {}			
		}
		final IFilter<?> filter = new MyTestFilter();	
		
		
		final AddFilterAction addFilterAction = new AddFilterAction();
		final ServiceReference sRef = mock(ServiceReference.class);
		checking(this.generateExpectations(sRef, serviceID, servicePID, null, filter, addFilterAction));
		
		// creating service-registration event
		ServiceEvent event = new ServiceEvent(ServiceEvent.REGISTERED,sRef);
		
		// trigger filter-listener
		this.fl.serviceChanged(event);
		
		// getting the created Filtercontext
		FilterContext c = addFilterAction.fc;
		assertNotNull(c);
		assertEquals(filterQueueID, c.getTargetID());
		assertEquals(filterQueuePos, c.getFilterPosition());
		assertEquals(serviceID, c.getServiceID());
		assertSame(filter, c.getFilter());
		assertTrue(c.isEnabled());
		
		// unregister the filter
		checking(new Expectations() {{
			one(fm).removeFilter(serviceID);
		}});
		ServiceEvent unregisterEvent = new ServiceEvent(ServiceEvent.UNREGISTERING, sRef);
		this.fl.serviceChanged(unregisterEvent);
	}
	
	/**
	 * JMock helper-action to access the {@link FilterContext} generated by the {@link FilterListener}
	 */
	private static class AddFilterAction implements Action {
		public FilterContext fc;
		public void describeTo(Description arg0) { }

		public Object invoke(Invocation invocation) throws Throwable {
			this.fc = (FilterContext)invocation.getParameter(0);
			return null;
		}		
	}
}
