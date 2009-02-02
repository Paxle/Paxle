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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.List;

import org.paxle.core.doc.IParserDocument;

public interface ISubParser {
	/**
	 * Specifies the mime-types supported by this parser. 
	 * The value of this property can be passed as semicolon-separated string or as array of strings, e.g.:
	 * <pre>
	 * Hashtable<String,String[]> parserProperties = new Hashtable<String,String[]>();
 	 * List<String> mimeTypes = parser.getMimeTypes();
	 * parserProperties.put(ISubParser.PROP_MIMETYPES, mimeTypes.toArray(new String[mimeTypes.size()]));
	 * </pre> 
	 */
	public static final String PROP_MIMETYPES = "MimeTypes";

	/**
	 * @return a list of mime-types supported by this sub-parser. 
	 */
	public List<String> getMimeTypes();
	
	/**
	 * Transforms the content of the given file into plain text lacking any format-specifics.
	 * 
	 * @param  location the URI of the resource
	 * @param  charset character set as determined before if possible, otherwise
	 *         <code>charset</code> may be <code>null</code>. If so, a charset detection
	 *         has to be performed using own means
	 * @param  content a file (may be in RAM or on disk) containing the resource's content
	 * @return an {@link IParserDocument} containing all information that could be gathered
	 *         from the resource
	 * @throws <b>ParserException</b> if something goes wrong
	 * @throws <b>UnsupportedEncodingException</b> if the previously detected character set
	 *         doesn't match the file
	 * @throws <b>IOException</b> if an I/O-error occures during reading <code>content</code>
	 */
	public IParserDocument parse(URI location, String charset, File content)
			throws ParserException, UnsupportedEncodingException, IOException;
	
	public IParserDocument parse(URI location, String charset, InputStream is)
			throws ParserException, UnsupportedEncodingException, IOException;
}
