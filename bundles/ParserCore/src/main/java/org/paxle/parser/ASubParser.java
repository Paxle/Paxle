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
			if (in != null) try { in.close(); } catch (Exception e) {/* ignore this */} 
		}
	}
	
	public IParserDocument parse(URI location, String charset, InputStream is) throws ParserException, UnsupportedEncodingException, IOException {
		final ParserContext context = ParserContext.getCurrentContext();
		if (context == null) throw new ParserException("cannot access ParserContext whereas this method must be used from within a sub-parser");
		
		final ITempFileManager tfm = context.getTempFileManager();
		if (tfm == null) throw new ParserException("cannot access temp-file manager");
		
		final File content = tfm.createTempFile();
		final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(content));
		try {
			IOTools.copy(is, bos);
		} finally { 
			bos.close(); 
		}
		
		return parse(location, charset, content);
	}
}
