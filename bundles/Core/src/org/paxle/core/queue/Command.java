package org.paxle.core.queue;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.core.doc.IParserDocument;

public class Command implements ICommand {
	
	private Result result = Result.Passed;
	private String resultText = null;
	
	private String location = null;
	
	private ICrawlerDocument crawlerDoc = null;
	private IParserDocument parserDoc = null;
	private List<IIndexerDocument> indexerDocs = new LinkedList<IIndexerDocument>();

	public ICrawlerDocument getCrawlerDocument() {
		return this.crawlerDoc;
	}

	public void setCrawlerDocument(ICrawlerDocument crawlerDoc) {
		this.crawlerDoc = crawlerDoc;
	}	

	public IParserDocument getParserDocument() {
		return this.parserDoc;
	}

	public void setParserDocument(IParserDocument parserDoc) throws IOException {
		parserDoc.close();
		this.parserDoc = parserDoc;
	}

	public IIndexerDocument[] getIndexerDocuments() {
		return this.indexerDocs.toArray(new IIndexerDocument[this.indexerDocs.size()]);
	}	
	
	
	public void addIndexerDocument(IIndexerDocument indexerDoc) {
		this.indexerDocs.add(indexerDoc);
	}	

	public Result getResult() {
		return this.result;
	}	
	
	public String getResultText() {
		return this.resultText;
	}

	public void setResult(Result status) {
		this.result = status;
	}
		
	public void setResult(Result status, String description) {
		this.result = status;
		this.resultText = description;
	}

	public String getLocation() {
		return this.location;
	}

	public void setLocation(String location) {
		this.location = location;
	}	
}
