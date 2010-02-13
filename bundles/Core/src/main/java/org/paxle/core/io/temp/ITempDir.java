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

package org.paxle.core.io.temp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Represents a directory which is used to store temporary files in it
 */
public interface ITempDir {
	
	/**
	 * Tries to create a new temporary file with the given pre- and suffix in this directory
	 * @param prefix
	 * @param suffix
	 * @return the <code>File</code> object of the new temp file
	 * @throws IOException
	 */
	public File createTempFile(String prefix, String suffix) throws IOException;
	
	/**
	 * @return <code>true</code> if and only if the file is successfully released; <code>false</code> otherwise
	 */
	public boolean releaseTempFile(File file) throws FileNotFoundException, IOException;
}
