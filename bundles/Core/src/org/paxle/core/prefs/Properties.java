package org.paxle.core.prefs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * This has the same API as a standard {@link Properties} class but 
 * stores its value into OSGi {@link Preferences}.
 */
public class Properties extends java.util.Properties {
	private static final long serialVersionUID = 1L;
	
	private Preferences prefs = null;
	
	public Properties(Preferences prefs) {
		if (prefs == null) throw new NullPointerException("The preferences obj was null");
		this.prefs = prefs;
	}
	
	/**
	 * @see java.util.Properties#put(Object, Object)
	 * @see Preferences#put(String, String)
	 */
	@Override
	public Object setProperty(String key, String value)  {
		try {
			String oldValue = this.getProperty(key);
			this.prefs.put(key, value);
			this.prefs.flush();
			return oldValue;
		} catch (BackingStoreException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * @see Properties#getProperty(String, String)
	 */
	@Override
	public String getProperty(String key) {
		return this.getProperty(key, null);
	}
	
	/**
	 * @see java.util.Properties#getProperty(String, String)
	 * @see Preferences#get(String, String)
	 */
	@Override
	public String getProperty(String key, String defaultValue) {
		return this.prefs.get(key, defaultValue);
	}
	
	/**
	 * @see java.util.Properties#remove(Object)
	 * @see Preferences#get(String, String)
	 * @see Preferences#remove(String)
	 */
	@Override
	public Object remove(Object key) {
		if (!(key instanceof String)) throw new IllegalArgumentException("Only string keys are supported");
		
		String oldValue = this.getProperty((String)key);
		this.prefs.remove((String)key);
		return oldValue;
	}
	
	/**
	 * @see java.util.Properties#clear()
	 * @see Preferences#clear()
	 */
	@Override
	public void clear() {
		try {
			this.prefs.clear();
		} catch (BackingStoreException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see java.util.Properties#keys()
	 * @see Preferences#keys()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Enumeration keys() { 
		try {
			String[] keys = this.prefs.keys();
			return Collections.enumeration(Arrays.asList(keys));
		} catch (BackingStoreException e) {
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Enumeration<String> propertyNames() {
		return (Enumeration<String>)this.keys();
	}
	
	/**
	 * @see java.util.Properties#size()
	 */
	@Override
	public int size() {
		try {
			return this.prefs.keys().length;
		} catch (BackingStoreException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * @see java.util.Properties#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return this.size() == 0;
	}
	
	/**
	 * @see java.util.Properties#elements()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public synchronized Enumeration<Object> elements() {
		return new Enumeration<Object>() {
			private final Enumeration<String> keys = Properties.this.keys();

			public boolean hasMoreElements() {
				return keys.hasMoreElements();
			}

			public Object nextElement() {
				String key = keys.nextElement();
				return getProperty(key);
			}			
		};
	}
	
	/**
	 * @see java.util.Properties#contains(Object)
	 */
	@Override
	public boolean contains(Object value) {
		Enumeration<Object> values = this.elements();
		while (values.hasMoreElements()) {
			Object nextValue = values.nextElement();
			if (nextValue == value || nextValue.equals(value)) return true;
		}
		return false;
	}
	
	/**
	 * @see java.util.Properties#containsValue(Object)
	 */
	@Override
	public boolean containsValue(Object value) {
		return this.contains(value);
	}
	
	/**
	 * @see java.util.Properties#containsKey(Object)
	 */
	@Override
	public boolean containsKey(Object key) {
		if (!(key instanceof String)) throw new IllegalArgumentException("Only string keys are supported");
		return this.getProperty((String) key) != null;
	}
	
	/**
	 * @see java.util.Properties#get(Object)
	 */
	@Override
	public Object get(Object key) {
		if (!(key instanceof String)) throw new IllegalArgumentException("Only string keys are supported");
		return this.getProperty((String) key);
	}
	
	/**
	 * @see java.util.Properties#put(Object, Object)
	 */
	@Override
	public Object put(Object key, Object value) {
		if (!(key instanceof String)) throw new IllegalArgumentException("Only string keys are supported");
		if (!(value instanceof String)) throw new IllegalArgumentException("Only string values are supported");
		return this.setProperty((String)key, (String) value);
	}
	
	/**
	 * @see java.util.Properties#putAll(Map)
	 */
	@Override
	public void putAll(Map<? extends Object, ? extends Object> t) {
		if (t == null) return;
		for (Map.Entry<? extends Object, ? extends Object> entry : t.entrySet()) {
			this.put(entry.getKey(), entry.getValue());
		}
	}
	
	/**
	 * @see java.util.Properties#clone()
	 */
	@Override
	public synchronized Object clone() {
		throw new RuntimeException(new CloneNotSupportedException());
	}
	
	/**
	 * @see Properties#toString()
	 */
	@Override
	public synchronized String toString() {
		return this.prefs.toString();
	}
	
	/**
	 * @see java.util.Properties#values()
	 */
	@Override
	public Collection<Object> values() {
		return Collections.list(this.elements());
	}
	
	/**
	 * @see java.util.Properties#keySet()
	 */
	@Override
	public Set<Object> keySet() {
		try {
			return new HashSet<Object>(Arrays.asList(this.prefs.keys()));
		} catch (BackingStoreException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * @see java.util.Properties#entrySet()
	 */
	@Override
	public Set<Entry<Object, Object>> entrySet() {
		throw new RuntimeException("not supported yet!");
	}
	
	@Override
	public synchronized void load(InputStream inStream) throws IOException {
		throw new RuntimeException("not supported yet!");
	}	
	
	@Override
	public synchronized void loadFromXML(InputStream in) throws IOException, InvalidPropertiesFormatException {
		throw new RuntimeException("not supported yet!");
	}	
	
	@Override
	public synchronized void save(OutputStream out, String comments) {
		throw new RuntimeException("not supported yet!");
	}
	
	@Override
	public synchronized void store(OutputStream out, String comments) throws IOException {
		throw new RuntimeException("not supported yet!");
	}
	
	@Override
	public synchronized void storeToXML(OutputStream os, String comment) throws IOException {
		throw new RuntimeException("not supported yet!");	
	}
	
	@Override
	public synchronized void storeToXML(OutputStream os, String comment, String encoding) throws IOException {
		throw new RuntimeException("not supported yet!");	
	}
	
	@Override
	public void list(PrintStream out) {
		throw new RuntimeException("not supported yet!");
	}
	
	@Override
	public void list(PrintWriter out) {
		throw new RuntimeException("not supported yet!");	
	}
	
	public String[] getArray(String key) {
		String stringValue = this.getProperty(key);
		if (stringValue == null) return new String[0];
		return stringValue.split(",");
	}
	
	public List<String> getList(String key) {
		return Arrays.asList(this.getArray(key));
	}	
	
	public Set<String> getSet(String key) {
		return new HashSet<String>(this.getList(key));
	}
	
	public void setArray(String key, String[] valueArray) {
		if (valueArray == null || valueArray.length == 0) {
			this.remove(key);
			return;
		}
		
		String valueString = Arrays.toString(valueArray);
		valueString = valueString.substring(1, valueString.length()-1);
		
		this.setProperty(key, valueString);
	}	
	
	public void setList(String key, List<String> list) {
		if (list == null) {
			this.remove(key);
			return;
		}
		
		this.setArray(key, list.toArray(new String[list.size()]));
	}
	
	public void setSet(String key, Set<String> set) {
		if (set == null) {
			this.remove(key);
			return;
		}
		
		this.setArray(key, set.toArray(new String[set.size()]));
	}
}
