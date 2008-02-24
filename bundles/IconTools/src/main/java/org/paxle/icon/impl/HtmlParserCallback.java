package org.paxle.icon.impl;

import java.io.IOException;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

/**
 * A quick and dirty parser to extract the favicon or icon header from the
 * html metadata, e.g.:<br />
 * <pre>
 * &lt;LINK REL="SHORTCUT ICON" HREF="icons/myIcon.ico"&gt;
 * </pre> 
 */
public class HtmlParserCallback extends HTMLEditorKit.ParserCallback {
	/**
	 * The URL of the web-page favicon (if available)
	 */
	private String faviconURL = null;
    private String faviconType = null;

	private HtmlReader input = null;
	
	public HtmlParserCallback(HtmlReader input) {
		this.input = input;
	}
	
	/**
	 * @see javax.swing.text.html.HTMLEditorKit.ParserCallback#handleStartTag(javax.swing.text.html.HTML.Tag, javax.swing.text.MutableAttributeSet, int)
	 */
	@Override
	public void handleStartTag(HTML.Tag currentTag, MutableAttributeSet a, int pos) {		
		if (currentTag.equals(HTML.Tag.BODY)) {
			try {
				this.input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @see javax.swing.text.html.HTMLEditorKit.ParserCallback#handleEndTag(javax.swing.text.html.HTML.Tag, int)
	 */
	@Override
	public void handleEndTag(HTML.Tag currentTag, int pos) {	
		if (currentTag.equals(HTML.Tag.HEAD)) {
			try {
				this.input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}	
	
	/**
	 * @see javax.swing.text.html.HTMLEditorKit.ParserCallback#handleSimpleTag(javax.swing.text.html.HTML.Tag, javax.swing.text.MutableAttributeSet, int)
	 */
	public void handleSimpleTag(HTML.Tag currentTag, MutableAttributeSet a, int pos) {
		if (currentTag.equals(HTML.Tag.LINK) /* && this.tags.getLast().equals(HTML.Tag.HEAD) */) {
			Object linkRel = a.getAttribute(HTML.Attribute.REL);
			if ((linkRel != null)) {
				if (((String)linkRel).equalsIgnoreCase("shortcut icon")) {
					this.faviconURL = (String) a.getAttribute(HTML.Attribute.HREF);
                    this.faviconType = (String) a.getAttribute(HTML.Attribute.TYPE);
                } else if (((String)linkRel).equalsIgnoreCase("icon")) {
                    this.faviconURL = (String) a.getAttribute(HTML.Attribute.HREF);
                    this.faviconType = (String) a.getAttribute(HTML.Attribute.TYPE);                    
				} 
			}
		}       
	}
	
	public String getFaviconUrl() {
		return this.faviconURL;
	}
	
	public String getFaviconType() {
		return this.faviconType;
	}
}
