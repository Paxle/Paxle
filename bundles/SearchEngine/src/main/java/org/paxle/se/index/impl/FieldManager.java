/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2010 the original author or authors.
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

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.paxle.core.doc.Field;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.se.index.IFieldManager;

@Component
@Service(IFieldManager.class)
public class FieldManager extends AbstractMap<String, Field<?>> implements IFieldManager {
	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());

	/**
	 * A set of all currently known {@link Field fields}
	 */
	private final Map<String, Field<?>> fields = new HashMap<String, Field<?>>();
	
	public FieldManager() {		
		// detect all default indexer-document fields
		java.lang.reflect.Field[] fields = IIndexerDocument.class.getFields();
		if (fields != null) {
			for (java.lang.reflect.Field field : fields) {
				Object obj = null;
				try {
					obj = field.get(null);
					if (obj instanceof Field) {
						Field<?> indexerField = (Field<?>)obj;
						this.logger.debug(String.format("New field detected: %s.", indexerField.toString()));
						this.put(indexerField.getName(), indexerField);
					}
				} catch (Exception e) {
					this.logger.error("Unexpected error while determining default fields",e);
				}
			}
		}
	}
	
	public boolean isKnown(String fieldName) {
		return this.containsKey(fieldName);
	}

	@Override
	public Set<Entry<String, Field<?>>> entrySet() {
		return this.fields.entrySet();
	}
		
	@Override
	public Field<?> put(String key, Field<?> value) {
		return this.fields.put(key, value);
	}	
	
	public Field<?> get(String key) {
		return super.get(key);
	}

	public Collection<String> getFieldNames() {
		return Collections.unmodifiableSet(this.keySet());
	}

	public Collection<Field<?>> getFields() {
		return Collections.unmodifiableCollection(this.values());
	}
}
