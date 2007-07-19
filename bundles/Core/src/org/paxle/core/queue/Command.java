package org.paxle.core.queue;

import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.core.doc.IParserDocument;

public class Command implements ICommand {
	
	private Result result = Result.Passed;
	private String resultText = null;
	
	private String location = null;
	
	private ICrawlerDocument crawlerDoc = null;
	private IParserDocument parserDoc = null;
	private IIndexerDocument indexerDoc = null;

	public ICrawlerDocument getCrawlerDocument() {
		return this.crawlerDoc;
	}

	public void setCrawlerDocument(ICrawlerDocument crawlerDoc) {
		this.crawlerDoc = crawlerDoc;
	}	

	public IParserDocument getParserDocument() {
		return this.parserDoc;
	}

	public void setParserDocument(IParserDocument parserDoc) {
		this.parserDoc = parserDoc;
	}

	public IIndexerDocument getIndexerDocument() {
		return this.indexerDoc;
	}	
	
	
	public void setIndexerDocument(IIndexerDocument indexerDoc) {
		this.indexerDoc = indexerDoc;
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
