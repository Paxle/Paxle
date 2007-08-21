package org.paxle.core.doc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class IndexerDocument extends HashMap<Field<?>,Object> implements IIndexerDocument {
	
	private static final long serialVersionUID = 1L;
	
	private Status status = null;
	private String statusText = null;
	
	public <Type> void set(Field<Type> key, Type value) {
		super.put(key, value);
	}
	
	// the type of the value can only be Type (or the value is null) as the method above is the only
	// one adding key/value-pairs to the underlying HashMap
	// this has to be made sure by any possible sub-classes as well
	@SuppressWarnings("unchecked")
	public <Type> Type get(Field<Type> prop) {
		return (Type)super.get(prop);
	}
	
	public Iterator<Field<?>> fieldIterator() {
		return super.keySet().iterator();
	}
	
	public Iterator<Map.Entry<Field<?>,Object>> iterator() {
		return super.entrySet().iterator();
	}
	
	public Status getStatus() {
		return this.status;
	}
	
	public String getStatusText() {
		return this.statusText;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}
	
	public void setStatus(Status status, String text) {
		this.status = status;
		this.statusText = text;
	}
	
	public void setStatusText(String text) {
		this.statusText = text;
	}
}
