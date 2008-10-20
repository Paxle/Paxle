/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 * 
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.parser.lha.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import lha.LhaEntry;
import lha.LhaFile;

import org.paxle.core.charset.ICharsetDetector;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.doc.ParserDocument;
import org.paxle.core.io.IOTools;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.parser.ASubParser;
import org.paxle.parser.ParserContext;
import org.paxle.parser.ParserException;
import org.paxle.parser.iotools.SubParserDocOutputStream;
import org.paxle.parser.lha.ILhaParser;

public class LhaParser extends ASubParser implements ILhaParser {
	
	private static final List<String> MIME_TYPES = Arrays.asList(
			"application/x-lzh-compressed",
			"application/x-lzh-archive",
			"application/lzh",
			"application/x-lzh",
			"application/x-lha",
			"application/x-compress",
			"application/x-compressed",
			"application/x-lzh-archive");
	
	public List<String> getMimeTypes() {
		return MIME_TYPES;
	}
	
	@Override
	public IParserDocument parse(URI location, String charset, File content) throws ParserException, UnsupportedEncodingException, IOException {
		// some helper tools required for parsing
		final ParserContext context = ParserContext.getCurrentContext();
		final ITempFileManager tfm = context.getTempFileManager();
		final ICharsetDetector cd = context.getCharsetDetector();

		// the result object
		final IParserDocument pdoc = new ParserDocument();
		
		// open the file and loop through all entries
		final LhaFile lhaf = new LhaFile(content);
		final Enumeration<?> eenum = lhaf.entries();
		while (eenum.hasMoreElements()) {
			final LhaEntry e = (LhaEntry)eenum.nextElement();
			
			final File ef = e.getFile();
			if (ef.isDirectory()) continue;
			
			final SubParserDocOutputStream spdos = new SubParserDocOutputStream(tfm, cd, pdoc, location, ef.getPath(), e.getOriginalSize());
			final InputStream lis = lhaf.getInputStream(e);
			try {
				IOTools.copy(lis, spdos);
			} finally {
				try { lis.close(); } catch (IOException ex) { /* ignore */ }
				try { spdos.close(); } catch (IOException ex) { /* ignore */ }
			}
		}		
		lhaf.close();
				
		pdoc.setStatus(IParserDocument.Status.OK);		
		return pdoc;
	}
}
