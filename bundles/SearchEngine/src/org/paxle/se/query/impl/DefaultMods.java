package org.paxle.se.query.impl;

import java.util.Hashtable;
import java.util.Map;

import org.paxle.core.doc.Field;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.se.query.ITokenFactory;
import org.paxle.se.query.tokens.FieldToken;
import org.paxle.se.query.tokens.PlainToken;

public class DefaultMods {
	
	private static final Map<String,Field<?>> fieldMods = new Hashtable<String,Field<?>>();
	static {
		fieldMods.put("inurl", IIndexerDocument.LOCATION);
		fieldMods.put("title", IIndexerDocument.TITLE);
		fieldMods.put("author", IIndexerDocument.AUTHOR);
		fieldMods.put("domain", IIndexerDocument.LOCATION);	// TODO
		fieldMods.put("mimetype", IIndexerDocument.MIME_TYPE);
		fieldMods.put("protocol", IIndexerDocument.PROTOCOL);
	}
	
	public static boolean isModSupported(String mod) {
		return fieldMods.containsKey(mod);
	}
	
	public static FieldToken toToken(ITokenFactory factory, PlainToken token, String modifier) {
		return factory.toFieldToken(token, fieldMods.get(modifier));
	}
}
