/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 * 
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.charset.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsPSMDetector;
import org.paxle.core.charset.ICharsetDetector;

public class CharsetDetector implements ICharsetDetector {
	private Log logger = LogFactory.getLog(this.getClass());
	private HashSet<String> inspectableMimeTypes = null;

	public CharsetDetector(URL mimeTypeList) {
		this.inspectableMimeTypes = this.readMimeTypeSet(mimeTypeList);
	}

	/**
	 * Read the list of inspectable mime-types from file
	 * @param url the URL of the list
	 * @return a set of inspectable mime-types
	 */
	private HashSet<String> readMimeTypeSet(URL url) {
		HashSet<String> set = new HashSet<String>();
		try {
			URLConnection connection = url.openConnection();
			InputStream connectionIn = connection.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(connectionIn));

			String line = null;		
			while ((line = reader.readLine())!=null) {
				set.add(line.trim());
			}

			reader.close();
			connectionIn.close();
		} catch (Exception e) {
			this.logger.warn("Unable to read inspectable mimeTypes from " + url,e);
		}

		return set;
	}

	/**
	 * {@inheritDoc}
	 * @see ICharsetDetector#createOutputStream(OutputStream)
	 */
	public CharsetDetectorOutputStream createOutputStream(OutputStream out) {
		return new CharsetDetectorOutputStream(out);
	}

	/**
	 * {@inheritDoc}
	 * @see ICharsetDetector#createInputStream(InputStream)
	 */	
	public CharsetDetectorInputStream createInputStream(InputStream in) {
		return new CharsetDetectorInputStream(in);
	}

	/**
	 * {@inheritDoc}
	 * @see ICharsetDetector#getSupportedCharsets()
	 * @see nsDetector#getProbableCharsets()
	 */		
	public String[] getSupportedCharsets() {
		return new nsDetector(nsPSMDetector.ALL).getProbableCharsets();
	}

	/**
	 * {@inheritDoc}
	 * @see ICharsetDetector#getInspectableMimeTypes()
	 */		
	public String[] getInspectableMimeTypes() {
		return this.inspectableMimeTypes.toArray(new String[this.inspectableMimeTypes.size()]);
	}

	/**
	 * {@inheritDoc}
	 * @see ICharsetDetector#isInspectable(String)
	 */		
	public boolean isInspectable(String mimeType) {		
		return this.inspectableMimeTypes.contains(mimeType);
	}

}
