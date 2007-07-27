package org.paxle.core.doc;

public final class Field<Type> implements Comparable<Field<?>> {
	
	private final boolean index;
	private final boolean savePlain;
	private final Class<Type> clazz;
	private final String name;
	
	public Field(final boolean index, final boolean savePlain, String name, Class<Type> clazz) {
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
}
