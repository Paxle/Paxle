package org.paxle.gui.remote.cm;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

public class ConfigProperty {
	private String id;
	private String name;
	private String description;
	private int cardinality = 0;
	private Map<String, String> options;	
	private String type;
	private Object value;	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getCardinality() {
		return cardinality;
	}
	public void setCardinality(int cardinality) {
		this.cardinality = cardinality;
	}
	public Map<String, String> getOptions() {
		return options;
	}
	public void setOptions(Map<String, String> options) {
		this.options = options;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	@SuppressWarnings("unchecked")
	public int getSize() {
		if (this.value == null) return 1;
		else if (this.value.getClass().isArray()) return Array.getLength(this.value);
		else if (Collection.class.isInstance(this.value)) return ((Collection)this.value).size();
		return 1;
	}
}
