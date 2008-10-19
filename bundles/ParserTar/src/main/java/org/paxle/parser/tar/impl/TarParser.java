/*
This file is part of the Paxle project.
Visit http://www.paxle.net for more information.
Copyright 2007-2008 the original author or authors.

Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
or in the file LICENSE.txt in the root directory of the Paxle distribution.

Unless required by applicable law or agreed to in writing, this software is distributed
on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*/

package org.paxle.parser.tar.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.paxle.core.doc.IParserDocument;
import org.paxle.core.doc.ParserDocument;
import org.paxle.core.io.IOTools;
import org.paxle.parser.ASubParser;
import org.paxle.parser.ParserContext;
import org.paxle.parser.ParserException;
import org.paxle.parser.iotools.SubParserDocOutputStream;
import org.paxle.parser.tar.ITarParser;

import com.ice.tar.TarEntry;
import com.ice.tar.TarInputStream;

public class TarParser extends ASubParser implements ITarParser {
	
	private static final String[] MIMETYPES = {
		"application/x-tar",
		"application/x-gtar",
		"application/x-ustar",
		/* FIXME: http://filext.com/file-extension/TAR additionally lists the following. Are these valid?
		 *	multipart/x-tar 
		 *	application/x-compress 
		 *	application/x-compressed
		 */
	};
	
	public List<String> getMimeTypes() {
		return Arrays.asList(MIMETYPES);
	}
	
	@Override
	public IParserDocument parse(URI location, String charset, InputStream is)
			throws ParserException, UnsupportedEncodingException, IOException {
		final TarInputStream tis = new TarInputStream(is);
		final IParserDocument pdoc = new ParserDocument();
		final ParserContext context = ParserContext.getCurrentContext();
		
		TarEntry te;
		while ((te = tis.getNextEntry()) != null) {
			if (!te.isDirectory()) {
				final SubParserDocOutputStream pdos = new SubParserDocOutputStream(
						context.getTempFileManager(),
						context.getCharsetDetector(),
						pdoc, location, te.getName(), te.getSize());
				try {
					IOTools.copy(tis, pdos, te.getSize());
				} finally {
					try { pdos.close(); } catch (IOException e) {
						if (e.getCause() instanceof ParserException) {
							throw (ParserException)e.getCause();
						} else {
							throw e;
						}
					}
				}
			}
		}
		pdoc.setStatus(IParserDocument.Status.OK);
		return pdoc;
	}
}
