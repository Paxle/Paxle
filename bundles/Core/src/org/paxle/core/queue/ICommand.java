package org.paxle.core.queue;

import java.io.Closeable;
import java.io.IOException;

import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.core.doc.IParserDocument;

/**
 * Represents a command-object that is passed to components
 * such as:
 * <ul>
 * 	<li>Core-Crawler</li>
 * 	<li>Core-Parser</li>
 * 	<li>Indexer</li>
 * </ul>
 * 
 * This command is enqueued by a data-provider in the {@link IInputQueue input-queue}
 * of one of the above components. The component processes the {@link ICommand command}
 * and enqueues the modified {@link ICommand command} in the {@link IOutputQueue output-queue}
 * where it is fetched by a data-consumer and written to disk or DB.
 */
public interface ICommand extends Closeable {
	/* =======================================================
	 * General information
	 * ======================================================= */
	public static enum Result {
		Passed,
		Rejected,
		Failure
	}	
	
    public int getOID(); 

    public void setOID(int OID); 
	
	/* TODO: reference a document containing metadata about the overall job
	 * - job ID
	 * - start location
	 * - constraints such as
     *   - reg.exp. filters, depth restriction, etc
	 */	
	
	public Result getResult();
	public String getResultText();
	public void setResultText(String description);
	public void setResult(Result result);
	public void setResult(Result result, String description);
	
	public String getLocation();
	public void setLocation(String location);
	
	public ICrawlerDocument getCrawlerDocument();
	public void setCrawlerDocument(ICrawlerDocument crawlerDoc);
	
	public IParserDocument getParserDocument();
	public void setParserDocument(IParserDocument parserDoc);
	
	public IIndexerDocument[] getIndexerDocuments();
	public void setIndexerDocuments(IIndexerDocument[] indexerDocs);
	public void addIndexerDocument(IIndexerDocument indexerDoc);
	
	// TODO: call this method after the ICommand passed through the whole processing
	public void close() throws IOException;
	
//	/* =======================================================
//	 * Crawler-related information
//	 * ======================================================= */
//	
//	public String getLocation();
//	public void setLocation(String location);
//	
//	public File getCrawlerContent();
//	public void setCrawlerContent(File content);	
//	public int getCrawlerContentSize();
//	
//	public String getCharset();	
//	public void setCharset(String charset);
//	public boolean isCharsetSet();
//	
//	public Date getDocumentDate();
//	public void setDocumentDate(Date documentDate);
}
