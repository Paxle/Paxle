package org.paxle.core.queue;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.core.doc.IParserDocument;

public class Command implements ICommand {
	
	/**
	 * Primary key required by Object-EER mapping 
	 */
	private int _oid;
	
	private Result result = Result.Passed;
	private String resultText = null;
	
	private String location = null;
	
	private ICrawlerDocument crawlerDoc = null;
	private IParserDocument parserDoc = null;
	private List<IIndexerDocument> indexerDocs = new LinkedList<IIndexerDocument>();

	public static Command createCommand(String location) {
		Command cmd = new Command();
		cmd.setLocation(location);
		return cmd;
	}

    public int getOID(){ 
    	return _oid; 
    }

    public void setOID(int OID){ 
    	this._oid = OID; 
    }	
	
	public ICrawlerDocument getCrawlerDocument() {
		return this.crawlerDoc;
	}

	public void setCrawlerDocument(ICrawlerDocument crawlerDoc) {
		this.crawlerDoc = crawlerDoc;
//		if (this.crawlerDoc != null) this.crawlerDoc.setCommand(this);
	}	

	public IParserDocument getParserDocument() {
		return this.parserDoc;
	}

	public void setParserDocument(IParserDocument parserDoc) throws IOException {
		if (parserDoc != null) parserDoc.close();
		this.parserDoc = parserDoc;
	}

	public IIndexerDocument[] getIndexerDocuments() {
		return this.indexerDocs.toArray(new IIndexerDocument[this.indexerDocs.size()]);
	}
	
	public void setIndexerDocuments(IIndexerDocument[] indexerDocs) {
		this.indexerDocs.clear();
		this.indexerDocs.addAll(Arrays.asList(indexerDocs));
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
	
	public void setResultText(String description) {
		this.resultText = description;
	}

	public void setResult(Result status) {
		this.result = status;
	}
		
	public void setResult(Result status, String description) {
		this.setResult(status);
		this.setResultText(description);
	}

	public String getLocation() {
		return this.location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		str.append(this.location);
		
		return str.toString();
	}
}
