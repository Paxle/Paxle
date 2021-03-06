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

package org.paxle.parser.zip.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.io.IIOTools;
import org.paxle.parser.IParserContext;
import org.paxle.parser.IParserContextLocal;
import org.paxle.parser.ISubParser;
import org.paxle.parser.ParserException;
import org.paxle.parser.iotools.SubParserDocOutputStream;

@Component(metatype=false)
@Service(ISubParser.class)
@Property(name=ISubParser.PROP_MIMETYPES, value={
		"application/zip",
		"application/x-zip",
		"application/x-zip-compressed",
		"application/java-archive"
})
public class ZipParser implements ISubParser {

	@Reference
	protected IParserContextLocal contextLocal;
	
	public IParserDocument parse(URI location, String charset, InputStream is) throws ParserException, UnsupportedEncodingException, IOException 
	{
		final IParserContext context = this.contextLocal.getCurrentContext();		
		final IIOTools iotools = context.getIoTools();
		
		final IParserDocument pdoc = context.createDocument();
		final ZipInputStream zis = new ZipInputStream(is);
		ZipEntry ze;
		while ((ze = zis.getNextEntry()) != null) {
			if (ze.isDirectory()) continue;
			final long size = ze.getSize();
			final SubParserDocOutputStream sos;
			if (size == -1) {
				sos = new SubParserDocOutputStream(
						context.getTempFileManager(),
						context.getCharsetDetector(),
						pdoc, location, ze.getName());
			} else {
				sos = new SubParserDocOutputStream(
						context.getTempFileManager(),
						context.getCharsetDetector(),
						pdoc, location, ze.getName(), size);
			}
			try {				
				iotools.copy(zis, sos, size);						// size == -1 is ok here
			} finally {
				try { sos.close(); } catch (IOException e) {
					if (e.getCause() instanceof ParserException) {
						throw (ParserException)e.getCause();
					} else {
						throw e;
					}
				}
			}
		}
		
		pdoc.setStatus(IParserDocument.Status.OK);
		return pdoc;
	}
	
	public IParserDocument parse(URI location, String charset, File content)
			throws ParserException, UnsupportedEncodingException, IOException {
		final FileInputStream fis = new FileInputStream(content);
		try {
			return parse(location, charset, fis);
		} finally { 
			fis.close(); 
		}
	}
}
