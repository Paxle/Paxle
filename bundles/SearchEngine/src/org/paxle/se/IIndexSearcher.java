package org.paxle.se;

import java.io.IOException;

import org.paxle.core.doc.IIndexerDocument;
import org.paxle.se.query.IToken;

public interface IIndexSearcher {
	
	public static final String QUERY_TOKEN_FACTORY = "query.token.factory";
	
	public IIndexerDocument[] search(IToken searchToken) throws IOException, IndexException;
}
