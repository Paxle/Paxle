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
package org.paxle.core.doc.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.paxle.core.doc.Field;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.core.doc.impl.jaxb.JaxbFactory;
import org.paxle.core.doc.impl.jaxb.JaxbFieldMapAdapter;

@XmlRootElement(name="indexerDocument")
@XmlType(factoryClass=JaxbFactory.class, factoryMethod="createBasicIndexerDocument")
public class BasicIndexerDocument extends HashMap<Field<?>,Object> implements IIndexerDocument {	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Primary key required by Object-EER mapping 
	 */
	protected int _oid;		
	
	protected IIndexerDocument.Status status = null;
	
	protected String statusText = null;
	
	@XmlAttribute(name="id")
    public int getOID(){ 
    	return _oid; 
    }

    public void setOID(int OID){ 
    	this._oid = OID; 
    }			
	
	public <Type extends Serializable> void set(Field<Type> key, Type value) {
		if (value == null)
			throw new NullPointerException("Tried setting " + key.getName() + " to null");
		super.put(key, value);
	}
	
	// the type of the value can only be Type (or the value is null) as the method above is the only
	// one adding key/value-pairs to the underlying HashMap
	// this has to be made sure by any possible sub-classes as well
	@SuppressWarnings("unchecked")
	public <Type extends Serializable> Type get(Field<Type> prop) {
		return (Type)super.get(prop);
	}
	
	public Iterator<Field<?>> fieldIterator() {
		return super.keySet().iterator();
	}
	
	public Iterator<Map.Entry<Field<?>,Object>> iterator() {
		return super.entrySet().iterator();
	}
	
	public IIndexerDocument.Status getStatus() {
		return this.status;
	}
	
	public String getStatusText() {
		return this.statusText;
	}
	
	public void setStatus(IIndexerDocument.Status status) {
		this.status = status;
	}
	
	public void setStatus(IIndexerDocument.Status status, String text) {
		this.status = status;
		this.statusText = text;
	}
	
	public void setStatusText(String text) {
		this.statusText = text;
	}
	
	@SuppressWarnings("unchecked")
	@XmlJavaTypeAdapter(JaxbFieldMapAdapter.class)
	public Map<Field<?>, ?> getFields() {
		return (Map<Field<?>, ?>) this.clone();
	}

	public void setFields(Map<Field<?>, ?> fields) {
		this.clear();
		this.putAll(fields);
	}
}
