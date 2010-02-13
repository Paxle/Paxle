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

package org.paxle.core.doc.impl.jaxb;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.io.IOUtils;
import org.paxle.core.doc.Field;
import org.paxle.core.io.temp.ITempFileManager;

public class JaxbFieldMapAdapter extends XmlAdapter<FieldMap, Map<Field<?>, Serializable>> {
	/**
	 * A component to create temp-files
	 */
	private ITempFileManager tempFileManager;
	
	public JaxbFieldMapAdapter(ITempFileManager tempFileManager) {
		this.tempFileManager = tempFileManager;
	}
	
	@Override
	public FieldMap marshal(Map<Field<?>, Serializable> fieldMap) throws Exception {
		if (fieldMap==null) return null;
		
		final List<FieldMapEntry> fieldList = new ArrayList<FieldMapEntry>();
		for (Entry<Field<?>, Serializable> entry : fieldMap.entrySet()) {			
			final Field<?> key = entry.getKey();
			final DataHandler value = this.convert(key, entry.getValue());
			fieldList.add(new FieldMapEntry(key,value));
		}		
		
		return new FieldMap(fieldList);
	}

	@Override
	public Map<Field<?>, Serializable> unmarshal(FieldMap fieldList) throws Exception {
		if (fieldList==null) return null;
		
		final Map<Field<?>, Serializable> fieldMap = new HashMap<Field<?>, Serializable>();
		for (FieldMapEntry entry : fieldList.getField()) {
			final Field<?> key = entry.getKey();
			final Serializable value = this.convert(key, entry.getValue());
			fieldMap.put(key, value);
		}
		return fieldMap;
	}
	
	private DataHandler convert(Field<?> key, Serializable value) throws IOException {
		if (value == null) return null;
		
		DataSource source = null;
		if (key.getType().isAssignableFrom(File.class)) {
			source = new FileDataSource((File)value);
		} else {			
			source = new JaxbSerializableDataSource(key.getName(), value);
		}
		return new DataHandler(source);
	}
	
	private Serializable convert(Field<?> key, DataHandler handler) throws IOException, ClassNotFoundException {
		if (handler == null) return null;
		
		InputStream input = null;
		OutputStream output = null;
		try {
			// getting the input stream
			input = handler.getInputStream();
			
			if (key.getType().isAssignableFrom(File.class)) {
				File tempFile = null;
				try {
					// getting the output stream
					tempFile = this.tempFileManager.createTempFile();
					output = new BufferedOutputStream(new FileOutputStream(tempFile));
					
					// copy data
					long byteCount = IOUtils.copy(input, output);
					System.out.println(byteCount + " bytes copied");
					return tempFile;
				} catch (IOException e) {
					if (tempFile != null && this.tempFileManager.isKnown(tempFile)) {
						this.tempFileManager.releaseTempFile(tempFile);
					}
					throw e;
				}
			} else {				
				input = new ObjectInputStream(input);
				return (Serializable) ((ObjectInputStream)input).readObject();
			}
		} finally {
			if (input != null) input.close();
			if (output != null) output.close();
		}
	}
}



class FieldMap {
    private List<FieldMapEntry> entries = new ArrayList<FieldMapEntry>();
    
    public FieldMap() {}
    
    public FieldMap(List<FieldMapEntry> entries) {
    	this.entries = entries;
    }
    
    public List<FieldMapEntry> getField() {
    	return this.entries;
    }
    
    public void setField(List<FieldMapEntry> entries) {
    	this.entries = entries;
    }
}

@XmlAccessorType(XmlAccessType.NONE)
class FieldMapEntry {
    public Field<?> key; 
        
    public DataHandler value;
    
    public FieldMapEntry() {}
    
    public FieldMapEntry(Field<?> key, DataHandler value) {
       this.key = key;
       this.value = value;
    }

	@XmlTransient
	public Field<?> getKey() {
		return key;
	}    
    
    @XmlAttribute
	public String getName() {
		return key.getName();
	}

	public void setName(String key) {
		// ignore this here
	}

	@XmlElement
	public String getDescription() {
		return this.key.toString();
	}
	
	public void setDescription(String description) {
		this.key = Field.valueOf(description);
	}
    
	@XmlElement
	public DataHandler getValue() throws IOException {
		return this.value;
	}
	
	public void setValue(DataHandler value) throws IOException, ClassNotFoundException {
		this.value = value;
	}
}
