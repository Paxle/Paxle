package org.paxle.parser;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.paxle.core.doc.IParserDocument;

public interface ISubParser {
	public static final String PROP_MIMETYPES = "MimeTypes";

	/**
	 * @return a list of mime-types supported by this sub-parser
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
	public IParserDocument parse(String location, String charset, File content)
			throws ParserException, UnsupportedEncodingException, IOException;
}
