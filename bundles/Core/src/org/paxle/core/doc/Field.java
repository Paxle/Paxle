package org.paxle.core.doc;

public final class Field<Type> {
	
	private final boolean index;
	private final boolean savePlain;
	
	public Field(final boolean index, final boolean savePlain) {
		this.index = index;
		this.savePlain = savePlain;
	}
	
	public final boolean isIndex() {
		return this.index;
	}
	
	public final boolean isSavePlain() {
		return this.savePlain;
	}
}
