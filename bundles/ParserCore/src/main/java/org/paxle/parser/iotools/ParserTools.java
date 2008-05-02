
package org.paxle.parser.iotools;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import org.paxle.core.charset.ACharsetDetectorInputStream;
import org.paxle.core.charset.ICharsetDetector;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.mimetype.IMimeTypeDetector;
import org.paxle.parser.ISubParser;
import org.paxle.parser.ParserContext;
import org.paxle.parser.ParserException;
import org.paxle.parser.ParserNotFoundException;

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
	
	public static String getCharset(File file, String mimeType) throws IOException, ParserException {
		final ParserContext context = ParserContext.getCurrentContext();
		if (context == null)
			throw new ParserException("cannot access ParserContext whereas this method must be used from within a sub-parser");
		return getCharset(file, context.getCharsetDetector(), mimeType);
	}
	
	public static String getCharset(File file, ICharsetDetector cd, String mimeType) throws IOException, ParserException {
		if (cd == null)
			throw new ParserException("no charset detector service available");
		if (mimeType != null && !cd.isInspectable(mimeType))
			throw new ParserException("MIME type '" + mimeType + "' cannot be processed by the charset detector");
		
		final ACharsetDetectorInputStream cdis = cd.createInputStream(new BufferedInputStream(new FileInputStream(file)));
		try {
			final byte[] buf = new byte[1024];
			while (!cdis.charsetDetected() && cdis.read(buf) > -1);
		} finally { cdis.close(); }
		return cdis.getCharset();
	}
	
	public static String getMimeType(File file) throws IOException, ParserException {
		final ParserContext context = ParserContext.getCurrentContext();
		if (context == null)
			throw new ParserException("cannot access ParserContext whereas this method must be used from within a sub-parser");
		return getMimeType(file, context.getMimeTypeDetector());
	}
	
	public static String getMimeType(File file, IMimeTypeDetector mtd) throws IOException, ParserException {
		if (mtd == null)
			throw new ParserException("cannot determine the MIME type of " + file + " due to missing MIME type detector");
		final String mimeType;
		try {
			mimeType = mtd.getMimeType(file);
		} catch (Exception e) {
			if (e instanceof RuntimeException)
				throw (RuntimeException)e;
			throw new ParserException("error detecting MIME type of file " + file, e);
		}
		return mimeType;
	}
	
	public static String getMimeType(final byte[] buf, final String logName) throws IOException, ParserException {
		final ParserContext context = ParserContext.getCurrentContext();
		if (context == null)
			throw new ParserException("cannot access ParserContext whereas this method must be used from within a sub-parser");
		return getMimeType(buf, logName, context.getMimeTypeDetector());
	}
	
	public static String getMimeType(final byte[] buf, final String logName, final IMimeTypeDetector mtd) throws IOException, ParserException {
		if (mtd == null)
			throw new ParserException("cannot determine the MIME type of " + logName + " due to missing MIME type detector");
		final String mimeType;
		try {
			mimeType = mtd.getMimeType(buf, logName);
		} catch (Exception e) {
			if (e instanceof RuntimeException)
				throw (RuntimeException)e;
			throw new ParserException("error detecting MIME type of " + logName, e);
		}
		return mimeType;
	}
	
	/**
	 * Determines the MIME type and character set of the given file and finally calles
	 * the {@link ISubParser#parse(String, String, File)} of the found sub-parser.
	 * <p>
	 *  This method is intended for usage by archive parsers which have to handle
	 *  multiple, possibly unknown internal files of archive containers.
	 * </p>
	 * 
	 * @see #getMimeType(File)
	 * @see #getCharset(File, String)
	 * @see ParserContext#getCurrentContext()
	 * @see ParserContext#getMimeTypeDetector()
	 * @see ParserContext#getCharsetDetector()
	 * @see ParserContext#getParser(String)
	 * @see ISubParser#parse(String, String, File)
	 * @param  location name or location of the resource within the container
	 * @param  content {@link File} containing the raw content of the (uncompressed) file
	 * @return the resulting {@link IParserDocument} (in case of an archive-parser to add to
	 *         the main parser-doc as sub-document)
	 * @throws <b>ParserException</b>:
	 * <ul>
	 *  <li>
	 *   if no {@link ParserContext} is available because this method has not been called
	 *   from the thread executing the {@link ISubParser}
	 *  </li>
	 *  <li>if no MIME type detector object could be retrieved from the {@link ParserContext}</li>
	 *  <li>if a MIME type detection error occured</li>
	 *  <li>if no sub-parser handling the detected MIME type could be found</li>
	 *  <li>
	 *   if the charset has been detected incorrectly and the selected {@link ISubParser} throwed
	 *   an {@link UnsupportedEncodingException}
	 *  </li>
	 *  <li>if the selected {@link ISubParser} was unable to parse the given resource</li>
	 * </ul>
	 * @throws <b>IOException</b> if an I/O-error occures during reading <code>content</code>
	 */
	public static IParserDocument parse(String location, File content) throws ParserException, IOException, URISyntaxException {
		final String mimeType = getMimeType(content);
		if (mimeType == null)
			throw new ParserException("detected MIME type of file " + content + " is null, cannot parse it");
		
		String charset;
		try {
			charset = getCharset(content, mimeType);
		} catch (ParserException e) {
			/* the charset is not "that" important, many formats have pre-defined character sets or save
			 * their charset information internally, so a non-detectable charset is not really bad. The
			 * sub-parser will throw an exception if it needed a previously detected charset. */
			charset = null;
		}
		
		try {
			return parse(new URI(location), mimeType, charset, content);
		} catch (UnsupportedEncodingException e) {
			throw new ParserException("Detected wrong charset '" + charset + "' for file: " + content, e);
		}
	}
	
	public static IParserDocument parse(URI location, String mimeType, String charset, File content) throws ParserException,
			IOException, UnsupportedEncodingException {
		// retrieve the sub-parser for the found MIME type, parse content and return the document
		final ParserContext context = ParserContext.getCurrentContext();
		if (context == null)
			throw new ParserException("cannot access ParserContext whereas this method must be used from within a sub-parser");
		
		if (mimeType == null)
			throw new ParserException("detected MIME type of file " + content + " is null, cannot parse it");
		final ISubParser sp = context.getParser(mimeType);
		if (sp == null)
			throw new ParserNotFoundException(mimeType);
		
		final IParserDocument pdoc = sp.parse(location, charset, content);
		if (pdoc.getMimeType() == null)
			pdoc.setMimeType(mimeType);
		
		return pdoc;
	}
	
	/* ================================================================================
	 * Stream methods
	 * ================================================================================ */
	
	public static IParserDocument parse(URI location, String mimeType, String charset, InputStream content) throws ParserException,
			IOException, UnsupportedEncodingException {
		try {
			// retrieve the sub-parser for the found MIME type, parse content and return the document
			final ParserContext context = ParserContext.getCurrentContext();
			if (context == null)
				throw new ParserException("cannot access ParserContext whereas this method must be used from within a sub-parser");
			
			if (mimeType == null)
				throw new ParserException("detected MIME type of contained file in " + location + " is null, cannot parse it");
			final ISubParser sp = context.getParser(mimeType);
			if (sp == null)
				throw new ParserNotFoundException(mimeType);
			
			final IParserDocument pdoc = sp.parse(location, charset, content);
			if (pdoc.getMimeType() == null)
				pdoc.setMimeType(mimeType);
			
			return pdoc;
		} finally { content.close(); }
	}
}
