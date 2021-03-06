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

public interface ITempFileManager {
	
	public File createTempFile() throws IOException;
	public void releaseTempFile(File file) throws FileNotFoundException, IOException;
	
	/**
	 * @param file 
	 * @return <code>true</code> if the specified temp-file is managed by the temp-file-manager
	 */
	public boolean isKnown(File file);
	
	public void setTempDirFor(ITempDir dir, String... classNames);
	public void removeTempDirFor(String... classNames);
}
