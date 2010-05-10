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

package org.paxle.parser.tar.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.io.IIOTools;
import org.paxle.parser.ASubParser;
import org.paxle.parser.IParserContext;
import org.paxle.parser.IParserContextLocal;
import org.paxle.parser.ISubParser;
import org.paxle.parser.ParserException;
import org.paxle.parser.iotools.SubParserDocOutputStream;

import com.ice.tar.TarEntry;
import com.ice.tar.TarInputStream;

@Component(metatype=false)
@Service(ISubParser.class)
@Property(name=ISubParser.PROP_MIMETYPES, value={
		"application/x-tar",
		"application/x-gtar",
		"application/x-ustar"
})
public class TarParser extends ASubParser implements ISubParser {
	
	@Reference
	protected IParserContextLocal contextLocal;
	
	@Override
	public IParserDocument parse(URI location, String charset, InputStream is) throws ParserException, UnsupportedEncodingException, IOException {
		final TarInputStream tis = new TarInputStream(is);
		final IParserContext context = this.contextLocal.getCurrentContext();
		final IIOTools iotools = context.getIoTools();
		final IParserDocument pdoc = context.createDocument();
		
		TarEntry te;
		while ((te = tis.getNextEntry()) != null) {
			if (!te.isDirectory()) {
				final SubParserDocOutputStream pdos = new SubParserDocOutputStream(
						context.getTempFileManager(),
						context.getCharsetDetector(),
						pdoc, location, te.getName(), te.getSize());
				try {					
					iotools.copy(tis, pdos, te.getSize());
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
