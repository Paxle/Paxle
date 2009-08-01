/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package org.paxle.core.doc.impl;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.paxle.core.doc.ICommand;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.core.doc.IParserDocument;


public class BasicCommand implements ICommand {
	
	/**
	 * Primary key required by Object-EER mapping 
	 */
	protected int _oid;
	
	/**
	 * 
	 */
	protected int depth = 0;
	
	/**
	 * ID of the {@link ICommandProfile profile} this {@link ICommand}
	 * belongs to.
	 */
	protected int profileID = -1;
	
	/**
	 * Current status of {@link ICommand} processing. 
	 * @see #getResult()
	 */
	protected Result result = Result.Passed;
	
	/**
	 * A textual description of {@link #result}
	 * @see #getResultText()
	 */
	protected String resultText = null;
	
	/**
	 * The location of the document to process.
	 * @see #getLocation()
	 */
	protected URI location = null;
	
	/**
	 * The crawled {@link ICrawlerDocument document}
	 */
	protected ICrawlerDocument crawlerDoc = null;
	
	/**
	 * The parsed {@link IParserDocument document}.
	 * A {@link IParserDocument parser-document} can contain multiple
	 * {@link IParserDocument sub-parser-document}
	 */
	protected IParserDocument parserDoc = null;
	
	/**
	 * The indexed {@link IIndexerDocument documents}-
	 */
	protected List<IIndexerDocument> indexerDocs = new LinkedList<IIndexerDocument>();

    public int getOID(){ 
    	return _oid; 
    }

    public void setOID(int OID){ 
    	this._oid = OID; 
    }	
	
    public int getProfileOID() {
    	return this.profileID;
    }
    
    public void setProfileOID(int profileOID) {
    	this.profileID = profileOID;
    }
    
    public int getDepth() {
    	return this.depth;
    }
    
    public void setDepth(int depth) {
    	if (depth < 0) throw new IllegalArgumentException("The depth must be greater or qual 0.");
    	this.depth = depth;
    }
    
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
		if (parserDoc != null) try { parserDoc.close(); } catch (Exception e) {e.printStackTrace();}
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
	
	public boolean isResult(Result result) {
		return (this.result == null) 
			? Result.Passed.equals(result)
			: this.result.equals(result);
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

	public URI getLocation() {
		return this.location;
	}

	public void setLocation(URI location) {
		this.location = location;
	}
	
	public void close() throws IOException {
		this.crawlerDoc.close();
		this.parserDoc.close();
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		str.append(this.location);
		
		return str.toString();
	}
}
