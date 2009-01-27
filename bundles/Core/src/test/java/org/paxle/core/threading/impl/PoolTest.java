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

package org.paxle.core.threading.impl;

import org.apache.commons.pool.PoolableObjectFactory;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.paxle.core.threading.IPool;
import org.paxle.core.threading.IWorker;

public class PoolTest extends MockObjectTestCase {

	private PoolableObjectFactory objFactoryMock = null;
	private IPool<Object> mockedPool = null;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		this.objFactoryMock = mock(PoolableObjectFactory.class);
		this.mockedPool = new Pool<Object>(this.objFactoryMock);
	}
		
	public void testGetWorker() throws Exception {		
		checking(new Expectations(){{
			@SuppressWarnings("unchecked")
			final IWorker<Object> worker = mock(IWorker.class);
			
			one(objFactoryMock).makeObject();
			will(returnValue(worker));
			
			one(objFactoryMock).activateObject(worker);
		}});
		
		IWorker<Object> worker = this.mockedPool.getWorker();
		assertNotNull(worker);
		assertEquals(1, this.mockedPool.getActiveJobCount());
	}
	
	public void testGetWorkerExceptionOnMakeObject() throws Exception {
		checking(new Expectations(){{
			one(objFactoryMock).makeObject();
			will(throwException(new RuntimeException("testGetWorkerExceptionOnMakeObject")));
		}});

		try {
			this.mockedPool.getWorker();
			fail("An RuntimeException was expected.");
		} catch (RuntimeException e) {/* ignore this */}
		
		assertEquals(0, this.mockedPool.getActiveJobCount());
	}
	
}
