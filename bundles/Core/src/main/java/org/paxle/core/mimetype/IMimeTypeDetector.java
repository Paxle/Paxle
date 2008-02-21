package org.paxle.core.mimetype;

import java.io.File;

public interface IMimeTypeDetector {
	/**
	 * Function to detect the <a href="http://www.iana.org/assignments/media-types/">mime-type</a> of a given file.
	 * @param file the file to inspect
	 * @return the detected mime-type or <code>null</code> if the file has an unknonwn type.
	 * @throws Exception
	 */
	public String getMimeType(File file) throws Exception;
}
