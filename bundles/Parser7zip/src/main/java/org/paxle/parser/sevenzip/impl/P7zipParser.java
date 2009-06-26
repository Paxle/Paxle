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
package org.paxle.parser.sevenzip.impl;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.parser.ASubParser;
import org.paxle.parser.IParserContext;
import org.paxle.parser.ISubParser;
import org.paxle.parser.ParserContext;
import org.paxle.parser.ParserException;

import SevenZip.Archive.SevenZip.Handler;

@Component(metatype=false)
@Service(ISubParser.class)
@Property(name=ISubParser.PROP_MIMETYPES, value={"application/x-7z-compressed"})
public class P7zipParser extends ASubParser implements ISubParser {
	
	public static final List<String> MIME_TYPES = Arrays.asList("application/x-7z-compressed");
	
	@Override
	public IParserDocument parse(URI location, String charset, File content) throws
			ParserException, UnsupportedEncodingException, IOException {
		final Handler archive = new Handler();
		archive.Open(new RAFInStream(content));
		
		final IParserContext context = ParserContext.getCurrentContext();
		final ITempFileManager tfm = context.getTempFileManager();
		final IParserDocument doc = context.createDocument();
		final SZParserExtractCallback aec = new SZParserExtractCallback(location, doc, archive, tfm, context.getCharsetDetector());
		try {
			archive.Extract(null, -1, 0, aec);
			doc.setStatus(IParserDocument.Status.OK);
		} finally { 
			archive.close();
		}
		return doc;
	}
}
