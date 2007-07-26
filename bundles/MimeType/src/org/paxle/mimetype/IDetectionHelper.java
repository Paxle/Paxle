package org.paxle.mimetype;

import java.util.List;

public interface IDetectionHelper {
	public static final String PROP_MIMETYPES = "MimeTypes";
	
	/**
	 * @return a list of mime-types supported by this mime-type detector
	 */
	public List<String> getMimeTypes();	
	
	/**
	 * @return a list of file-extensions supported by this mime-type detector
	 */
	public List<String> getFileExtensions();

}
