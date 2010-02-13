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

package org.paxle.core.charset;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ICharsetDetector {
	public boolean isInspectable(String mimeType);
	public @Nonnull String[] getInspectableMimeTypes();
	
	/**
	 * @return an array containing all detectable charsets.
	 */
	public @Nonnull String[] getSupportedCharsets();
	public @Nonnull ACharsetDetectorOutputStream createOutputStream(@Nonnull OutputStream out);
	public @Nonnull ACharsetDetectorInputStream createInputStream(@Nonnull InputStream in);
	public @Nullable String detectCharset(@Nonnull File file) throws IOException;
}
