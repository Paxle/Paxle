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

package org.paxle.parser.lha.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Iterator;

import net.sourceforge.lhadecompressor.LhaEntry;
import net.sourceforge.lhadecompressor.LhaFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.paxle.core.charset.ICharsetDetector;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.io.IIOTools;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.parser.ASubParser;
import org.paxle.parser.IParserContext;
import org.paxle.parser.IParserContextLocal;
import org.paxle.parser.ISubParser;
import org.paxle.parser.ParserException;
import org.paxle.parser.iotools.SubParserDocOutputStream;

@Component(metatype=false)
@Service(ISubParser.class)
@Property(name=ISubParser.PROP_MIMETYPES, value={
		"application/x-lzh-compressed",
		"application/x-lzh-archive",
		"application/lzh",
		"application/x-lzh",
		"application/x-lha",
		"application/x-compress",
		"application/x-compressed",
		"application/x-lzh-archive"
})
public class LhaParser extends ASubParser implements ISubParser {
	/**
	 * For logging
	 */
	protected Log logger = LogFactory.getLog(this.getClass());
	
	@Reference
	protected IParserContextLocal contextLocal;
	
	@Override
	public IParserDocument parse(URI location, String charset, File content) throws ParserException, UnsupportedEncodingException, IOException {
		// some helper tools required for parsing
		final IParserContext context = this.contextLocal.getCurrentContext();
		final ITempFileManager tfm = context.getTempFileManager();
		final ICharsetDetector cd = context.getCharsetDetector();
		final IIOTools iotools = context.getIoTools();

		// the result object
		final IParserDocument pdoc = context.createDocument();
		
		// open the file and loop through all entries
		final LhaFile lhaf = new LhaFile(content);
		
		@SuppressWarnings("unchecked")
		final Iterator<LhaEntry> eiter = lhaf.entryIterator();
		while (eiter.hasNext()) {
			final LhaEntry e = eiter.next();
			
			final File ef = e.getFile();
			if (ef.isDirectory()) continue;
			
			final SubParserDocOutputStream spdos = new SubParserDocOutputStream(tfm, cd, pdoc, location, ef.getPath(), e.getOriginalSize());
			final InputStream lis = lhaf.getInputStream(e);
			try {
				iotools.copy(lis, spdos);
			} finally {
				try { lis.close();   } catch (IOException ex) { this.logger.error(e); }
				try { spdos.close(); } catch (IOException ex) { this.logger.error(e); }
			}
		}		
		lhaf.close();
				
		pdoc.setStatus(IParserDocument.Status.OK);		
		return pdoc;
	}
}
