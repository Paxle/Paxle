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

package org.paxle.core.queue;

import java.net.URI;
import java.util.Properties;
import java.util.Random;

import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.paxle.core.queue.ICommand.Result;

public class CommandEventTest extends MockObjectTestCase {
	public void testCreateEvent() {
		Random rand = new Random();
		
		final Properties props = new Properties();		
		final String componentID = "component" + System.currentTimeMillis();
		final String topic = "test";
		final int commandOID = rand.nextInt();
		final int commandProfileID = rand.nextInt();
		final URI commandLocation = URI.create("http://irgendwas.de/" + rand.nextInt());
		final Result commandResult = Result.Failure;
		final String commandResultText = "Processing failed";
		
		final ICommand command = mock(ICommand.class);
		checking(new Expectations(){{
			one(command).getOID(); will(returnValue(commandOID));
			one(command).getProfileOID(); will(returnValue(commandProfileID));
			one(command).getLocation(); will(returnValue(commandLocation));
			one(command).getResult(); will(returnValue(commandResult));
			one(command).getResultText(); will(returnValue(commandResultText));
		}});
		
		// create an event
		Event event = CommandEvent.createEvent(componentID, topic, command, props);
		assertNotNull(event);
		
		assertTrue(props.containsKey(EventConstants.TIMESTAMP));
		assertEquals(topic, event.getTopic());		
		assertEquals(componentID, event.getProperty(CommandEvent.PROP_COMPONENT_ID));
		assertEquals(Long.valueOf(commandOID), event.getProperty(CommandEvent.PROP_COMMAND_ID));
		assertEquals(Long.valueOf(commandProfileID), event.getProperty(CommandEvent.PROP_PROFILE_ID));
		assertEquals(commandLocation.toASCIIString(), event.getProperty(CommandEvent.PROP_COMMAND_LOCATION));
		assertEquals(commandResult.toString(), event.getProperty(CommandEvent.PROP_COMMAND_RESULT));
		assertEquals(commandResultText, event.getProperty(CommandEvent.PROP_COMMAND_RESULT_TEXT));
	}
}
