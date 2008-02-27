package org.paxle.mimetype;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.paxle.core.mimetype.IMimeTypeDetector;

/**
 * A class which implements this interface can be plugged-in into the {@link IMimeTypeDetector} 
 * for mime-type detection.
 */
public interface IDetectionHelper {
	
	/**
	 * Specifies the mime-types supported by this mime-type detector. 
	 * The value of this property can be passed as semicolon-separated string or as array of strings, e.g.:
	 * <pre>
	 * Hashtable<String,String[]> detectorProperties = new Hashtable<String,String[]>();
 	 * List<String> mimeTypes = detector.getMimeTypes();
	 * detectorProperties.put(IDetectionHelper.PROP_MIMETYPES, mimeTypes.toArray(new String[mimeTypes.size()]));
	 * </pre> 
	 */	
	public static final String PROP_MIMETYPES = "MimeTypes";
	
	/**
	 * @return a list of mime-types supported by this mime-type detector
	 */
	public List<String> getMimeTypes();	
	
//	/**
//	 * @return a list of file-extensions supported by this mime-type detector
//	 */
//	public List<String> getFileExtensions();
	
	public String getMimeType(File file) throws IOException;
}
