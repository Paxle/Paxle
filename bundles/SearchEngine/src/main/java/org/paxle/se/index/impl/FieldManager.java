/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 * 
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

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
						this.logger.debug(String.format("New field detected: %s.", obj.toString()));
						this.add((Field<?>)obj);
					}
				} catch (Exception e) {
					this.logger.error("Unexpected error while determining default fields",e);
				}
			}
		}
	}
	
	void add(Field<?> field) {
		if (field == null) throw new NullPointerException("The field is null");
		super.map.put(field.getName(), field);
	}
}
