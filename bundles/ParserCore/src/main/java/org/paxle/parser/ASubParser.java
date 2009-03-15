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
package org.paxle.parser;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.paxle.core.doc.IParserDocument;
import org.paxle.core.io.IOTools;
import org.paxle.core.io.temp.ITempFileManager;

public abstract class ASubParser implements ISubParser {
	
	public IParserDocument parse(URI location, String charset, File content) throws ParserException, UnsupportedEncodingException, IOException {
		InputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(content));			
			return parse(location, charset, in);
		} finally {
			if (in != null) try { in.close(); } catch (Exception e) { throw new ParserException(e); } 
		}
	}
	
	public IParserDocument parse(URI location, String charset, InputStream is) throws ParserException, UnsupportedEncodingException, IOException {
		// getting a reference to the  parser-context
		final ParserContext context = ParserContext.getCurrentContext();
		if (context == null) throw new ParserException("cannot access ParserContext whereas this method must be used from within a sub-parser");
		
		// getting a reference to the temp file manager
		final ITempFileManager tfm = context.getTempFileManager();
		if (tfm == null) throw new ParserException("cannot access temp-file manager");
		
		File content = null;		
		BufferedOutputStream bos = null;
		try {
			// copying data from the stream into the temp file
			content = tfm.createTempFile();
			bos = new BufferedOutputStream(new FileOutputStream(content));
			IOTools.copy(is, bos);			
			bos.close();
			bos = null;
			
			// parsing data
			return parse(location, charset, content);
		} catch (Throwable e) {
			// releasing temp-file
			if (content != null && tfm.isKnown(content)) {
				tfm.releaseTempFile(content);
			}
			
			// re-throw well known exceptions
			if (e instanceof ParserException) throw (ParserException) e;
			else if (e instanceof UnsupportedEncodingException) throw (UnsupportedEncodingException) e;
			else if (e instanceof IOException) throw (IOException) e;
			
			// wrap unknown exceptions into a parser-exception
			throw new ParserException(e);
		} finally { 
			if (bos != null) bos.close(); 
		}
	}
}
