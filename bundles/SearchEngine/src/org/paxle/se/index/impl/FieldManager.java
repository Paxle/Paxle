package org.paxle.se.index.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.doc.Field;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.core.service.ASimpleManager;
import org.paxle.se.index.IFieldManager;

public class FieldManager extends ASimpleManager<String,Field<?>> implements IFieldManager {
	private Log logger = LogFactory.getLog(this.getClass());
	
	public FieldManager() {		
		// detect all default indexer-document fields
		java.lang.reflect.Field[] fields = IIndexerDocument.class.getFields();
		if (fields != null) {
			for (java.lang.reflect.Field field : fields) {
				Object obj = null;
				try {
					obj = field.get(null);
					if (obj instanceof Field) {
						this.logger.debug(String.format("New field detected: %s.",((Field)obj).toString()));
						this.add((Field)obj);
					}
				} catch (Exception e) {
					this.logger.error("Unexpected error while determining default fields",e);
				}
			}
		}
	}
	
	void add(Field<?> field) {
		super.map.put(field.getName(), field);
	}
}
