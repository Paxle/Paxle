package org.paxle.core.doc;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

public interface IParserDocument {
	
	public static enum Status {
		/** Parsing finished without major errors so that the resulting document is usable. */
		OK,
		/** An error occured and parsing the document has to be stopped */
		FAILURE
	}

	/**
	 * Headlines in a document define the scopes of it's content and are therefore important
	 * to understand what it is dealing with. Furthermore headlines are marked specifically
	 * in most formats.
	 *  
	 * @param headline a headline as {@link String}. Weighting is not supported up to now but
	 *        may be added in the future
	 */
	public abstract void addHeadline(String headline);

	/**
	 * A keyword outlines the document's content in one or more words.
	 * 
	 * @param keyword a keyword relative to the content
	 */
	public abstract void addKeyword(String keyword);

	/**
	 * Documents may contain content in several languages. If one is detected, it should be
	 * added here.
	 * 
	 * @param lang a language this document contains text in
	 */
	public abstract void addLanguage(String lang);

	/**
	 * Documents may reference other documents or locations in order to prove or underlay the
	 * content with other writings. 
	 *  
	 * @param ref the URL or resource location referenced
	 * @param name the label of the reference if named, may be <code>null</code>
	 */
	public abstract void addReference(String ref, String name);

	/**
	 * Documents may contain or link to images which themselves are to be found via this
	 * document's content. So images related to or referenced by this document should be added
	 * here.
	 * 
	 * @param ref the URL or resource location referenced
	 * @param name the label or alternate text of the image if named, may be <code>null</code>
	 */
	public abstract void addReferenceImage(String ref, String name);

	/**
	 * If this document is a container for other data, like i.e. an archive, this data may not be
	 * parseable by the archive parser itself. Therefore any so-called sub-document in the container
	 * should be added as child to this document in order to provide a consistent document-tree
	 * for the indexer.
	 *  
	 * @param  location the (absolute if possible) location or path identifying the sub-document
	 *         within the container
	 * @return the newly created sub-document which itself is a child of this document. Any further
	 *         actions are not to be taken in order to link documents to another
	 */
	public abstract IParserDocument addSubDocument(String location);

	/**
	 * Append (parts of) the extracted text. The single words will be findable after indexing
	 * @param text the text of the document as {@link String} in Java's default character encoding,
	 *        Unicode
	 */
	public abstract void addText(CharSequence text);

	/**
	 * The author(s) of the document respectively it's content. Multiple authors have to be
	 * concatenated by a semi-colon (<code>;</code>).
	 * 
	 * @param author the author(s) of the document
	 */
	public abstract void setAuthor(String author);

	/**
	 * The last-modified date provides interesting long-term information regarding how often this
	 * document is being updated. Therefore this date could increase crawling efficency.
	 * 
	 * @param date the date of the last modification of this document in GMT (Greenwhich Mean Time),
	 *        which is used persistently in Paxle
	 */
	public abstract void setLastChanged(Date date);

	/**
	 * @param status the {@link Status result} of the parsing operation
	 */
	public abstract void setStatus(Status status);
	
	/**
	 * Summary of this document's content which may be helpful either for snippets or for
	 * categorizing the document
	 * 
	 * @param summary a short summary of what this documents deals with
	 */
	public abstract void setSummary(String summary);

	/**
	 * @param title the title of the document
	 */
	public abstract void setTitle(String title);

	/**
	 * @see #setAuthor(String)
	 * @return the author(s) of this document or <code>null</code> if not set. Multiple
	 *         authors are separated by a semi-colon
	 */
	public abstract String getAuthor();

	/**
	 * @see #addHeadline(String)
	 * @return a collection of all headlines in this document 
	 */
	public abstract Collection<String> getHeadlines();

	/**
	 * @see #addReferenceImage(String, String)
	 * @return all referenced images by this document, mapping the location to it's (maybe
	 *         non-existant) name or label 
	 */
	public abstract Map<String, String> getImages();

	/**
	 * @see #addKeyword(String)
	 * @return a collection of all keywords describing this document
	 */
	public abstract Collection<String> getKeywords();

	/**
	 * @see #addLanguage(String)
	 * @return a collection of all languages this document contains text in
	 */
	public abstract Set<String> getLanguages();

	/**
	 * @see #setLastChanged(Date)
	 * @return the date of the last modification of this document in GMT or <code>null</code>
	 *         if not set
	 */
	public abstract Date getLastChanged();

	/**
	 * @see #addReference(String, String)
	 * @return all references by this document exclusive images, mapping the location to it's
	 *         (maybe non-existant) name or label
	 */
	public abstract Map<String, String> getLinks();

	/**
	 * @return this resource's location, where and how to retrieve it as URI 
	 */
	public abstract String getLocation();
	
	/**
	 * @see #setStatus(org.paxle.core.doc.IParserDocument.Status)
	 * @return the status of the parsing operation
	 */
	public abstract Status getStatus();

	/**
	 * @see #addSubDocument(String) for a more detailed description of what sub-documents are
	 * @return a collection referencing all sub-documents of this document. Note that the
	 *         returned {@link Set} is not the internal set of this document and adding/removal
	 *         operations won't have an effect on this object
	 */
	// don't manipulate the sub-docs
	public abstract Set<IParserDocument> getSubDocs();

	/**
	 * @see #setSummary(String)
	 * @return a summary of this document's content or <code>null</code> if not available
	 */
	public abstract String getSummary();

	/**
	 * @see #addText(CharSequence)
	 * @return the whole (readable) text of this document as Unicode-sequence 
	 */
	public abstract CharSequence getText();

	/**
	 * @see #setTitle(String)
	 * @return the title of this document or <code>null</code> if not available
	 */
	public abstract String getTitle();

	/**
	 * @return a debugging-friendly expression of everything this document knows 
	 */
	public abstract String toString();
}