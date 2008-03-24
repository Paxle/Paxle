package org.paxle.core.doc;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.paxle.core.io.IOTools;

public class ParserDocument implements IParserDocument {
	
	/**
	 * Primary key required by Object-EER mapping 
	 */
	protected int _oid;	
	
	protected Map<String,IParserDocument> subDocs = new HashMap<String,IParserDocument>();
	protected Collection<String> headlines = new LinkedList<String>();
	protected Collection<String> keywords = new LinkedList<String>();
	protected Map<URI,String> links = new HashMap<URI,String>();
	protected Map<URI,String> images = new HashMap<URI,String>();
	protected Set<String> languages = new HashSet<String>();	
	protected String author;
	protected Date lastChanged;
	protected String summary;
	protected String title;
	protected IParserDocument.Status status;
	protected String statusText;
	protected File content;
	protected FileWriter contentOut = null;
	protected String mimeType;
	protected Charset charset = Charset.defaultCharset();
	
    public int getOID(){ 
    	return _oid; 
    }

    public void setOID(int OID){ 
    	this._oid = OID; 
    }
    
    public Charset getCharset() {
    	return this.charset;
    }
    
    public void setCharset(Charset charset) {
    	this.charset = charset;
    }
		
	/**
	 * {@inheritDoc}
	 * @see org.paxle.parser.IParserDocument#getHeadlines()
	 */
	public Collection<String> getHeadlines() {
		return this.headlines;
	}    
    
	/**
	 * {@inheritDoc}
	 * @see org.paxle.parser.IParserDocument#addHeadline(java.lang.String)
	 */
	public void addHeadline(String headline) {
		this.headlines.add(whitespaces2Space(headline));
	}
	
