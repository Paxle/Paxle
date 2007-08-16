package org.paxle.core.service;

import java.util.Collection;

public interface IManager<Key,Value> {
	
	public abstract boolean isKnown(Key key);
	public abstract Value get(Key key);
	public abstract Collection<Key> getKeys();
	public abstract Collection<Value> getValues();
}
