/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2010 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.parser.html.impl;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.htmlparser.PrototypicalNodeFactory;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.lexer.Page;
import org.htmlparser.nodes.RemarkNode;
import org.htmlparser.tags.DoctypeTag;
import org.htmlparser.tags.HeadingTag;
import org.htmlparser.tags.Html;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.JspTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.MetaTag;
import org.htmlparser.tags.ParagraphTag;
import org.htmlparser.tags.ProcessingInstructionTag;
import org.htmlparser.tags.ScriptTag;
import org.htmlparser.tags.StyleTag;
import org.htmlparser.tags.TableTag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.NodeVisitor;

import org.paxle.core.doc.IParserDocument;
import org.paxle.core.doc.LinkInfo;
import org.paxle.core.norm.IReferenceNormalizer;
import org.paxle.parser.html.impl.tags.AbbrTag;
import org.paxle.parser.html.impl.tags.AddressTag;
import org.paxle.parser.html.impl.tags.AreaTag;
import org.paxle.parser.html.impl.tags.BoldTag;
import org.paxle.parser.html.impl.tags.FixedLink;
import org.paxle.parser.html.impl.tags.FixedMetaTag;
import org.paxle.parser.html.impl.tags.ItalicTag;
import org.paxle.parser.html.impl.tags.MetaTagManager;

/**
 * This class provides the callback for the HTML parser's node-iterator.
 * <p>
 *  Everytime the parser encounters a new HTML-tag, the {@link #visitTag(Tag)}-method
 *  is called which then cares about extracting all relevant information from the tag.
 * </p>
 * @see #visitTag(Tag) for a list of tags supported by the {@link NodeCollector}
 * @see #postProcessMeta() for a list of supported META-tag-properties
 */
public class NodeCollector extends NodeVisitor {
	
	/**
	 * The node factory used by the underlying HTML parser to determine the type of a node and
	 * to create the corresponding {@link org.htmlparser.Tag} objects.
	 * <p>Tags additionally used by this implementation besides the inbuilt ones:</p>
	 * <ul>
	 *  <li>{@link org.paxle.parser.html.impl.tags.AddressTag}</li>
	 *  <li>{@link org.paxle.parser.html.impl.tags.BoldTag}</li>
	 *  <li>{@link org.paxle.parser.html.impl.tags.ItalicTag}</li>
	 *  <li>{@link org.paxle.parser.html.impl.tags.FixedLink}</li>
	 *  <li>{@link org.paxle.parser.html.impl.tags.FixedMetaTag}</li>
	 *  <li>{@link org.paxle.parser.html.impl.tags.AbbrTag}</li>
	 *  <li>{@link org.paxle.parser.html.impl.tags.AreaTag}</li>
	 * </ul>
	 */
	public static final PrototypicalNodeFactory NODE_FACTORY = new PrototypicalNodeFactory(false);
	static {
		NODE_FACTORY.registerTag(new AddressTag());
		NODE_FACTORY.registerTag(new BoldTag());
		NODE_FACTORY.registerTag(new ItalicTag());
		NODE_FACTORY.registerTag(new FixedLink());
		NODE_FACTORY.registerTag(new FixedMetaTag());
		NODE_FACTORY.registerTag(new AbbrTag());
		NODE_FACTORY.registerTag(new AreaTag());
	}
	
	private static final HashSet<String> DiscardedTags = new HashSet<String>(Arrays.asList(
			"div", "tr", "td", "th", "li", "ul", "ol", "dt", "dd", "dl", "form", "input", "head",
			"br", "option", "select", "link", "hr", "code", "pre", "sup", "small", "tt", "center",
			"lh", "caption", "label", "span", "body"
	));
	
	private final MetaTagManager mtm;
	private final ParserLogger logger;
	private final Page page;
	private IParserDocument doc;
	private IReferenceNormalizer refNorm;
	
	private boolean noParse = false;
	private boolean noFollowLinks = false;
	private boolean obeyRobotsNoindex = true;
	private boolean obeyRobotsNofollow = true;
	
	public NodeCollector(final IParserDocument doc, final ParserLogger logger, final Page page, final IReferenceNormalizer refNorm) {
		super(true, true);
		this.doc = doc;
		this.logger = logger;
		this.mtm = new MetaTagManager(logger);
		this.page = page;
		this.refNorm = refNorm;
	}
	
	public NodeCollector(final ParserLogger logger, final Page page) {
		super(true, true);
		this.logger = logger;
		this.mtm = new MetaTagManager(logger);
		this.page = page;
	}
	
	public void init(
			final IParserDocument doc,
			final IReferenceNormalizer refNorm,
			final boolean obeyRobotsNoindex,
			final boolean obeyRobotsNofollow) {
		this.mtm.clear();
		this.doc = doc;
		this.refNorm = refNorm;
		this.noParse = false;
		this.noFollowLinks = false;
		this.obeyRobotsNoindex = obeyRobotsNoindex;
		this.obeyRobotsNofollow = obeyRobotsNofollow;
	}
	
