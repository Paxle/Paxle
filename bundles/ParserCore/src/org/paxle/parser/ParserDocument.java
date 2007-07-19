package org.paxle.parser;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.paxle.core.doc.IParserDocument;

public final class ParserDocument implements IParserDocument {
	
	private final Set<ParserDocument> subDocs = new HashSet<ParserDocument>();
	private final String location;
	private final Collection<String> headlines = new LinkedList<String>();
	private final Collection<String> keywords = new LinkedList<String>();
	private final Map<String,String> links = new HashMap<String,String>();
	private final Map<String,String> images = new HashMap<String,String>();
	private final Set<String> languages = new HashSet<String>();
	private final StringBuilder text = new StringBuilder();
	private String author;
	private Date lastChanged;
	private String summary;
	private String title;
	
	public ParserDocument(String location) {
		this.location = location;
	}
	
	/* (non-Javadoc)
	 * @see org.paxle.parser.IParserDocument#addHeadline(java.lang.String)
	 */
	public void addHeadline(String headline) {
		this.headlines.add(headline);
	}
	
	/* (non-Javadoc)
	 * @see org.paxle.parser.IParserDocument#addKeyword(java.lang.String)
	 */
	public void addKeyword(String keyword) {
		this.keywords.add(keyword);
	}
	
	/* (non-Javadoc)
	 * @see org.paxle.parser.IParserDocument#addLanguage(java.lang.String)
	 */
	public void addLanguage(String lang) {
		this.languages.add(lang);
	}
	
	/* (non-Javadoc)
	 * @see org.paxle.parser.IParserDocument#addReference(java.lang.String, java.lang.String)
	 */
	public void addReference(String ref, String name) {
		this.links.put(ref, name);
	}
	
	/* (non-Javadoc)
	 * @see org.paxle.parser.IParserDocument#addReferenceImage(java.lang.String, java.lang.String)
	 */
	public void addReferenceImage(String ref, String name) {
		this.images.put(ref, name);
	}
	
	/* (non-Javadoc)
	 * @see org.paxle.parser.IParserDocument#addSubDocument(java.lang.String)
	 */
	public IParserDocument addSubDocument(String location) {
		final ParserDocument doc = new ParserDocument(location);
		this.subDocs.add(doc);
		return doc;
	}
	
	/* (non-Javadoc)
	 * @see org.paxle.parser.IParserDocument#addText(java.lang.CharSequence)
	 */
	public void addText(CharSequence text) {
		this.text.append(text);
	}
	
	/* (non-Javadoc)
	 * @see org.paxle.parser.IParserDocument#setAuthor(java.lang.String)
	 */
	public void setAuthor(String author) {
		this.author = author;
	}
	
	/* (non-Javadoc)
	 * @see org.paxle.parser.IParserDocument#setLanguages(java.lang.String[])
	 */
	public void setLanguages(String[] langs) {
		this.languages.clear();
		this.languages.addAll(Arrays.asList(langs));
	}
	
	/* (non-Javadoc)
	 * @see org.paxle.parser.IParserDocument#setLastChanged(java.util.Date)
	 */
	public void setLastChanged(Date date) {
		this.lastChanged = date;
	}
	
	/* (non-Javadoc)
	 * @see org.paxle.parser.IParserDocument#setSummary(java.lang.String)
	 */
	public void setSummary(String summary) {
		this.summary = summary;
	}
	
	/* (non-Javadoc)
	 * @see org.paxle.parser.IParserDocument#setTitle(java.lang.String)
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/* (non-Javadoc)
	 * @see org.paxle.parser.IParserDocument#getAuthor()
	 */
	public String getAuthor() {
		return author;
	}
	
	/* (non-Javadoc)
	 * @see org.paxle.parser.IParserDocument#getHeadlines()
	 */
	public Collection<String> getHeadlines() {
		return this.headlines;
	}
	
	/* (non-Javadoc)
	 * @see org.paxle.parser.IParserDocument#getImages()
	 */
	public Map<String,String> getImages() {
		return this.images;
	}
	
	/* (non-Javadoc)
	 * @see org.paxle.parser.IParserDocument#getKeywords()
	 */
	public Collection<String> getKeywords() {
		return this.keywords;
	}
	
	/* (non-Javadoc)
	 * @see org.paxle.parser.IParserDocument#getLanguages()
	 */
	public Set<String> getLanguages() {
		return this.languages;
	}
	
	/* (non-Javadoc)
	 * @see org.paxle.parser.IParserDocument#getLastChanged()
	 */
	public Date getLastChanged() {
		return this.lastChanged;
	}
	
	/* (non-Javadoc)
	 * @see org.paxle.parser.IParserDocument#getLinks()
	 */
	public Map<String,String> getLinks() {
		return this.links;
	}
	
	/* (non-Javadoc)
	 * @see org.paxle.parser.IParserDocument#getLocation()
	 */
	public String getLocation() {
		return this.location;
	}
	
	// don't manipulate the sub-docs
	/* (non-Javadoc)
	 * @see org.paxle.parser.IParserDocument#getSubDocs()
	 */
	public Set<IParserDocument> getSubDocs() {
		return new HashSet<IParserDocument>(this.subDocs);
	}
	
	/* (non-Javadoc)
	 * @see org.paxle.parser.IParserDocument#getSummary()
	 */
	public String getSummary() {
		return this.summary;
	}
	
	/* (non-Javadoc)
	 * @see org.paxle.parser.IParserDocument#getText()
	 */
	public StringBuilder getText() {
		return this.text;
	}
	
	/* (non-Javadoc)
	 * @see org.paxle.parser.IParserDocument#getTitle()
	 */
	public String getTitle() {
		return this.title;
	}
	
	/* (non-Javadoc)
	 * @see org.paxle.parser.IParserDocument#toString()
	 */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(100 + this.text.length());
		sb.append("Location: ").append(this.location).append('\n');
		sb.append("Title: ").append(title).append('\n');
		sb.append("Author: ").append(author).append('\n');
		sb.append("last changed: ").append(lastChanged).append('\n');
		sb.append("Summary: ").append(summary).append('\n');
		print(sb, this.languages, "Languages");
		print(sb, this.headlines, "Headlines");
		print(sb, this.keywords, "Keywords");
		print(sb, this.images, "Images");
		print(sb, this.links, "Links");
		sb.append("Text:").append('\n').append(this.text);
		return sb.toString();
	}
	
	private static void print(StringBuilder sb, Map<String,String> map, String name) {
		Iterator<Map.Entry<String,String>> it = map.entrySet().iterator();
		sb.append(name).append(":").append('\n');
		while (it.hasNext()) {
			Map.Entry<String,String> e = it.next();
			sb.append(" * ").append(e.getKey()).append(" -> ").append(e.getValue()).append('\n');
		}
	}
	
	private static void print(StringBuilder sb, Collection<String> col, String name) {
		sb.append(name).append(": ").append('\n');
		Iterator<String> it = col.iterator();
		while (it.hasNext())
			sb.append(" * ").append(it.next()).append('\n');
	}
}
