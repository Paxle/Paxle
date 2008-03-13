
package org.paxle.se.query.tokens;

import org.paxle.core.doc.Field;

public class FieldToken extends AToken {
	
	protected final Field<?> field;
	protected final AToken token;
	
	public FieldToken(AToken token, Field<?> field) {
		this.token = token;
		this.field = field;
	}
	
	public AToken getToken() {
		return token;
	}
	
	public Field<?> getField() {
		return field;
	}
	
	@Override
	public String toString() {
		return "(" + this.getClass().getSimpleName() + ") Field: " + this.field.getName() + " & " + this.token.toString();
	}
}
