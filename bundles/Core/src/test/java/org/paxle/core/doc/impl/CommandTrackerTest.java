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
package org.paxle.core.doc.impl;

import java.net.URI;
import java.util.Random;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.paxle.core.doc.CommandEvent;
import org.paxle.core.doc.ICommand;
import org.paxle.core.doc.impl.CommandTracker;

public class CommandTrackerTest extends MockObjectTestCase {
	private EventAdmin eAdmin;
	private CommandTracker tracker;
	private Random rand;
	
	protected void setUp() throws Exception {
		super.setUp();
		this.rand = new Random();
		this.eAdmin = mock(EventAdmin.class);
		this.tracker = new CommandTracker(){{
			this.eventService = eAdmin;
			this.activate(null);
		}};
	}
	
	@Override
	protected void tearDown() throws Exception {	
		this.tracker.deactivate();
		super.tearDown();
	}
	
	public void testCommandCreated() {
		String componentID = "component" + System.currentTimeMillis();
		
		// create a dummy command
		final int commandOID = Math.abs(this.rand.nextInt());
		final URI commandURI = URI.create("http://xxx.yyy");
		
		final ICommand command = new BasicCommand();
		command.setLocation(commandURI);
		command.setOID(commandOID);
		
		final EventInterceptor interceptor = new EventInterceptor();
		checking(new Expectations() {{
			// the tracker must fire an event with topic CommandEvent.TOPIC_CREATED
			one(eAdmin).sendEvent(with(new EventMatcher(CommandEvent.TOPIC_CREATED)));
			will(interceptor); // intercept the event so that we can inspect it
		}});
		
		this.tracker.commandCreated(componentID, command);
		
		// check if we can fetch the command by ID and location
		assertSame(command, this.tracker.getCommandByID(Long.valueOf(commandOID)));
		assertSame(command, this.tracker.getCommandByLocation(commandURI));
		assertEquals(1, this.tracker.getTrackingSize());
		
		// check if the event was sent properly
		assertEquals(Long.valueOf(commandOID), interceptor.event.getProperty(CommandEvent.PROP_COMMAND_ID));
		assertEquals(commandURI.toASCIIString(), interceptor.event.getProperty(CommandEvent.PROP_COMMAND_LOCATION));
		assertEquals(componentID, interceptor.event.getProperty(CommandEvent.PROP_COMPONENT_ID));
	}
	
	public void testCommandCreatedWithNoOID() {
		String componentID = "component" + System.currentTimeMillis();
		
		// create a dummy command but without configuring the OID
		final int commandOID = Math.abs(this.rand.nextInt());
		final URI commandURI = URI.create("http://xxx.yyy");
		
		final ICommand command = new BasicCommand();
		command.setLocation(commandURI);
		
		final EventInterceptor interceptor = new EventInterceptor();
		checking(new Expectations() {{
			// the tracker must fire an event with topic CommandEvent.TOPIC_OID_REQUIRED
			one(eAdmin).sendEvent(with(new EventMatcher(CommandEvent.TOPIC_OID_REQUIRED)));
			
			/* 
			 * We intercept the event to set the OID properly
			 * This task is normally done by the ORM tool.
			 */
			will(new Action() {
				public void describeTo(Description arg0) {}

				public Object invoke(Invocation invocation) throws Throwable {
					// extract the command location from the event
					String location = (String) ((Event) invocation.getParameter(0)).getProperty(CommandEvent.PROP_COMMAND_LOCATION);
					
					// fetch the command via the command-tracker
					ICommand cmd = tracker.getCommandByLocation(URI.create(location));
					
					// configure a proper OID
					cmd.setOID(commandOID);					
					return null;
				}				
			});
			
			// the tracker must fire an event with topic CommandEvent.TOPIC_CREATED
			one(eAdmin).sendEvent(with(new EventMatcher(CommandEvent.TOPIC_CREATED)));
			will(interceptor);
		}});
		
		this.tracker.commandCreated(componentID, command);
		
		// check if the OID was set properly
		assertEquals(commandOID, command.getOID());
		
		// check if we can fetch the command by ID and location
		assertSame(command, this.tracker.getCommandByID(Long.valueOf(commandOID)));
		assertSame(command, this.tracker.getCommandByLocation(commandURI));
		assertEquals(1, this.tracker.getTrackingSize());
		
		// check if the event was sent properly
		assertEquals(Long.valueOf(commandOID), interceptor.event.getProperty(CommandEvent.PROP_COMMAND_ID));
		assertEquals(commandURI.toASCIIString(), interceptor.event.getProperty(CommandEvent.PROP_COMMAND_LOCATION));
		assertEquals(componentID, interceptor.event.getProperty(CommandEvent.PROP_COMPONENT_ID));
	}
	
	public void testCommandDestroyed() {
		String componentID = "component" + System.currentTimeMillis();
		
		// create a dummy command but without configuring the OID
		final int commandOID = Math.abs(this.rand.nextInt());
		final URI commandURI = URI.create("http://xxx.yyy");		
		final ICommand command = new BasicCommand();
		command.setLocation(commandURI);
		command.setOID(commandOID);
		
		final EventInterceptor interceptor = new EventInterceptor();
		checking(new Expectations() {{
			// the tracker must fire an event with topic CommandEvent.TOPIC_CREATED
			one(eAdmin).sendEvent(with(new EventMatcher(CommandEvent.TOPIC_CREATED)));
			
			// the tracker must fire an event with topic CommandEvent.TOPIC_CREATED
			one(eAdmin).sendEvent(with(new EventMatcher(CommandEvent.TOPIC_DESTROYED)));
			will(interceptor);
			will(new Action(){
				public void describeTo(Description arg0) {}

				public Object invoke(Invocation invocation) throws Throwable {
					// pass the event to the tracker
					tracker.handleEvent((Event) invocation.getParameter(0));
					return null;
				}
				
			});
		}});
		
		// signal cmd creation
		this.tracker.commandCreated(componentID, command);
		assertEquals(1, this.tracker.getTrackingSize());
		assertSame(command, this.tracker.getCommandByID(Long.valueOf(commandOID)));
		
		// signal cmd destruction
		this.tracker.commandDestroyed(componentID, command);
		
		// check if the command is in the destroyed list now
		assertTrue(this.tracker.isInDestroyedList(command));
		
		// check if we can still fetch the command by ID and location
		assertSame(command, this.tracker.getCommandByID(Long.valueOf(commandOID)));
		assertSame(command, this.tracker.getCommandByLocation(commandURI));
		assertEquals(1, this.tracker.getTrackingSize());	
	}
}

/**
 * A custom jMock-action to intercept an {@link Event} so that it can be inspected
 * by the test-code afterwards.
 */
class EventInterceptor implements Action {
	public Event event;
	
	public void describeTo(Description arg0) { }

	public Object invoke(Invocation invocation) throws Throwable {
		this.event = (Event) invocation.getParameter(0);
		return null;
	}	
}

/**
 * A custom jmock {@link Matcher} to test if a fired {@link Event}
 * has an expected {@link Event#getTopic() topic}
 */
class EventMatcher extends TypeSafeMatcher<Event> {
	private final String topic;
	
	public EventMatcher(String expectedTopic) {
		this.topic = expectedTopic;
	}
	
	@Override
	public boolean matchesSafely(Event event) {
		return event.getTopic().equals(this.topic);
	}

	public void describeTo(Description description) {
		description.appendText("am event starting topic ").appendValue(this.topic);		
	}
	
}