	public void setHeadlines(Collection<String> headlines) {
		this.headlines = headlines;
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.paxle.parser.IParserDocument#addKeyword(java.lang.String)
	 */
	public void addKeyword(String keyword) {
		this.keywords.add(whitespaces2Space(keyword));
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.paxle.parser.IParserDocument#addLanguage(java.lang.String)
	 */
	public void addLanguage(String lang) {
		this.languages.add(lang);
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.paxle.parser.IParserDocument#addReference(java.lang.String, java.lang.String)
	 */
	public void addReference(URI ref, String name) {
		name = whitespaces2Space(name);
		if (ref != null) {
			this.links.put(ref,name);
		}
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.paxle.parser.IParserDocument#addReferenceImage(java.lang.String, java.lang.String)
	 */
	public void addReferenceImage(URI ref, String name) {
		name = whitespaces2Space(name);
		if (ref != null)  {
			this.images.put(ref, whitespaces2Space(name));
		}
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.paxle.parser.IParserDocument#addSubDocument(java.lang.String)
	 */
	public void addSubDocument(String location, IParserDocument pdoc) {
		this.subDocs.put(whitespaces2Space(location), pdoc);
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.paxle.parser.IParserDocument#addText(java.lang.CharSequence)
	 */
	public void addText(CharSequence text) throws IOException {
		if (this.content == null) {
			this.content = IOTools.getTempFileManager().createTempFile();
		}
		if (this.contentOut == null) {
			this.contentOut = new FileWriter(this.content);
		}
		this.contentOut.write(text.toString());
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.paxle.parser.IParserDocument#setAuthor(java.lang.String)
	 */
	public void setAuthor(String author) {
		this.author = whitespaces2Space(author);
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.paxle.parser.IParserDocument#setLanguages(java.lang.String[])
	 */
	public void setLanguages(String[] langs) {
		this.languages.clear();
		this.languages.addAll(Arrays.asList(langs));
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.paxle.parser.IParserDocument#setLastChanged(java.util.Date)
	 */
	public void setLastChanged(Date date) {
		this.lastChanged = date;
	}
	
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.paxle.parser.IParserDocument#setSummary(java.lang.String)
	 */
	public void setSummary(String summary) {
		this.summary = whitespaces2Space(summary);
	}
	
	/**
	 * {@inheritDoc}
	 * @see IParserDocument#setTextFile(File)
	 */
	public void setTextFile(File file) throws IOException {
		this.content = file;
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.paxle.parser.IParserDocument#setTitle(java.lang.String)
	 */
	public void setTitle(String title) {
		this.title = whitespaces2Space(title);
	}

	/**
	 * {@inheritDoc}
	 * @see org.paxle.parser.IParserDocument#getAuthor()
	 */
	public String getAuthor() {
		return author;
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.paxle.parser.IParserDocument#getImages()
	 */
	public Map<URI,String> getImages() {
		return this.images;
	}
	
	/**
	 * @see IParserDocument#setImages(Map)
	 * TODO: maybe we should loop through the list and trim all strings
	 */
	public void setImages(Map<URI,String> images) {
		this.images = images;
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.paxle.parser.IParserDocument#getKeywords()
	 */
	public Collection<String> getKeywords() {
		return this.keywords;
	}
	
	/**
	 * @see IParserDocument#setKeywords(Collection)
	 */
	public void setKeywords(Collection<String> keywords) {
		ArrayList<String> keywordsList = new ArrayList<String>(keywords==null?0:keywords.size());
		if (keywords != null) {
			// loop through the keywords and trim
			for (String keyword : keywords) {
				keywordsList.add(keyword.trim());
			}
		}
		this.keywords = keywordsList;
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.paxle.parser.IParserDocument#getLanguages()
	 */
	public Set<String> getLanguages() {
		return this.languages;
	}
	
	/**
	 * @see IParserDocument#setLanguages(Set)
	 */
	public void setLanguages(Set<String> languages) {
		HashSet<String> languageSet = new HashSet<String>(languages==null?0:languages.size());
		if (languages != null) {
			// loop throught the languages ant trim
			for(String language: languages) {
				languageSet.add(language.trim());
			}
		}
		this.languages = languageSet;
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.paxle.parser.IParserDocument#getLastChanged()
	 */
	public Date getLastChanged() {
		return this.lastChanged;
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.paxle.parser.IParserDocument#getLinks()
	 */
	public Map<URI,String> getLinks() {
		return this.links;
	}
	
	/**
	 * @see IParserDocument#setLinks(Map)
	 * TODO: maybe we should loop through the list and trim all strings
	 */
	public void setLinks(Map<URI,String> links) {
		this.links = links;
	}
	
	// don't manipulate the sub-docs
	/**
	 * {@inheritDoc}
	 * @see org.paxle.parser.IParserDocument#getSubDocs()
	 */
	public Map<String,IParserDocument> getSubDocs() {
		return this.subDocs;
	}
	
	/**
	 * @see IParserDocument#setSubDocs(Map)
	 *  TODO: maybe we should loop through the list and trim all strings
	 */
	public void setSubDocs(Map<String,IParserDocument> subDocs) {
		this.subDocs = subDocs;
	}
	
	/**
	 * @see IParserDocument#getMimeType()
	 */
	public String getMimeType() {
		return this.mimeType;
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.paxle.parser.IParserDocument#getSummary()
	 */
	public String getSummary() {
		return this.summary;
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.paxle.parser.IParserDocument#getText()
	 */
	public Reader getTextAsReader() throws IOException {
		close();
		return (this.content == null) ? null : new FileReader(this.content);
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.paxle.parser.IParserDocument#getTitle()
	 */
	public String getTitle() {
		return this.title;
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.paxle.core.doc.IParserDocument#getTextFile()
	 */
	public File getTextFile() throws IOException {
		close();
		return this.content;
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.paxle.core.doc.IParserDocument#getStatus()
	 */
	public IParserDocument.Status getStatus() {
		return this.status;
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.paxle.core.doc.IParserDocument#setStatus(org.paxle.core.doc.IParserDocument.Status)
	 */
	public void setStatus(IParserDocument.Status status) {
		this.status = status;
	}
	
	public String getStatusText() {
		return this.statusText;
	}
	
	public void setStatusText(String statusText) {
		this.statusText = statusText;
	}
	
	public void setStatus(IParserDocument.Status status, String statusText) {
		this.setStatus(status);
		this.setStatusText(statusText);
	}
	
	/**
	 * {@inheritDoc}
	 * @see java.io.Closeable#close()
	 */
	public void close() throws IOException {
		if (this.contentOut != null) {
			this.contentOut.close();
		}
	}
	
	/**
	 * Lists the contents of this document in the following format using line-feeds (ASCII 10 or
	 * <code>\n</code>) for line breaks:
	 * <pre>
	 *   Title: &lt;Title&gt;
	 *   Author: &lt;Author&gt;
	 *   last changed: &lt;Last modified&gt;
	 *   Summary: &lt;Summary&gt;
	 *   Languages:
	 *    * &lt;Language 1&gt;
	 *    * &lt;Language 2&gt;
	 *    ...
	 *   Headlines:
	 *    * &lt;Headline 1&gt;
	 *    * &lt;Headline 2&gt;
	 *    ...
	 *   Keywords:
	 *    * &lt;Keyword 1&gt;
	 *    * &lt;Keyword 2&gt;
	 *    ...
	 *   Images:
	 *    * &lt;Reference 1&gt; -&gt; &lt;Label 1&gt;
	 *    * &lt;Reference 2&gt; -&gt; &lt;Label 2&gt;
	 *    ...
	 *   Links:
	 *    * &lt;Reference 1&gt; -&gt; &lt;Label 1&gt;
	 *    * &lt;Reference 2&gt; -&gt; &lt;Label 2&gt;
	 *    ...
	 *   Text:
	 *   &lt;Text&gt;
	 * </pre>
	 * @see java.util.Date#toString() for the format of the <code>&lt;Last modified&gt;</code>-string
	 * @return a debugging-friendly expression of everything this document knows in the above format
	 */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(100);
		sb.append("Title: ").append(title==null?"":title).append('\n');
		sb.append("Author: ").append(author==null?"":author).append('\n');
		sb.append("last changed: ").append((lastChanged==null)?"":lastChanged.toString()).append('\n');
		sb.append("Summary: ").append(summary==null?"":summary).append('\n');
		print(sb, this.languages, "Languages");
		print(sb, this.headlines, "Headlines");
		print(sb, this.keywords, "Keywords");
		print(sb, this.images, "Images");
		print(sb, this.links, "Links");
//		sb.append("Text:").append('\n').append(this.text);
		return sb.toString();
	}
	
	private static void print(StringBuilder sb, Map map, String name) {
		Iterator<Map.Entry<?,?>> it = map.entrySet().iterator();
		sb.append(name).append(":").append('\n');
		while (it.hasNext()) {
			Map.Entry<?,?> e = it.next();
			sb.append(" * ").append(e.getKey()).append(" -> ").append(e.getValue()).append('\n');
		}
	}
	
	private static void print(StringBuilder sb, Collection<String> col, String name) {
		sb.append(name).append(": ").append('\n');
		Iterator<String> it = col.iterator();
		while (it.hasNext())
			sb.append(" * ").append(it.next().trim()).append('\n');
	}
	
	/**
	 * Converts many whitespaces to one
	 * @param text
	 * @return the resulting string
	 */
	private static String whitespaces2Space(String text) {
		if (text == null) return null;
		return text.replaceAll("\\s", " ").trim();
	}	
}
