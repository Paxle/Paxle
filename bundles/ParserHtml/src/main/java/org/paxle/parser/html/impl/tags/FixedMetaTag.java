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

package org.paxle.parser.html.impl.tags;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;

import org.htmlparser.tags.MetaTag;
import org.htmlparser.util.EncodingChangeException;
import org.htmlparser.util.ParserException;

@SuppressWarnings("serial")
public class FixedMetaTag extends MetaTag {
	
	@Override
	public void doSemanticAction() throws ParserException {
		try {
	        String httpEquiv = this.getHttpEquiv();
	        if ("Content-Type".equalsIgnoreCase(httpEquiv)) {
	        	String sourceEncoding = this.getPage().getSource().getEncoding();
	        	String metaEncoding = this.getPage().getCharset(this.getAttribute("CONTENT"));
	        	
	        	if (sourceEncoding != null && metaEncoding != null && !sourceEncoding.toLowerCase().equals(metaEncoding.toLowerCase())) {
	    			try {
	    				if (Charset.isSupported(metaEncoding)) {
	    					this.getPage().setEncoding(metaEncoding);
	    				}
	    			} catch (IllegalCharsetNameException e) {
	    				// encoding not supported. leave encoding as is
	    			}			                
	            }
	        }
		} catch (EncodingChangeException e) { /* ignore this */ }
	}
}
