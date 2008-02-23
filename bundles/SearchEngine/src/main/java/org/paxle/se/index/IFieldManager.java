package org.paxle.se.index;

import java.util.Collection;

import org.paxle.core.doc.Field;
import org.paxle.core.service.IManager;

public interface IFieldManager extends IManager<String,Field<?>> {
	
	public boolean isKnown(String key);
	public Field<?> get(String key);
	public Collection<String> getKeys();
	public Collection<Field<?>> getValues();
}
