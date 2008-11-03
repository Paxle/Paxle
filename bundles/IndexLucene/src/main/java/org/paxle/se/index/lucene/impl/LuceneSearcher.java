/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 * 
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.se.index.lucene.impl;

import java.io.Closeable;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.LucenePackage;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;

import org.osgi.service.monitor.Monitorable;
import org.osgi.service.monitor.StatusVariable;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.se.index.IndexException;
import org.paxle.se.index.lucene.ILuceneSearcher;
import org.paxle.se.query.tokens.AToken;

public class LuceneSearcher implements ILuceneSearcher, Closeable, Monitorable {
	
	public static final String PID = "org.paxle.lucene-db";
	
	/* ====================================================================
	 * Names of MA StatusVariables
	 * ==================================================================== */
	private static final String VAR_NAME_KNOWN_DOCS = "docs.known";
	private static final String VAR_NAME_LUCENE_IMPL_VERSION = "lucene.impl.version";
	private static final String VAR_NAME_LUCENE_SPEC_VERSION = "lucene.spec.version";
	
	/**
	 * The names of all {@link StatusVariable status-variables} supported by this {@link Monitorable}
	 */
	private static final HashSet<String> VAR_NAMES =  new HashSet<String>(Arrays.asList(new String[]{
			VAR_NAME_KNOWN_DOCS,
			VAR_NAME_LUCENE_IMPL_VERSION,
			VAR_NAME_LUCENE_SPEC_VERSION
	}));
	
	/**
	 * Descriptions of all {@link StatusVariable status-variables} supported by this {@link Monitorable}
	 */
	private static final HashMap<String, String> VAR_DESCRIPTIONS = new HashMap<String, String>();
	static {
		VAR_DESCRIPTIONS.put(VAR_NAME_KNOWN_DOCS, "Number of documents in the index.");
		VAR_DESCRIPTIONS.put(VAR_NAME_LUCENE_IMPL_VERSION, "Lucene implementation version.");
		VAR_DESCRIPTIONS.put(VAR_NAME_LUCENE_SPEC_VERSION, "Lucene specification version.");
	}	
	
	/**
	 * For logging
	 */
	private final Log logger = LogFactory.getLog(LuceneSearcher.class);
	
	private final LuceneQueryFactory ltf = new LuceneQueryFactory(IIndexerDocument.TEXT, IIndexerDocument.TITLE);
	private final AFlushableLuceneManager manager;
	
	public LuceneSearcher(AFlushableLuceneManager manager) {
		this.manager = manager;
	}
	
	/* TODO: how to handle the timeout properly?
	 * TODO: transform the Paxle-query into a Lucene-query without
	 *       the indirection over String and the query-factory
	 */
	public void search(AToken request, List<IIndexerDocument> results, int maxCount, long timeout) throws IOException {
		final QueryParser queryParser = new QueryParser(null, new StandardAnalyzer());
		final Query query;
		final String queryString = ltf.transformToken(request);
		logger.debug("transformed query string: " + queryString);
		try {
			query = queryParser.parse(queryString);
		} catch (org.apache.lucene.queryParser.ParseException e) {
			throw new IndexException("error parsing query token '" + request + "' - query string: " + queryString, e);
		}
		this.logger.debug("searching for query '" + query + "' (" + request + ")");
		this.manager.search(query, new IIndexerDocHitCollector(results, maxCount));
	}
	
	private class IIndexerDocHitCollector extends AHitCollector {
		
		private final List<IIndexerDocument> results;
		private final int max;
		private int current = 0;
		
		public IIndexerDocHitCollector(List<IIndexerDocument> results, int max) {
			this.results = results;
			this.max = max;
		}
		
		@Override
		public void collect(int doc, float score) {
			LuceneSearcher.this.logger.debug("collecting search result " + this.current + "/" + this.max + ", document id '" + doc + "', score: " + score);
			if (this.current++ < this.max) try {
				final Document rdoc = this.searcher.doc(doc);
				this.results.add(Converter.luceneDoc2IIndexerDoc(rdoc));
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void close() throws IOException {
		this.manager.close();
	}
	
	public int getDocCount() {
		return this.manager.getDocCount();
	}
	
	/* ====================================================================== *
	 * Monitorable support
	 * ====================================================================== */
	
	public String getDescription(String id) throws IllegalArgumentException {
		if (!VAR_NAMES.contains(id))
			throw new IllegalArgumentException("no such variable '" + id + "'");

		return VAR_DESCRIPTIONS.get(id);
	}
	
	public StatusVariable getStatusVariable(String id) throws IllegalArgumentException {
		if (id.equals(VAR_NAME_KNOWN_DOCS)) {
			return new StatusVariable(
					VAR_NAME_KNOWN_DOCS, 
					StatusVariable.CM_CC, 
					this.getDocCount()
			);
		} else if (id.equals(VAR_NAME_LUCENE_IMPL_VERSION)) {
			return new StatusVariable(
					VAR_NAME_LUCENE_IMPL_VERSION, 
					StatusVariable.CM_SI,
					LucenePackage.class.getPackage().getImplementationVersion()
			);
		} else if (id.equals(VAR_NAME_LUCENE_SPEC_VERSION)) {
			return new StatusVariable(
					VAR_NAME_LUCENE_SPEC_VERSION, 
					StatusVariable.CM_SI,
					LucenePackage.class.getPackage().getSpecificationVersion()
			);
		}
		
		throw new IllegalArgumentException("no such variable '" + id + "'");
	}
	
	public String[] getStatusVariableNames() {
		return VAR_NAMES.toArray(new String[VAR_NAMES.size()]);
	}
	
	public boolean notifiesOnChange(String id) throws IllegalArgumentException {
		return false;
	}
	
	public boolean resetStatusVariable(String id) throws IllegalArgumentException {
		return false;
	}
}
