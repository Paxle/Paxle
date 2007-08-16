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
