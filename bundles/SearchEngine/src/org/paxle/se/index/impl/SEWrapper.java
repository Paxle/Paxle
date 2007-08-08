package org.paxle.se.index.impl;

import java.io.Closeable;
import java.io.IOException;
import java.text.ParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.paxle.core.doc.Field;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.se.DBUnitializedException;
import org.paxle.se.ISearchEngine;
import org.paxle.se.index.IIndexModifier;
import org.paxle.se.index.IIndexSearcher;
import org.paxle.se.index.IIndexWriter;
import org.paxle.se.index.IndexException;
import org.paxle.se.query.ITokenFactory;
import org.paxle.se.query.PaxleQueryParser;
import org.paxle.se.query.tokens.AToken;
import org.paxle.se.query.tokens.FieldToken;
import org.paxle.se.query.tokens.QuoteToken;

public class SEWrapper implements Closeable {
	
	private final Log logger = LogFactory.getLog(ISearchEngine.class);
	
	private PaxleQueryParser pqp = null;
	private IIndexModifier imodifier = null;
	private IIndexSearcher isearcher = null;
	private IIndexWriter iwriter = null;
	
	/* ==================================================================
	 * Interface methods
	 * ================================================================== */
	
	public IIndexerDocument[] doSearch(String paxleQuery, int count) throws DBUnitializedException, IndexException {
		if (this.isearcher == null)
			throw new DBUnitializedException("IndexSearcher has not been initialized yet");
		if (this.pqp == null)
			throw new DBUnitializedException("Query Parser has not been initialized yet");
		AToken stoken = this.pqp.parse(paxleQuery);
		Field<?> stdField = IIndexerDocument.TEXT;
		
		// TODO: search plugins

		IIndexerDocument[] hits = search(stoken, count, stdField);
		if (hits != null) {
			
			// TODO: post-search plugins / -filters
			
		}
		
		return hits;
	}
	
	public int getIndexedDocCount() throws DBUnitializedException, IndexException {
		if (this.isearcher == null)
			throw new DBUnitializedException("IndexSearcher has not been initialized yet");
		return this.isearcher.getDocCount();
	}
	
	public boolean isKnown(String url) throws DBUnitializedException, IndexException {
		if (this.pqp == null)
			throw new DBUnitializedException("token factory has not been initialized yet");
		// the url should match exactly so we quote it
		final QuoteToken urlToken = this.pqp.getTokenFactory().toQuoteToken(url);
		// only check the LOCATION-field in the index for it
		final FieldToken locationToken = this.pqp.getTokenFactory().toFieldToken(urlToken, IIndexerDocument.LOCATION);
		// search for the new token in the index
		final IIndexerDocument[] hits = search(locationToken, 1, IIndexerDocument.LOCATION);
		return (hits != null && hits.length != 0);
	}
	
	/* ==================================================================
	 * Internal methods
	 * ================================================================== */
	
	private IIndexerDocument[] search(AToken token, int count, Field<?> stdField) throws DBUnitializedException, IndexException {
		if (this.isearcher == null)
			throw new DBUnitializedException("IndexSearcher has not been initialized yet");
		try {
			return this.isearcher.search(token, count);
		} catch (IOException e) {
			throw new IndexException("I/O error searching index", e);
		} catch (ParseException e) {
			this.logger.error("Internal error: parsing generated IToken tree failed", e);
			return null;
		}
	}
	
	/* ==================================================================
	 * Wrapper-specific methods
	 * ================================================================== */
	
	public void addIModifier(IIndexModifier imodifier) {
		if (this.imodifier == null) {
			this.imodifier = imodifier;
			this.logger.info("Registered new index modifier service");
		} else {
			this.logger.error("Another index modifier service has been registered. The current implementation only supports one.");
		}
	}
	
	public void addISearcher(IIndexSearcher isearcher) {
		if (this.isearcher == null) {
			this.isearcher = isearcher;
			this.logger.info("Registered new index searcher service");
		} else {
			this.logger.error("Another index searcher service has been registered. The current implementation only supports one.");
		}
	}
	
	public void addIWriter(IIndexWriter iwriter) {
		if (this.iwriter == null) {
			this.iwriter = iwriter;
			this.logger.info("Registered new index writer service");
		} else {
			this.logger.error("Another index writer service has been registered. The current implementation only supports one.");
		}
	}
	
	public void addTokenFactory(ITokenFactory tfactory) {
		if (this.pqp == null) {
			this.pqp = new PaxleQueryParser(tfactory);
			this.logger.info("Registered new search token factory service");
		} else {
			this.logger.error("Another search token factory service has been registered. The current implementation only supports one.");
		}
	}
	
	public void removeIModifier() {
		this.imodifier = null;
		this.logger.info("Unregistered index modifier service");
	}
	
	public void removeISearcher() {
		this.isearcher = null;
		this.logger.info("Unregistered index searcher service");
	}
	
	public void removeIWriter() {
		this.iwriter = null;
		this.logger.info("Unregistered index writer service");
	}
	
	public void removeTokenFactory() {
		this.pqp = null;
		this.logger.info("Unregistered token factory service");
	}
	
	public void close() throws IOException {
		if (this.iwriter != null)
			this.iwriter.close();
		
		if (this.imodifier != null)
			this.imodifier.close();
		
		this.logger.info("Closed all index services");
	}
}
