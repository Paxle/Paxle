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
package org.paxle.core.monitorable.observer.impl;

import java.lang.reflect.Method;
import java.util.Hashtable;

import org.paxle.core.monitorable.observer.IObserverConcequence;

public class ObserverMethodExecutorConcequence implements IObserverConcequence {

	private final Method m;
	private final Object o;
	private final Object[] p;
	
	public ObserverMethodExecutorConcequence(Method m, Object o, Object[] p) {
		this.m = m;
		this.o = o;
		this.p = p;
	}
	
	public void triggerAction(Hashtable<String, Object> currentState) {
		try {
			m.invoke(o, (Object[]) p);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		
		buf.append("method[")
		   .append(this.m.toString())
		   .append("]");		
		
		return buf.toString();
	}
}
