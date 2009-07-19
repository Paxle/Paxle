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
package org.paxle.charset.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Dictionary;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsPSMDetector;
import org.osgi.service.component.ComponentContext;
import org.paxle.core.charset.ICharsetDetector;

@Component(immediate=true, metatype=true, label="Charset-Detector", description="A component to detect the charset of text files")
@Service(ICharsetDetector.class)
public class CharsetDetector implements ICharsetDetector {
	
	@Property(label="Supported MimeTypes", value="/mimeTypes")
	static final String MIMETYPE_FILE = "org.paxle.charset.impl.CharsetDetector.mimeTypeFile";
	
	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * A list of mime-types this component is capable to inspect
	 */
	protected HashSet<String> inspectableMimeTypes = null;

	protected ComponentContext context;
	
	protected void activate(ComponentContext context) throws MalformedURLException {
		this.context = context;
		
		// getting the service properties
		@SuppressWarnings("unchecked")
		Dictionary<String, Object> props = context.getProperties();
		
		// init this component
		this.activate(props);
	}
		
	protected void activate(Dictionary<String, Object> props) throws MalformedURLException {
		// getting the location of the mime-type file
		String mimeTypeFile = (String) props.get(MIMETYPE_FILE);
		if (mimeTypeFile == null) mimeTypeFile = "/mimeTypes";
		
		URL mimeTypes = null;
		if (mimeTypeFile.startsWith("/")) {			
			mimeTypes = this.context.getBundleContext().getBundle().getEntry("/mimeTypes");
		} else {
			mimeTypes = new URL(mimeTypeFile);
		}
		
		this.inspectableMimeTypes = this.readMimeTypeSet(mimeTypes);
	}

	protected void deactivate(ComponentContext context) {
		this.inspectableMimeTypes.clear();
	}
	
	/**
	 * Read the list of inspectable mime-types from file
	 * @param url the URL of the list
	 * @return a set of inspectable mime-types
	 */
	protected HashSet<String> readMimeTypeSet(URL url) {
		HashSet<String> set = new HashSet<String>();
		
		BufferedReader reader = null;
		try {
			final URLConnection connection = url.openConnection();
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			String line = null;		
			while ((line = reader.readLine())!=null) {
				set.add(line.trim());
			}

			reader.close();
		} catch (Exception e) {
			this.logger.warn("Unable to read inspectable mimeTypes from " + url,e);
		} finally {
			if (reader != null) try { reader.close(); } catch (Exception ignore) {}
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
	
	public String detectCharset(File file) throws IOException {
		CharsetDetectorInputStream is = null;
		try {
			is = createInputStream(new FileInputStream(file));
			byte[] buf = new byte[256];
			
			while (is.read(buf) != -1)
				if (is.charsetDetected())
					return is.getCharset();
			
			return null;
		} finally { if (is != null) is.close(); }
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
