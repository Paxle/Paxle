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
package org.paxle.core.doc;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckForNull;

public interface IParserDocument extends Closeable {

	public static enum Status {
		/** Parsing finished without major errors so that the resulting document is usable. */
		OK,
		/** An error occured and parsing the document has to be stopped */
		FAILURE
	}

	/**
	 * Returns the ID of this pDoc. It is unique across the command DB.
	 * @return the ID of this document 
	 */
	public int getOID(); 
	/**
	 * Sets the ID of this pDoc. It must be unique across the command DB.
	 * @param OID
	 */
	public void setOID(int OID); 

	/**
	 * Headlines in a document define the scopes of it's content and are therefore important
	 * to understand what it is dealing with. Furthermore headlines are marked specifically
	 * in most formats.
	 *  
	 * @param headline a headline as {@link String}. Weighting is not supported up to now but
	 *        may be added in the future
	 */
	public abstract void addHeadline(String headline);

	public void addImage(URI location, String description);

	public void setHeadlines(Collection<String> headlines);

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
	 * content with other writings.<br/>
	 * If possible use {@link #addReference(URI, String, String)} which includes the origin of the reference.
	 *  
	 * @param ref the URL or resource location referenced
	 * @param name the label of the reference if named, may be <code>null</code>
	 */
	public abstract void addReference(URI ref, String name);

	/**
	 * Documents may reference other documents or locations in order to prove or underlay the
	 * content with other writings. 
	 * 
	 * @param ref the URL or resource location referenced
	 * @param name the label of the reference if named, may be <code>null</code>
	 * @param origin the source of this reference, e.g. if generated by a command filter or extracted from a document
	 * @see LinkInfo#ORIGIN
	 */
	public abstract void addReference(URI ref, String name, String origin);

	/**
	 * Documents may reference other documents or locations in order to prove or underlay the
	 * content with other writings. 
	 *  
	 * @param ref the URL or resource location referenced
	 * @param info metadata about the reference. If <code>null</code> an empty {@link LinkInfo} object
	 * is created and added
	 */	
	public void addReference(URI ref, LinkInfo info);

	/**
	 * Documents may contain or link to images which themselves are to be found via this
	 * document's content. So images related to or referenced by this document should be added
	 * here.
	 * 
	 * @param ref the URL or resource location referenced
	 * @param name the label or alternate text of the image if named, may be <code>null</code>
	 */
	public abstract void addReferenceImage(URI ref, String name);

	/**
	 * If this document is a container for other data, like i.e. an archive, this data may not be
	 * parseable by the archive parser itself. Therefore any so-called sub-document in the container
	 * should be added as child to this document in order to provide a consistent document-tree
	 * for the indexer.
	 *  
	 * @param  location the (absolute if possible) location or path identifying the sub-document
	 *         within the container
	 */
	public abstract void addSubDocument(String location, IParserDocument pdoc);

	/**
	 * Append (parts of) the extracted text. The single words will be findable after indexing
	 * @param text the text of the document as {@link String} in Java's default character encoding,
	 *        Unicode
	 */
	public abstract void addText(CharSequence text) throws IOException;

	/**
	 * The author(s) of the document respectively it's content. Multiple authors have to be
	 * concatenated by a semi-colon (<code>;</code>).
	 * 
	 * @param author the author(s) of the document
	 */
	public abstract void setAuthor(String author);

	/**
	 * Returns the Charset of this pDoc.
	 * If this value has not been set via {@link #setCharset(Charset)}, this is <code>null</code>.
	 * @see #setCharset(Charset)
	 */
	@CheckForNull
	public abstract Charset getCharset();

	/**
	 * Sets the charset of this pDoc to the given Charset
	 * @param charset
	 * @see #getCharset()
	 */
	public abstract void setCharset(Charset charset);

	/**
	 * The last-modified date provides interesting long-term information regarding how often this
	 * document is being updated. Therefore this date could increase crawling efficiency.
	 * 
	 * @param date the date of the last modification of this document in GMT (Greenwich Mean Time),
	 *        which is used persistently in Paxle
	 */
	public abstract void setLastChanged(Date date);

	public abstract void setMimeType(String mimeType);

	/**
	 * @param status the {@link Status result} of the parsing operation
	 */
	public abstract void setStatus(Status status);

	public String getStatusText();

	public void setStatusText(String statusText);

	public void setStatus(Status status, String statusText);

	/**
	 * Summary of this document's content which may be helpful either for snippets or for
	 * categorizing the document
	 * 
	 * @param summary a short summary of what this documents deals with
	 */
	public abstract void setSummary(String summary);

	public abstract void setTextFile(File file) throws IOException;

	/**
	 * @param title the title of the document
	 */
	public abstract void setTitle(String title);

	/**
	 * @see #setAuthor(String)
	 * @return the author(s) of this document or <code>null</code> if not set. Multiple
	 *         authors are separated by a semi-colon
	 */
	@CheckForNull
	public abstract String getAuthor();

	/**
	 * @see #addHeadline(String)
	 * @return a collection of all headlines in this document 
	 */
	public abstract Collection<String> getHeadlines();

	/**
	 * @see #addReferenceImage(String, String)
	 * @return all referenced images by this document, mapping the location to it's (maybe
	 *         non-existent) name or label 
	 */
	public abstract Map<URI, String> getImages();

	public void setImages(Map<URI,String> images);

	/**
	 * @see #addKeyword(String)
	 * @return a collection of all keywords describing this document
	 */
	public abstract Collection<String> getKeywords();

	public void setKeywords(Collection<String> keywords);

	/**
	 * @see #addLanguage(String)
	 * @return a collection of all languages this document contains text in or <code>null</code> if unknown
	 */
	@CheckForNull
	public abstract Set<String> getLanguages();

	/**
	 * Sets the set of languages this {@link IParserDocument} contains.
	 * Should be <code>null</code> if the languages are unknown.
	 * @see #addLanguage(String)
	 * @param languages
	 */
	public void setLanguages(Set<String> languages);

	/**
	 * @see #setLastChanged(Date)
	 * @return the date of the last modification of this document in GMT or <code>null</code>
	 *         if not set
	 */
	@CheckForNull
	public abstract Date getLastChanged();

	/**
	 * @see #addReference(String, String)
	 * @return all references by this document exclusive images, mapping the location to 
	 * 		   some metadata, e.g. names or labels
	 */
	public abstract Map<URI, LinkInfo> getLinks();

	public void setLinks(Map<URI,LinkInfo> links);

	public String getMimeType();

	/**
	 * @see #setStatus(org.paxle.core.doc.IParserDocument.Status)
	 * @return the status of the parsing operation
	 */
	public abstract Status getStatus();

	/**
	 * @see #addSubDocument(String, IParserDocument) for a more detailed description of what sub-documents are
	 * @return a collection referencing all sub-documents of this document, mapping their
	 *         respective locations to the parsed sub-document. Note that the returned {@link Map}
	 *         is the internal map of this document and adding/removal operations directly affect
	 *         this object
	 */
	public abstract Map<String,IParserDocument> getSubDocs();
	public void setSubDocs(Map<String,IParserDocument> subDocs);

	/**
	 * @see #setSummary(String)
	 * @return a summary of this document's content or <code>null</code> if not available
	 */
	@CheckForNull
	public abstract String getSummary();

	/**
	 * @see #addText(CharSequence)
	 * @return the whole (readable) text of this document as Unicode-sequence or <code>null</code> if no content is available.
	 */
	@CheckForNull
	public abstract Reader getTextAsReader() throws IOException;

	/**
	 * Gets the content of this document as File.
	 * @return File if addText() has been used with this document, <code>null</code> otherwise (e.g. archives)
	 * @throws IOException
	 */
	@CheckForNull
	public abstract File getTextFile() throws IOException;

	/**
	 * @see #setTitle(String)
	 * @return the title of this document or <code>null</code> if not available
	 */
	@CheckForNull
	public abstract String getTitle();

	/**
	 * @return a debugging-friendly expression of everything this document knows 
	 */
	public abstract String toString();

	public abstract void close() throws IOException;
}