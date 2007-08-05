package org.paxle.se.query.tokens;

import org.paxle.core.doc.Field;
import org.paxle.se.query.IToken;

public class FieldToken extends PlainToken implements IToken {
	
	protected final Field<?> field;
	
	public FieldToken(Field<?> field, String text) {
		super(text);
		this.field = field;
	}
	
	@Override
	public String getString() {
		return "(" + this.getClass().getSimpleName() + ") Field: " + this.field.getName() + " & " + super.getString();
	}
}
