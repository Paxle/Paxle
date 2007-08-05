package org.paxle.se.index;

import java.io.IOException;
import java.text.ParseException;

import org.paxle.core.doc.Field;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.se.query.IToken;

public interface IIndexSearcher {
	
	public static final String QUERY_TOKEN_FACTORY = "query.token.factory";
	
	public IIndexerDocument[] search(IToken searchToken, int maxCount, Field<?> defaultField) throws IOException, IndexException, ParseException;
	
	public int getDocCount();
}
