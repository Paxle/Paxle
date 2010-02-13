/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2010 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.core.mimetype;

import java.io.File;

public interface IMimeTypeDetector {
	
	/**
	 * Function to detect the <a href="http://www.iana.org/assignments/media-types/">mime-type</a> of a given file.
	 * @param file the file to inspect
	 * @return the detected mime-type or <code>null</code> if the file has an unknown type.
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
