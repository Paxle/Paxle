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
	
	/**
	 * Function to detect the <a href="http://www.iana.org/assignments/media-types/">mime-type</a> of a given file.
	 * @param buffer the buffer to inspect
	 * @param logName a meaningful name of the file to print to the log in case an error occurs while inspecting the <code>buffer</code>
	 * @return the detected mime-type or <code>null</code> if the file has an unknonwn type.
	 * @throws Exception
	 */
	public String getMimeType(byte[] buffer, final String logName) throws Exception;
}
