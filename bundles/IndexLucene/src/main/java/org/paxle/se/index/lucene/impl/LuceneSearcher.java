/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2010 the original author or authors.
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

import java.io.IOException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Services;
import org.apache.lucene.LucenePackage;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.osgi.framework.Constants;
import org.osgi.service.monitor.Monitorable;
import org.osgi.service.monitor.StatusVariable;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.se.index.IIndexSearcher;
import org.paxle.se.index.IndexException;
import org.paxle.se.query.tokens.AToken;
import org.paxle.se.search.ISearchProvider;
import org.paxle.se.search.ISearchProviderContext;
import org.paxle.se.search.ISearchRequest;
import org.paxle.se.search.SearchProviderContext;

@Component(immediate=true, metatype=false, name=LuceneSearcher.PID)
@Services({
	@Service(IIndexSearcher.class),
	@Service(ISearchProvider.class),
	@Service(Monitorable.class)
})
@Properties({
	@Property(name="Monitorable-Localization", value="/OSGI-INF/l10n/LuceneSearcher"),
	@Property(name="org.paxle.metadata", boolValue=true),
	@Property(name="org.paxle.metadata.localization", value="/OSGI-INF/l10n/LuceneSearcher")
})
public class LuceneSearcher implements IIndexSearcher, ISearchProvider, Monitorable {
	/**
	 * The {@link Constants#SERVICE_PID} of this {@link Monitorable}
	 */
	static final String PID = "org.paxle.lucene-db";
	
	/**
	 * {@link ResourceBundle} basename
	 */
	static final String RB_BASENAME = "OSGI-INF/l10n/LuceneSearcher";		
	
	/* ====================================================================
	 * Names of MA StatusVariables
	 * ==================================================================== */
	private static final String VAR_NAME_KNOWN_DOCS = "docs.known";
	private static final String VAR_NAME_LUCENE_IMPL_VERSION = "lucene.impl.version";
	private static final String VAR_NAME_LUCENE_SPEC_VERSION = "lucene.spec.version";
	
	/**
	 * The names of all {@link StatusVariable status-variables} supported by this {@link Monitorable}
	 */
	@SuppressWarnings("serial")
	private static final HashSet<String> VAR_NAMES =  new HashSet<String>() {{
		add(VAR_NAME_KNOWN_DOCS);
		add(VAR_NAME_LUCENE_IMPL_VERSION);
		add(VAR_NAME_LUCENE_SPEC_VERSION);
	}};
	
	/**
	 * Descriptions of all {@link StatusVariable status-variables} supported by this {@link Monitorable}
	 */
	private final ResourceBundle rb = ResourceBundle.getBundle(RB_BASENAME);	
	
	/**
	 * For logging
	 */
	private final Log logger = LogFactory.getLog(LuceneSearcher.class);
	
	@Reference
	protected ILuceneManager manager;
	
	@Reference
	protected ISnippetFetcher snippetFetcher;
	
	@Reference
	protected IStopwordsManager stopWordsManager;
	
	private final LuceneQueryFactory ltf = new LuceneQueryFactory(IIndexerDocument.TEXT, IIndexerDocument.TITLE);	
	
	@Activate
	protected void activate(Map<String, Object> props) {
		this.logger.debug(this.getClass().getSimpleName() + " started");
	}
	
	/* TODO: how to handle the timeout properly?
	 * TODO: transform the Paxle-query into a Lucene-query without
	 *       the indirection over String and the query-factory
	 */
	public void search(ISearchRequest searchRequest, List<IIndexerDocument> results) throws IOException, InterruptedException {
		final ISearchProviderContext context = SearchProviderContext.getCurrentContext();
		final AToken request = searchRequest.getSearchQuery();  // the query string
		final long timeout = searchRequest.getTimeout();        // the search timeout
		final int maxCount = searchRequest.getMaxResultCount(); // max amount of items to return 
		
		final Analyzer analyzer = this.stopWordsManager.getDefaultAnalyzer();
		final QueryParser queryParser = new QueryParser(Version.LUCENE_29, null, analyzer);
		final Query query;
		final String queryString = ltf.transformToken(request);
		logger.debug("transformed query string: " + queryString);
		try {
			query = queryParser.parse(queryString);
		} catch (org.apache.lucene.queryParser.ParseException e) {
			throw new IndexException(String.format(
					"error parsing query token '%s' - query string: %s",
					request,
					queryString
			), e);
		}
		
		final long deadline = System.currentTimeMillis() + timeout;
		this.logger.debug("searching for query '" + query + "' (" + request + ")");
		this.manager.search(query, new IIndexerDocHitCollector(
				context, 
				results, 
				maxCount, 
				query, 
				deadline)
		);
	}
	
	private class IIndexerDocHitCollector extends AHitCollector {
		
		private final ISearchProviderContext context;
		private final List<IIndexerDocument> results;
		private final int max;
		private int current = 0;
		private Query query;
		private long deadline;
		
		public IIndexerDocHitCollector(ISearchProviderContext context, List<IIndexerDocument> results, int max, Query query, long deadline) {
			this.context = context;
			this.results = results;
			this.max = max;
			this.query = query;
			this.deadline = deadline;
		}
		
		@Override
		public void collect(int doc, float score) {
			LuceneSearcher.this.logger.debug("collecting search result " + this.current + "/" + this.max + ", document id '" + doc + "', score: " + score);
			if (this.current++ < this.max) try {
				// reading the document from the lucene index
				final Document sourceDoc = this.searcher.doc(doc);
				IIndexerDocument targetDoc = this.context.createDocument();
				
				// converting the document into an indexer-doc
				 Converter.luceneDoc2IIndexerDoc(sourceDoc, targetDoc);
				if (snippetFetcher != null) {					
					targetDoc = snippetFetcher.createProxy(targetDoc, this.query, this.deadline);
				}
				
				// adding to result list
				this.results.add(targetDoc);
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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

		return this.rb.getString(id);
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
