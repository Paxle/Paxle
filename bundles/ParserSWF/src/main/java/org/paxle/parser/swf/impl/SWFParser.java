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

package org.paxle.parser.swf.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.karlchenofhell.swf.TextExtractorTagFactory;
import org.karlchenofhell.swf.TextSink;
import org.karlchenofhell.swf.parser.SWFTagReader;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.io.IIOTools;
import org.paxle.parser.ASubParser;
import org.paxle.parser.IParserContext;
import org.paxle.parser.IParserContextLocal;
import org.paxle.parser.ISubParser;
import org.paxle.parser.ParserException;

@Component(metatype=false)
@Service(ISubParser.class)
@Property(name=ISubParser.PROP_MIMETYPES, value={"application/x-shockwave-flash"})
public class SWFParser extends ASubParser implements ISubParser {
	private static final Charset UTF8 = Charset.forName("UTF-8");
	
	private final Log logger = LogFactory.getLog(SWFParser.class);
	
	@Reference
	protected IParserContextLocal contextLocal;	
	
	@Override
	public IParserDocument parse(final URI location, String charset, InputStream is)
			throws ParserException, UnsupportedEncodingException, IOException {
		
		final IParserContext context = this.contextLocal.getCurrentContext();
		
		final IIOTools iotools = context.getIoTools();

		final IParserDocument pdoc = context.createDocument();	
		
		final class SwfTextSink implements TextSink {
			
			Exception ex;
			
			private final ISubParser htmlParser;
			private boolean warnedNoHtmlParser = false;
			
			public SwfTextSink() {
				htmlParser = context.getParser("text/html");
			}
			
			public void addText(String text, boolean isHTML) {
				try {
					if (!isHTML) {
						pdoc.append(text).append(' ');
						return;
					}
					if (htmlParser == null) {
						if (!warnedNoHtmlParser) {
							logger.warn("Cannot parse HTML content of SWF-file due to missing HTML-parser");
							warnedNoHtmlParser = true;
						}
						return;
					}
					final ByteBuffer bb = UTF8.encode(text);
					final IParserDocument htmlParserDoc = htmlParser.parse(
							location,
							UTF8.name(),
							new ByteArrayInputStream(bb.array(), 0, bb.limit()));
					if (htmlParserDoc.getStatus() != IParserDocument.Status.OK) {
						logger.warn("Failed parsing HTML-content of SWF-file from '" + location + "': " + htmlParserDoc.getStatusText());
					} else {
						Reader reader = null;
						try {
							reader = htmlParserDoc.getTextAsReader();
							if (reader != null) {
								iotools.copy(reader, pdoc);
								pdoc.append(' ');
							}
						} finally {
							if (reader != null) reader.close();
						}
					}
				} catch (Exception e) { ex = e; }
			}
		}
		
		final SwfTextSink sink = new SwfTextSink();
		final TextExtractorTagFactory tetf = new TextExtractorTagFactory(sink);
		try {
			SWFTagReader.processAll(location.toString(), is, tetf);
			if (sink.ex != null)
				throw new ParserException("error parsing '" + location + "'", sink.ex);
		} finally { pdoc.close(); }
		pdoc.setStatus(IParserDocument.Status.OK);
		return pdoc;
	}
}
