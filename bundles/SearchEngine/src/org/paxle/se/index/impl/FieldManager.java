package org.paxle.se.index.impl;

import org.paxle.core.doc.Field;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.core.service.ASimpleManager;
import org.paxle.se.index.IFieldManager;

public class FieldManager extends ASimpleManager<String,Field<?>> implements IFieldManager {
	
	public FieldManager() {
		add(IIndexerDocument.AUTHOR);
		add(IIndexerDocument.INTERNAL_NAME);
		add(IIndexerDocument.KEYWORDS);
		add(IIndexerDocument.LANGUAGES);
		add(IIndexerDocument.LAST_CRAWLED);
		add(IIndexerDocument.LAST_MODIFIED);
		add(IIndexerDocument.LOCATION);
		add(IIndexerDocument.MD5);
		add(IIndexerDocument.SIZE);
		add(IIndexerDocument.SUMMARY);
		add(IIndexerDocument.TEXT);
		add(IIndexerDocument.TITLE);
		add(IIndexerDocument.TOPICS);
	}
	
	void add(Field<?> field) {
		super.map.put(field.getName(), field);
	}
}
