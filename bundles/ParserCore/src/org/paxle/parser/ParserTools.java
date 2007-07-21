package org.paxle.parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.paxle.core.doc.IParserDocument;

public class ParserTools {
	
	/**
	 * Replaces every non-space whitespace character with a space character and truncates
	 * multiple spaces to a single one. This results in the text-representation free of
	 * obscurities.
	 *  
	 * @param text the text to process
	 * @return the words of the given text each separated by only a single space
	 */
	public static String whitespaces2Space(String text) {
		if (text == null) return null;
		return text.replaceAll("\\s", " ").trim();
	}
	
	/**
	 * Determines the character set and MIME type of the given file and then calles
	 * the {@link ISubParser#parse(String, String, File)} of the found sub-parser.
	 * <p>
	 *  This method is intended for usage by archive parsers which have to handle
	 *  multiple, possibly unknown internal files of archive containers.
	 * </p>
	 * 
	 * @see ParserContext#getParser(String)
	 * @see ISubParser#parse(String, String, File)
	 * @param  location name or location of the resource within the container
	 * @param  content {@link File} containing the raw content of the (uncompressed) file
	 * @return a sub-{@link IParserDocument} to add to the main parser-doc as sub-document
	 * @throws <b>ParserException</b> if the selected sub-parser was unable to parse the
	 *         given resource 
	 * @throws <b>IOException</b> if an I/O-error occures during reading <code>content</code>
	 */
	public static IParserDocument parse(String location, File content)
			throws ParserException, IOException {
		final String mimeType = null; // TODO: retrieve MIME type of file
		final String charset = null; // TODO: retrieve character set of file
		final ISubParser sp = ParserContext.getCurrentContext().getParser(mimeType);
		try {
			return sp.parse(location, charset, content);
		} catch (UnsupportedEncodingException e) {
			System.out.println(
					"Character set detection detected wrong charset '"
					+ charset + "' for file: " + content);
		}
		return null;
	}
	
	/**
	 * Copies all data from the given {@link InputStream} to the given {@link OutputStream}
	 * using a buffer of 1024 bytes size.
	 * 
	 * @see InputStream#read(byte[], int, int)
	 * @see OutputStream#write(byte[], int, int)
	 * @param is the stream to read from
	 * @param os the stream to write to
	 * @return the number of copied bytes
	 * @throws <b>IOException</b> if an I/O-error occures
	 */
	public static int copy(InputStream is, OutputStream os) throws IOException {
		final byte[] buf = new byte[1024];
		int rn, rt = 0;
		while ((rn = is.read(buf, 0, 1024)) > -1) {
			os.write(buf, 0, rn);
			rt += rn;
		}
		return rt;
	}
	
	/*
	public static File createTempFile(String location, Class clazz) {
		
	}
	*/
}
