package org.paxle.parser.iotools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;

import org.paxle.core.doc.IParserDocument;
import org.paxle.core.mimetype.IMimeTypeDetector;
import org.paxle.parser.ISubParser;
import org.paxle.parser.ParserContext;
import org.paxle.parser.ParserException;

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
		final IMimeTypeDetector mtd = ParserContext.getCurrentContext().getMimeTypeDetector();
		final String mimeType;
		try {
			mimeType = mtd.getMimeType(content);
		} catch (Exception e) {
			throw new ParserException("error detecting MIME type of file " + content, e);
		}
		
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
	
	public static final int DEFAULT_BUFFER_SIZE_BYTES = 1024;
	public static final int DEFAULT_BUFFER_SIZE_CHARS = DEFAULT_BUFFER_SIZE_BYTES / 2;
	
	public static long copy(Reader in, Appendable out) throws IOException {
		return copy(in, out, -1);
	}
	
	public static long copy(Reader in, Appendable out, long bytes) throws IOException {
        final char[] buf = new char[DEFAULT_BUFFER_SIZE_CHARS];                
        int cs = (int)((bytes > 0 && bytes < (DEFAULT_BUFFER_SIZE_CHARS)) ? bytes : (DEFAULT_BUFFER_SIZE_CHARS));
        
        int rn;
        long rt = 0;
        while ((rn = in.read(buf, 0, cs)) > 0) {
            out.append(CharBuffer.wrap(buf, 0, rn));
            rt += rn;
            
            if (bytes > 0) {
                cs = (int)Math.min(bytes - rt, DEFAULT_BUFFER_SIZE_CHARS);
                if (cs == 0)
                	break;
            }
        }
        return rt;
	}
	
	/**
	 * Copies all data from the given {@link InputStream} to the given {@link OutputStream}
	 * using a buffer of 1024 bytes size.
	 * <p><i>Note: this method does neither close the supplied InputStream nor the OutputStream.</i></p>
	 * 
	 * @see InputStream#read(byte[], int, int)
	 * @see OutputStream#write(byte[], int, int)
	 * @param is the stream to read from
	 * @param os the stream to write to
	 * @return the number of copied bytes
	 * @throws <b>IOException</b> if an I/O-error occures
	 */
	public static long copy(InputStream is, OutputStream os) throws IOException {
		return copy(is, os, -1);
	}
	
	public static long copy(InputStream is, OutputStream os, long bytes) throws IOException {
        final byte[] buf = new byte[DEFAULT_BUFFER_SIZE_BYTES];                
        int cs = (int)((bytes > 0 && bytes < DEFAULT_BUFFER_SIZE_BYTES) ? bytes : DEFAULT_BUFFER_SIZE_BYTES);
        
        int rn;
        long rt = 0;
        while ((rn = is.read(buf, 0, cs)) > 0) {
            os.write(buf, 0, rn);
            rt += rn;
            
            if (bytes > 0) {
                cs = (int)Math.min(bytes - rt, DEFAULT_BUFFER_SIZE_BYTES);
                if (cs == 0)
                	break;
            }
        }
        os.flush();
        return rt;
	}
	
	// from YaCy: de.anomic.plasma.parser.AbstractParser
	public static File createTempFile(String name, Class clazz) throws IOException {
        String parserClassName = clazz.getSimpleName();
                    
        // getting the file extension
        int idx = name.lastIndexOf("/");
        String fileName = (idx != -1) ? name.substring(idx+1) : name;        
        
        idx = fileName.lastIndexOf(".");
        String fileExt = (idx > -1) ? fileName.substring(idx+1) : "";
        
        // creates the temp file
        File tempFile = File.createTempFile(
        		parserClassName + "_" + ((idx > -1) ? fileName.substring(0, idx) : fileName),
        		(fileExt.length() > 0) ? "." + fileExt : fileExt);
        return tempFile;
	}
}
