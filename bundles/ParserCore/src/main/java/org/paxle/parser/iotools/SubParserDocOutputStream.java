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

package org.paxle.parser.iotools;

import java.io.IOException;
import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.charset.ICharsetDetector;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.parser.ParserNotFoundException;

public class SubParserDocOutputStream extends ParserDocOutputStream {

	private static final Log logger = LogFactory.getLog(SubParserDocOutputStream.class);
	
	private final URI location;
	private final IParserDocument pdoc;
	private final String name;
	
	public SubParserDocOutputStream(ITempFileManager tfm, ICharsetDetector cd, IParserDocument pdoc, URI location, String name) throws IOException {
		super(tfm, cd);
		this.location = location;
		this.pdoc = pdoc;
		this.name = name;
	}
	
	public SubParserDocOutputStream(
			final ITempFileManager tfm,
			final ICharsetDetector cd,
			final IParserDocument pdoc,
			final URI location,
			final String name,
			final long expectedSize) throws IOException {
		super(tfm, cd, expectedSize);
		this.location = location;
		this.pdoc = pdoc;
		this.name = name;
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		String mimeType = null;
		try {
			mimeType = getMimeType(name);
			logger.info(String.format("Parsing sub-doc '%s' (%s) of '%s'", name, mimeType, location));
			this.pdoc.addSubDocument(name, super.parse(location, mimeType));
		} catch (Exception e) {
			if (e instanceof RuntimeException)
				throw (RuntimeException)e;
			// ignore the sub-document if we cannot parse it
			if (logger.isTraceEnabled()) {
				logger.trace(String.format("%s parsing sub-document '%s'", e.getClass().getName(), location), e);
			} else if (e instanceof ParserNotFoundException) {
				logger.info(String.format("Cannot parse sub-doc '%s' of '%s': no parser available for mime-type '%s'", name, location, mimeType));
			} else {
				logger.info(String.format("%s parsing sub-document '%s': %s", e.getClass().getSimpleName(), location, e.getMessage()));
			}
		}
	}
}
