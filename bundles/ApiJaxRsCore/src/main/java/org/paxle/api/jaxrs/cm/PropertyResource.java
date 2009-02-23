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
package org.paxle.api.jaxrs.cm;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import javax.ws.rs.GET;

import org.osgi.service.cm.Configuration;
import org.osgi.service.metatype.AttributeDefinition;


/**
 * This class is used as a wrapper around a single {@link Configuration}-property.
 * @see Configuration#getProperties()#getValue()
 */
public class PropertyResource {
	/**
	 * A mapping between type-nr and type string
	 */
    private static final Map<Integer,Class<?>> TYPEMAP = new HashMap<Integer,Class<?>>();
    static {
    	TYPEMAP.put(Integer.valueOf(AttributeDefinition.BOOLEAN), Boolean.class);
    	TYPEMAP.put(Integer.valueOf(AttributeDefinition.BYTE), Byte.class);
    	TYPEMAP.put(Integer.valueOf(AttributeDefinition.CHARACTER), Character.class);
    	TYPEMAP.put(Integer.valueOf(AttributeDefinition.DOUBLE), Double.class);
    	TYPEMAP.put(Integer.valueOf(AttributeDefinition.FLOAT), Float.class);
    	TYPEMAP.put(Integer.valueOf(AttributeDefinition.INTEGER), Integer.class);
    	TYPEMAP.put(Integer.valueOf(AttributeDefinition.LONG), Long.class);
    	TYPEMAP.put(Integer.valueOf(AttributeDefinition.SHORT), Short.class);
    	TYPEMAP.put(Integer.valueOf(AttributeDefinition.STRING), String.class);
    }		
	
	private Object value;
	private String propID;
	private AttributeDefinition attribDef;
	
	public PropertyResource() {
		// needed for reading
	}
	
	public PropertyResource(String propID, Object propValue, AttributeDefinition attribDef) {
		this.propID = propID;
		this.value = propValue;
		this.attribDef = attribDef;
	}
	
	/**
	 * This method is called when using the URL
	 * <pre>http://localhost:8282/configurations/[bundleID]/[servicePID]/[propertyID]</pre>
	 * 
	 * @return the {@link PropertyResource} itself
	 */
	@GET
	public PropertyResource returnThis() {
		return this;
	}
	
	/**
	 * @return the id of this property as {@link String}
	 * @see AttributeDefinition#getID()
	 */
	public String getID() {
		return this.propID;
	}
	
	public void setID(String propID) {
		this.propID = propID;
	}
	
	/**
	 * @return the type of this property as {@link String}
	 * @see AttributeDefinition#getType()
	 */
	public String getType() {
		if (this.attribDef == null) return null;		
		int type = this.attribDef.getType();
		return TYPEMAP.get(Integer.valueOf(type)).getSimpleName();
	}
	
	/**
	 * @return the name of this property
	 * @see AttributeDefinition#getName()
	 */
	public String getName() {
		if (this.attribDef == null) return null;
		return this.attribDef.getName();
	}
	
	/**
	 * @return the description of this property
	 * @see AttributeDefinition#getDescription()
	 */
	public String getDescription() {
		if (this.attribDef == null) return null;
		return this.attribDef.getDescription();
	}	
	
	/**
	 * @return the value of this property
	 */
	public Object getValue() {
		if (this.value != null) return this.value;
		else if (this.attribDef == null) return null;
		else return this.getDefaultPropertyValue();
	}
	
	public void setValue(Object value) {
		this.value = value;
	}
		
	public Integer getCardinality() {
		if (this.attribDef == null) return null;
		return new Integer(this.attribDef.getCardinality());
	}
	
	public String[] getDefaultValue() {
		if (this.attribDef == null) return null;
		return this.attribDef.getDefaultValue();
	}
	
	public LinkedHashMap<String, String> getOptions() {
		if (this.attribDef == null) return null;
		else if (this.attribDef.getOptionLabels() == null) return null;
		else if (this.attribDef.getOptionValues() == null) return null;
		
		String[] labels = this.attribDef.getOptionLabels();
		String[] values = this.attribDef.getOptionValues();
		if (labels.length != values.length) return null;
		
		LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
		for (int i=0; i < labels.length; i++) {
			options.put(values[i], labels[i]);
		}
		return options;
	}
	
	@SuppressWarnings("unchecked")
	private Object getDefaultPropertyValue() {
		Object defaultValue = null;
		try {
			String[] defaultValues = this.attribDef.getDefaultValue();
			if (defaultValues == null || defaultValues.length == 0) return null;
			
			int cardinality = this.attribDef.getCardinality();
			Class<?> typeClass = TYPEMAP.get(this.attribDef.getType());
			int size =  Math.min(Math.abs(cardinality), defaultValues.length);
			
			// loop through the default values and convert them
			for (int i=0; i < defaultValues.length; i++) {
				Object value = this.valueOf(cardinality, typeClass, defaultValues[i]);
				if (cardinality == 0) {
					defaultValue = value;
					break;
				} else if (cardinality < 0) {
					if (defaultValue == null) defaultValue = new Vector<Object>();
					((Vector<Object>)defaultValue).add(value);
				} else if (cardinality > 0) {					
					if (defaultValue == null) defaultValue = Array.newInstance(typeClass, size);
					Array.set(defaultValue, i, value);
				}
			}			
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return defaultValue;		
	}	
	
	private Object valueOf(int cardinality, Class<?> typeClass, String value) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
		// getting the primitive type-class (if required)
		if (!String.class.isAssignableFrom(typeClass) && cardinality < 0) {
			// add primitive type
			Field primitiveTypeField = typeClass.getDeclaredField("TYPE");
			typeClass = (Class<?>) primitiveTypeField.get(null);
		}
		
		// get parser-method
		Method valueOf = null;
		try {
			valueOf = typeClass.getMethod("valueOf", String.class);
		} catch (NoSuchMethodException e) {
			valueOf = typeClass.getMethod("valueOf", Object.class);
		}
		
		// get concrete value
		return valueOf.invoke(null, value);		
	}
}
