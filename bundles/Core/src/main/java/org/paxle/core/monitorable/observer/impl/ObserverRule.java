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

import java.util.Dictionary;
import java.util.Hashtable;

import org.paxle.core.monitorable.observer.IObserverConcequence;
import org.paxle.core.monitorable.observer.IObserverCondition;
import org.paxle.core.monitorable.observer.IObserverRule;

public class ObserverRule implements IObserverRule {
	private IObserverCondition condition;
	private IObserverConcequence concequence;
	
	public ObserverRule(IObserverCondition condition, IObserverConcequence concequence) {
		this.condition = condition;
		this.concequence = concequence;
	}
	
	public boolean match(Dictionary dictionary) {
		return this.condition.match(dictionary);		
	}
	
	public void triggerAction(Hashtable<String, Object> currentState) {
		this.concequence.triggerAction(currentState);
	}
	
	public IObserverCondition getCondition() {
		return this.condition;
	}
	
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		
		buf.append(this.condition.toString())
		   .append(" -> ")
		   .append(this.concequence.toString());
		
		return buf.toString();
	}
}
