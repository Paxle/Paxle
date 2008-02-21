package org.paxle.core.doc;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class Field<Type extends Serializable> implements Comparable<Field<?>>, Serializable {
	private static final long serialVersionUID = 1L;

	private static Pattern pattern = Pattern.compile("(\\w+)\\s\\(([^)]+)\\)(?:\\s(indexed))?(?:\\s(savedPlain))?");
	
	private final boolean index;
	private final boolean savePlain;
	private final Class<Type> clazz;
	private final String name;
	
	/**
	 * @param index specifies that the value of the field is made <i>searchable</i>
	 * @param savePlain specifies that the value of the field is <i>stored as-is</i> in the index
	 * @param name the name of the field
	 * @param clazz the field value type, which must be {@link Serializable serializable}
	 */
	public Field(final boolean index, final boolean savePlain, String name, Class<Type> clazz) {
		if (!(clazz instanceof Serializable)) throw new IllegalArgumentException("Class must be serializable");
		if (name.length() > 80) throw new IllegalArgumentException("The name is too long. A maximum of 80 chars is allowed.");
		this.index = index;
		this.savePlain = savePlain;
		this.name = name;
		this.clazz = clazz; 
	}
	
	public final boolean isIndex() {
		return this.index;
	}
	
	public final boolean isSavePlain() {
		return this.savePlain;
	}
	
	public final Class<Type> getType() {
		return this.clazz;
	}
	
	public final String getName() {
		return this.name;
	}
	
	public int compareTo(Field<?> o) {
		return this.name.compareTo(o.name);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Field) {
			return this.name.equals(((Field)obj).name);
		}
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return this.name.hashCode();
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(this.name);
		sb.append(" (").append(this.clazz.getName()).append(')');
		if (this.index)
			sb.append(" indexed");
		if (this.savePlain)
			sb.append(" savedPlain");
		return sb.toString();
	}
	
	@SuppressWarnings("unchecked")
	public static Field valueOf(String value) {
		Matcher m = pattern.matcher(value);
		if (!m.matches()) throw new IllegalArgumentException();
		
		String name = m.group(1);
		String clazzName = m.group(2);
		boolean index = m.group(3) != null;
		boolean savePlain = m.group(4) != null;
		
		try {
			return new Field(
					index,
					savePlain,
					name,
					Thread.currentThread().getContextClassLoader().loadClass(clazzName)
			);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
