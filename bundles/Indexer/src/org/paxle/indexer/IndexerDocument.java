package org.paxle.indexer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.paxle.core.doc.Field;
import org.paxle.core.doc.IIndexerDocument;

public class IndexerDocument extends HashMap<Field<?>,Object> implements IIndexerDocument {
	
	private static final long serialVersionUID = 1L;
	
	public <Type> void set(Field<Type> key, Type value) {
		super.put(key, value);
	}
	
	@Override
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
}