	/**
	 * This method is called when the parsing of the whole document is finished, because
	 * then all used META-tags in the HTML-document are collected, which eases processing
	 * of several META-tags which basically have the same meaning.
	 * <p>The following META-tags are used currently:</p>
	 * <dl>
	 *  <dt>The document's language:</dt>
	 *  <dd>{@link MetaTagManager.Names#Content_Language}</dd>
	 *  <dd>{@link MetaTagManager.Names#Language}</dd>
	 *  <dt>The document's author:</dt>
	 *  <dd>{@link MetaTagManager.Names#Author}</dd>
	 *  <dd>{@link MetaTagManager.Names#Creator}</dd>
	 *  <dd>{@link MetaTagManager.Names#Contributor}</dd>
	 *  <dd>{@link MetaTagManager.Names#Publisher}</dd>
	 *  <dt>The document's keywords:</dt>
	 *  <dd>{@link MetaTagManager.Names#Keywords}</dd>
	 *  <dt>The document's abstract:</dt>
	 *  <dd>{@link MetaTagManager.Names#Abstract}</dd>
	 *  <dd>{@link MetaTagManager.Names#Description}</dd>
	 *  <dd>{@link MetaTagManager.Names#Description_Abstract}</dd>
	 *  <dd>{@link MetaTagManager.Names#Description_TableOfContents}</dd>
	 *  <dd>{@link MetaTagManager.Names#Page_Topic}</dd>
	 *  <dd>{@link MetaTagManager.Names#Subject}</dd>
	 *  <dd>{@link MetaTagManager.Names#Title}</dd>
	 *  <dd>{@link MetaTagManager.Names#Title_Alternative}</dd>
	 *  <dt>The document's robots directives:</dt>
	 *  <dd>{@link MetaTagManager.Names#Robots}</dd>
	 * </dl>
	 */
	private void postProcessMeta() {
		// Languages (mustn't contain any entities, so we don't dereplace any)
		final Collection<String> lngs = this.mtm.get(
				MetaTagManager.Names.Content_Language,
				MetaTagManager.Names.Language);
		if (lngs.size() > 0)
			addLanguages(lngs.toArray(new String[lngs.size()]));
		
		// Author(s)
		// TODO: create possibility to set more than only one author
		final String author = this.mtm.getCombined(
				MetaTagManager.Names.Author,
				MetaTagManager.Names.Creator,
				MetaTagManager.Names.Contributor,
				MetaTagManager.Names.Publisher);
		if (author != null && author.length() > 0) {
			this.doc.setAuthor(HtmlTools.deReplaceHTML(author));
		}
		
		// keywords (should be comma-separated)
		final Collection<String> keywordStrings = this.mtm.get(
				MetaTagManager.Names.Keywords);
		if (keywordStrings != null && keywordStrings.size() > 0)
			for (String keywordString : keywordStrings)
				for (final String keyword : keywordString.split(","))
					this.doc.addKeyword(HtmlTools.deReplaceHTML(keyword));
		
		// abstracts
		final Collection<String> abstrcts = this.mtm.get(
				MetaTagManager.Names.Abstract,
				MetaTagManager.Names.Description,
				MetaTagManager.Names.Description_TableOfContents,
				MetaTagManager.Names.Description_Abstract,
				MetaTagManager.Names.Page_Topic,
				MetaTagManager.Names.Subject,
				MetaTagManager.Names.Title,
				MetaTagManager.Names.Title_Alternative);
		if (abstrcts != null && abstrcts.size() > 0)
			for (final String abstrct : abstrcts)
				this.doc.addHeadline(HtmlTools.deReplaceHTML(abstrct));
		
		// refresh meta-tag
		final Collection<String> refreshs = this.mtm.get(MetaTagManager.Names.Refresh);
		if (refreshs != null && refreshs.size() > 0)
			for (final String refresh : refreshs) {
				final String unescaped = HtmlTools.deReplaceHTML(refresh);
				final String absUrl = page.getAbsoluteURL(unescaped, false);
				if (absUrl == null)
					continue;
				final URI uri = refNorm.normalizeReference(absUrl);
				if (uri != null)
					this.doc.addReference(uri, unescaped, "ParserHtml");
			}
		
		// robots meta-tag: noindex / nofollow
		if (this.obeyRobotsNoindex || this.obeyRobotsNofollow) {
			final Collection<String> robots = mtm.get(MetaTagManager.Names.Robots);
			if (robots != null) {
				if (this.obeyRobotsNoindex && robots.contains("noindex"))
					this.doc.toggleFlags(IParserDocument.FLAG_NOINDEX);
				if (this.obeyRobotsNofollow && robots.contains("nofollow"))
					this.doc.toggleFlags(IParserDocument.FLAG_NOFOLLOW);
			}
		}
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
		} else */
		if (!this.noParse) {
			try {
				this.doc.append(HtmlTools.deReplaceHTML(string.getText()));
			} catch (IOException e) {
				logger.logError("Error processing string-node", string.getStartPosition(), e);
			}
		}
	}
	
	@Override
	public void finishedParsing() {
		postProcessMeta();
	}
	
	/**
	 * Each newly discovered {@link Tag} in the HTML's tree causes this method to be called,
	 * which determines the type of the tag and if supported, does one of the following:
	 * <ul>
	 *  <li>
	 *   {@link AddressTag}, {@link HeadingTag}, {@link Html}-tag, {@link ImageTag},
	 *   {@link LinkTag}, {@link ParagraphTag}, {@link TitleTag} -&gt; the corresponding
	 *   {@link #process()}-method is called
	 *  </li>
	 *  <li>
	 *   {@link JspTag}, {@link ScriptTag}, {@link StyleTag} -&gt; the "don't parse the next
	 *   {@link Text}-node"-flag is set (which will be reset on identification of the next
	 *   end-tag)
	 *  </li>
	 *  <li>
	 *   {@link MetaTag} -&gt; the tag is added to the {@link MetaTagManager} for later
	 *   processing
	 *  </li>
	 * </ul>
	 */
	@Override
	public void visitTag(Tag tag) {
		try {
			if (DiscardedTags.contains(tag.getRawTagName().toLowerCase())) {
				return;
			}
			else if (tag instanceof ProcessingInstructionTag) 	process((ProcessingInstructionTag)tag);
			else if (tag instanceof DoctypeTag) 				process((DoctypeTag)tag);
			else if (tag instanceof AddressTag)					{ process((AddressTag)tag); noParse = true; }
			else if (tag instanceof BoldTag)					; // handled by visitStringNode(), TODO: extra weight
		//	else if (tag instanceof DoctypeTag)     			process((DoctypeTag)tag);
			else if (tag instanceof HeadingTag)					{ process((HeadingTag)tag); noParse = true; }
			else if (tag instanceof Html)						process((Html)tag);
			else if (tag instanceof ImageTag)					process((ImageTag)tag);
			else if (tag instanceof ItalicTag)					; // handled by visitStringNode(), TODO: extra weight
			else if (tag instanceof JspTag)						this.noParse = true;
			else if (tag instanceof LinkTag)					process((LinkTag)tag);
			else if (tag instanceof MetaTag) 					process((MetaTag)tag);
			else if (tag instanceof ParagraphTag)				; // handled by visitStringNode()
			else if (tag instanceof RemarkNode)					this.noParse = true;
			else if (tag instanceof ScriptTag)					this.noParse = true;
			else if (tag instanceof StyleTag)					this.noParse = true;
			else if (tag instanceof TableTag)					process((TableTag)tag);
			else if (tag instanceof TitleTag) 					{ process((TitleTag)tag); noParse = true; }
			else if (!tag.isEndTag())
				this.logger.logDebug("missed named tag " + tag.getClass().getSimpleName() + " (" + tag.getRawTagName() + ")", tag.getStartingLineNumber());
		} catch (Throwable e) {
			logger.logError("Error processing named tag '" + tag.getRawTagName() + "'", tag.getStartingLineNumber(), e);
		}
	}
	
	@Override
	public void visitEndTag(Tag tag) {
		final Tag ntag = NODE_FACTORY.get(tag.getTagName());		// otherwise tag is always an instance of TagNode
		if (ntag != null) {
			ntag.setStartPosition(tag.getStartPosition());
			ntag.setEndPosition(tag.getEndPosition());
			if (ntag instanceof AddressTag)			noParse = false;
			else if (ntag instanceof HeadingTag)	noParse = false;
			else if (ntag instanceof JspTag)		noParse = false;
// 			else if (ntag instanceof LinkTag)		noParse = false;
			else if (ntag instanceof RemarkNode)	noParse = false;
			else if (ntag instanceof ScriptTag)		noParse = false;
			else if (ntag instanceof StyleTag)		noParse = false;
			else if (ntag instanceof TitleTag)		noParse = false;
		}
	}
	
	private static final String PI_ENCODING_START = "encoding=\"";
	
	private void process(ProcessingInstructionTag tag) {
		String tagstr = tag.toHtml();
		tagstr = tagstr.substring(2, tagstr.length() - 3);		// '<?xml [..]?>' => 'xml [..]'
		final String[] props = tagstr.split("[ \t\n\r\f]");
		if (props.length < 2)
			return;
		if (!"xml".equals(props[0]))
			return;
		for (int i=1; i<props.length; i++)
			if (props[i].length() > PI_ENCODING_START.length() &&
					props[i].startsWith(PI_ENCODING_START) &&
					props[i].charAt(props[i].length() - 1) == '"') {
				final String encName = props[i].substring(PI_ENCODING_START.length(), props[i].length() - 1).trim();
				try {
					if (Charset.isSupported(encName))
						page.setEncoding(encName);
				} catch (IllegalCharsetNameException e) {
					logger.warning("illegal charset name detected in processing instruction: '" + encName + "'");
				} catch (ParserException e) {
					logger.error("cannot set charset '" + encName + "' requested by document", e);
				}
				break;
			}
	}
	
	private void process(MetaTag tag) {
		this.mtm.addMetaTag(tag);
		if (this.obeyRobotsNofollow && !noFollowLinks) {
			final Collection<String> robotsVals = mtm.get(MetaTagManager.Names.Robots);
			if (robotsVals != null)
				noFollowLinks = robotsVals.contains("nofollow");
		}
	}
	
	private void process(TableTag tag) {
		final String summary = tag.getAttribute("summary");
		if (summary != null)
			this.doc.addKeyword(HtmlTools.deReplaceHTML(summary));
	}
	/*
	private static final Pattern DoctypePattern = Pattern.compile(
			"<!DOCTYPE" +
			"\\s+(\\w+)" +				// the root node this DOCTYPE applies to [1]
	// XXX: the lexer does not seem to be able to handle inline definitions, so we don't need to distinguish here,
	// see http://sourceforge.net/tracker/index.php?func=detail&aid=1785668&group_id=24399&atid=381399
	//		"(" +
				"\\s+PUBLIC" +			// we can only access PUBLIC DOCTYPEs, so there is no need for matching SYSTEM
				"\\s+\"([^\"]+)\"" +	// the formal public identifier of this DOCTYPE [2]
				"\\s+\"([^\"]+)\"" +	// the URI of the DTD [3]
	//		"|" +
	//			"\\s+\\[([^\\]]*)\\]" +	// inline definitions
	//		")" +
			"\\s*>", Pattern.MULTILINE);
	*//*
	private void process(DoctypeTag tag) {
		
		final Matcher m = DoctypePattern.matcher(tag.toHtml());
		if (m.matches()) {
			TODO: process entities
		}
		
	}*/
	
	private void process(TitleTag tag) {
		this.doc.setTitle(HtmlTools.deReplaceHTML(tag.getTitle()));
	}
	
	private void process(LinkTag tag) {
		String link = tag.getLink().trim();
		if (link.length() == 0)
			return;
		if (tag.isJavascriptLink() || tag.isIRCLink() || tag.isMailLink() || link.toLowerCase().startsWith("javascript")) {
			logger.logDebug("ignoring unsupported link '" + link + "'", tag.getStartingLineNumber());
			return;
		}
		
		final URI uri = refNorm.normalizeReference(HtmlTools.deReplaceHTML(link));
		if (uri != null) {
			final LinkInfo linkInfo = new LinkInfo(HtmlTools.deReplaceHTML(tag.getLinkText().trim()), "ParserHtml");
			// TODO: don't set links to "filtered" initially, this is semantically wrong, rather set FLAG_NOFOLLOW in the document
			if (noFollowLinks)
				linkInfo.setStatus(LinkInfo.Status.FILTERED, "forbidden by HTML meta tag");
			this.doc.addReference(uri, linkInfo);
		}
	}
	
	private void process(HeadingTag tag) {
		this.doc.addHeadline(HtmlTools.deReplaceHTML(tag.toPlainTextString()));
	}
	
	private void process(AddressTag tag) {
		this.doc.setAuthor(HtmlTools.deReplaceHTML(tag.toPlainTextString()));
	}
	
	private void process(Html tag) {
		String slng = tag.getAttribute("lang");
		if (slng != null)
			addLanguages(slng.split(" "));
		slng = tag.getAttribute("xml:lang");
		if (slng != null)
			addLanguages(slng.split(" "));
		
		// if the mime-type was not already set we should do it now
		if (this.doc.getMimeType() == null) {
			this.doc.setMimeType("text/html");
		}
	}
	
	private void process(ImageTag tag) {
		final URI uri = refNorm.normalizeReference(HtmlTools.deReplaceHTML(tag.getImageURL()));
		if (uri != null)
			this.doc.addReferenceImage(uri, HtmlTools.deReplaceHTML(tag.getAttribute("alt")));
	}
	
	private void process(DoctypeTag tag) {
		String tagHtml = tag.toTagHtml();		
		if (tagHtml != null && tagHtml.toLowerCase().matches("<!doctype\\s+html")) {
			if (tagHtml.indexOf("xhtml") != -1) {
				this.doc.setMimeType("application/xhtml+xml");
			} else {
				this.doc.setMimeType("text/html");
			}
		}
	}
}
