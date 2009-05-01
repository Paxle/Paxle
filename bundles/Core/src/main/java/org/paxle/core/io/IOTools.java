/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package org.paxle.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;

import org.paxle.core.io.temp.ITempFileManager;

/**
 * @deprecated please fetch an instance of this tool via 
 * the interface {@link IIOTools} from the OSGi framework instead 
 */
public class IOTools {
	private static IIOTools iotools = new org.paxle.core.io.impl.IOTools();
	
	private static ITempFileManager tempFileManager = null;
	
	public static void setTempFileManager(ITempFileManager manager) {
		tempFileManager = manager;
	}
	
	/**
	 * @deprecated
	 */
	public static ITempFileManager getTempFileManager() {
		return tempFileManager;
	}
	
	public static long copy(Reader in, Appendable out) throws IOException {
		return iotools.copy(in, out);
	}
	
	public static long copy(Reader in, Appendable out, long chars) throws IOException {
		return iotools.copy(in, out, chars);
	}
	
	public static long copy(InputStream is, OutputStream os) throws IOException {		
		return iotools.copy(is, os);
	}
	
	public static long copy(InputStream is, OutputStream os, long bytes) throws IOException {
		return iotools.copy(is, os, bytes);
	}
	
	public static long copy(final InputStream is, final OutputStream os, final long bytes, final int limitKBps) throws IOException {
		return iotools.copy(is, os, bytes, limitKBps);
	}
}
