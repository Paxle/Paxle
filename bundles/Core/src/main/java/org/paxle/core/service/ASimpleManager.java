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
package org.paxle.core.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;

public class ASimpleManager<Key,Value> implements IManager<Key,Value> {
	
	protected final Map<Key,Value> map = new Hashtable<Key,Value>();
	
	public boolean isKnown(Key key) {
		return this.map.containsKey(key);
	}
	
	public Value get(Key key) {
		return this.map.get(key);
	}
	
	public Collection<Key> getKeys() {
		return new HashSet<Key>(this.map.keySet());
	}
	
	public Collection<Value> getValues() {
		return new HashSet<Value>(this.map.values());
	}
}
