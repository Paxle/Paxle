package org.paxle.se.impl;

import java.io.IOException;
import java.text.ParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.paxle.core.doc.IIndexerDocument;
import org.paxle.se.DBUnitializedException;
import org.paxle.se.ISearchEngine;
import org.paxle.se.index.IIndexModifier;
import org.paxle.se.index.IIndexSearcher;
import org.paxle.se.index.IIndexWriter;
import org.paxle.se.index.IndexException;
import org.paxle.se.query.IToken;
import org.paxle.se.query.ITokenFactory;
import org.paxle.se.query.PaxleQueryParser;

public class SEWrapper implements ISearchEngine {
	
	private final Log logger = LogFactory.getLog(ISearchEngine.class);
	
	private PaxleQueryParser pqp = null;
	private IIndexModifier imodifier = null;
	private IIndexSearcher isearcher = null;
	private IIndexWriter iwriter = null;
	
	public IIndexerDocument[] doSearch(String paxleQuery, int count) throws DBUnitializedException, IndexException {
		if (this.isearcher == null)
			throw new DBUnitializedException("IndexSearcher has not been initialized yet");
		if (this.pqp == null)
			throw new DBUnitializedException("Query Parser has not been initialized yet");
		IToken stoken = this.pqp.parse(paxleQuery);
		
		// TODO: search plugins
		
		try {
			IIndexerDocument[] hits = this.isearcher.search(stoken, count, IIndexerDocument.TEXT);
			
			// TODO: post-search plugins / -filters
			
			return hits;
		} catch (IOException e) {
			throw new IndexException("I/O error searching index", e);
		} catch (ParseException e) {
			this.logger.error("Error parsing IToken tree", e);
			return null;
		}
	}
	
	public int getIndexedDocCount() throws DBUnitializedException, IndexException {
		if (this.isearcher == null)
			throw new DBUnitializedException("IndexSearcher has not been initialized yet");
		return this.isearcher.getDocCount();
	}
	
	public void setIModifier(IIndexModifier imodifier) {
		this.imodifier = imodifier;
	}
	
	public void setISearcher(IIndexSearcher isearcher) {
		this.isearcher = isearcher;
	}
	
	public void setIWriter(IIndexWriter iwriter) {
		this.iwriter = iwriter;
	}
	
	public void setTokenFactory(ITokenFactory tfactory) {
		this.pqp = new PaxleQueryParser(tfactory);
	}
}
