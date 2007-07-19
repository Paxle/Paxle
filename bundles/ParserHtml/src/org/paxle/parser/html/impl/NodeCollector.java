package org.paxle.parser.html.impl;

import java.util.Collection;
import java.util.LinkedList;

import org.htmlparser.Attribute;
import org.htmlparser.PrototypicalNodeFactory;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.tags.HeadingTag;
import org.htmlparser.tags.Html;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.JspTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.MetaTag;
import org.htmlparser.tags.ParagraphTag;
import org.htmlparser.tags.ScriptTag;
import org.htmlparser.tags.StyleTag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.visitors.NodeVisitor;

import org.paxle.parser.ParserDocument;
import org.paxle.parser.html.impl.tags.AddressTag;
import org.paxle.parser.html.impl.tags.MetaTagManager;

public class NodeCollector extends NodeVisitor {
	
	public static enum Debug {
		NONE, LOW, HIGH
	}
	
	public static final PrototypicalNodeFactory NODE_FACTORY = new PrototypicalNodeFactory();
	static {
		NODE_FACTORY.registerTag(new AddressTag());
	}
	
	private final Collection<Exception> exceptions = new LinkedList<Exception>();
	private final MetaTagManager mtm = new MetaTagManager();
	private final ParserDocument doc;
	private final Debug debug;
	private boolean noParse = false;
	
	public NodeCollector(final ParserDocument doc, final Debug debug) {
		super(true, true);
		this.doc = doc;
		this.debug = debug;
	}
	
	public void postProcessMeta() {
		final String lngs = this.mtm.getCombined(
				MetaTagManager.Names.Content_Language,
				MetaTagManager.Names.Language);
		if (lngs != null)
			addLanguages(lngs.split(" "));
		
		final String author = this.mtm.getCombined(
				MetaTagManager.Names.Author,
				MetaTagManager.Names.Creator,
				MetaTagManager.Names.Contributor,
				MetaTagManager.Names.Publisher);
		if (author != null)
			this.doc.setAuthor(HtmlTools.deReplaceHTML(author));
	}
	
	public Collection<Exception> getExceptions() {
		return this.exceptions;
	}
	
	private void addLanguages(String... languages) {
		for (final String language : languages) {
			boolean valid = language != null && (language.length() == 2 || language.length() == 5 && language.charAt(3) == '-');
			if (!valid) continue;
			// we lower-case the first part because ISO 639 states that language names
			// are to be written lower-case, country names should be written upper-case
			// instead but we aren't searching for the country :)
			this.doc.addLanguage(language.substring(0, 2).toLowerCase());
		}
	}
	
	@Override
	public void visitStringNode(Text string) {
		/*if ((((TextNode)string).getParent() instanceof ScriptTag)) {
			System.err.println("tag: " + ((TextNode)string).getText());
			Node n = ((TextNode)string);
			while ((n = n.getParent()) != null)
				System.err.println("parent is: " + n.getClass().getName());
			//System.err.println();
		} else {*/
		if (!this.noParse) {
			final String txt = whitespace2Space(HtmlTools.deReplaceHTML(string.getText()));
			if (txt.length() > 0)
				this.doc.addText(txt);
		}
	}
	
	@Override
	public void finishedParsing() {
		postProcessMeta();
	}
	
	@Override
	public void visitTag(Tag tag) {
		if (this.debug == Debug.HIGH)
			printTagInfo(tag, true);
		try {
			if (tag instanceof AddressTag)			process((AddressTag)tag);
			else if (tag instanceof HeadingTag)		process((HeadingTag)tag);
			else if (tag instanceof Html)			process((Html)tag);
			else if (tag instanceof ImageTag)		process((ImageTag)tag);
			else if (tag instanceof JspTag)			this.noParse = true;
			else if (tag instanceof LinkTag)		process((LinkTag)tag);
			else if (tag instanceof MetaTag) 		this.mtm.addMetaTag((MetaTag)tag);
			else if (tag instanceof ParagraphTag)	process((ParagraphTag)tag);
			else if (tag instanceof ScriptTag)		this.noParse = true;
			else if (tag instanceof StyleTag)		this.noParse = true;
			else if (tag instanceof TitleTag) 		process((TitleTag)tag);
			else if (this.debug == Debug.LOW || this.debug == Debug.HIGH)
				System.err.println("missed tag " + tag.getClass().getSimpleName() + ": " + tag.getText());
		} catch (Exception e) { this.exceptions.add(e); }
	}
	
	@Override
	public void visitEndTag(Tag tag) {
		this.noParse = false;
	}
	
	private static void printTagInfo(Tag tag, boolean start) {
		System.err.println("found " + ((start) ? "start" : "end") + "-tag: " + tag.getTagName()
				+ ((Attribute)tag.getAttributesEx().elementAt(0)).getName() + ", "
				+ tag.getClass().getSimpleName()/* + " (assignable from "
				+ clazz.getSimpleName() + ": " + clazz.isAssignableFrom(tag.getClass()) + ")"*/);
	}
	
	private void process(TitleTag tag) {
		this.doc.setTitle(whitespace2Space(HtmlTools.deReplaceHTML(tag.getTitle())));
	}
	
	private void process(ParagraphTag tag) {
		this.doc.addText(whitespace2Space(HtmlTools.deReplaceHTML(tag.getText())));
	}
	
	private void process(LinkTag tag) {
		this.doc.addReference(
				HtmlTools.deReplaceHTML(tag.getLink().trim()),
				whitespace2Space(HtmlTools.deReplaceHTML(tag.getLinkText())));
	}
	
	private void process(HeadingTag tag) {
		this.doc.addHeadline(whitespace2Space(HtmlTools.deReplaceHTML(tag.toPlainTextString())));
	}
	
	private void process(AddressTag tag) {
		this.doc.setAuthor(whitespace2Space(HtmlTools.deReplaceHTML(tag.toPlainTextString())));
	}
	
	private void process(Html tag) {
		String slng = tag.getAttribute("lang");
		if (slng != null)
			addLanguages(slng.split(" "));
		slng = tag.getAttribute("xml:lang");
		if (slng != null)
			addLanguages(slng.split(" "));
	}
	
	private void process(ImageTag tag) {
		this.doc.addReferenceImage(
				HtmlTools.deReplaceHTML(tag.getImageURL()),
				whitespace2Space(HtmlTools.deReplaceHTML(tag.getAttribute("alt"))));
	}
	
	@Deprecated
	private static String whitespace2Space(String text) {
		if (text == null) return null;
		return text.replaceAll("\\s", " ").trim();
	}
}
