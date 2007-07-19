package org.paxle.core.doc;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

public interface IParserDocument {

	public abstract void addHeadline(String headline);

	public abstract void addKeyword(String keyword);

	public abstract void addLanguage(String lang);

	public abstract void addReference(String ref, String name);

	public abstract void addReferenceImage(String ref, String name);

	public abstract IParserDocument addSubDocument(String location);

	public abstract void addText(CharSequence text);

	public abstract void setAuthor(String author);

	public abstract void setLanguages(String[] langs);

	public abstract void setLastChanged(Date date);

	public abstract void setSummary(String summary);

	public abstract void setTitle(String title);

	public abstract String getAuthor();

	public abstract Collection<String> getHeadlines();

	public abstract Map<String, String> getImages();

	public abstract Collection<String> getKeywords();

	public abstract Set<String> getLanguages();

	public abstract Date getLastChanged();

	public abstract Map<String, String> getLinks();

	public abstract String getLocation();

	// don't manipulate the sub-docs
	public abstract Set<IParserDocument> getSubDocs();

	public abstract String getSummary();

	public abstract StringBuilder getText();

	public abstract String getTitle();

	public abstract String toString();

}