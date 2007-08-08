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
	 * @see ICharsetDetector
	 */
	public CharsetDetectorOutputStream createOutputStream(OutputStream out) {
		return new CharsetDetectorOutputStream(out);
	}

	/**
	 * {@inheritDoc}
	 * @see ICharsetDetector
	 */	
	public CharsetDetectorInputStream createInputStream(InputStream in) {
		return new CharsetDetectorInputStream(in);
	}

	/**
	 * {@inheritDoc}
	 * @see ICharsetDetector
	 */		
	public String[] getSupportedCharsets() {
		return new nsDetector(nsPSMDetector.ALL).getProbableCharsets();
	}

	/**
	 * {@inheritDoc}
	 * @see ICharsetDetector
	 */		
	public String[] getInspectableMimeTypes() {
		return this.inspectableMimeTypes.toArray(new String[this.inspectableMimeTypes.size()]);
	}

	/**
	 * {@inheritDoc}
	 * @see ICharsetDetector
	 */		
	public boolean isInspectable(String mimeType) {		
		return this.inspectableMimeTypes.contains(mimeType);
	}

}
