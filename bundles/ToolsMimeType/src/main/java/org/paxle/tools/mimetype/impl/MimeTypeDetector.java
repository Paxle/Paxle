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

package org.paxle.tools.mimetype.impl;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicMatch;
import net.sf.jmimemagic.MagicMatchNotFoundException;
import net.sf.jmimemagic.MagicMatcher;
import net.sf.jmimemagic.MagicParser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.ServiceReference;
import org.paxle.core.mimetype.IMimeTypeDetector;
import org.paxle.tools.mimetype.IDetectionHelper;

@Component
@Service(IMimeTypeDetector.class)
@Reference(
	name="detectionHelpers",
	referenceInterface=IDetectionHelper.class,
	cardinality=ReferenceCardinality.OPTIONAL_MULTIPLE,
	policy=ReferencePolicy.DYNAMIC,
	bind="addDetectionHelper",
	unbind="removeDetectionHelper",
	target="(MimeTypes=*)"
)
public class MimeTypeDetector implements IMimeTypeDetector {
	private Log logger = LogFactory.getLog(this.getClass());

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();

	private HashMap<String,Matcher> helpers = new HashMap<String,Matcher>();
	private List<MagicMatcher> matcherList = null;
	
	@SuppressWarnings("unchecked")
	protected void activate(Map<String, Object> props) throws Exception {
		// configure jMimeMagic to use our custom magic file
		
		String jMimeMagicFileName = null;
		if (props != null)
			jMimeMagicFileName = (String) props.get("magicFile");
		if (jMimeMagicFileName != null) {		
			// load the class
			Class<?> magicClass = this.getClass().getClassLoader().loadClass("net.sf.jmimemagic.MagicParser");

			// change the file name used by jMimeMagic
			Field magicFile = magicClass.getDeclaredField("magicFile");
			magicFile.setAccessible(true);
			magicFile.set(null, jMimeMagicFileName);
		}

		// init jMimeMagic
		Magic.initialize();

		// get the jMimeMagic magic parser object
		Field magicParserField = Magic.class.getDeclaredField("magicParser");
		magicParserField.setAccessible(true);
		MagicParser magicParser = (MagicParser) magicParserField.get(null);

		// Get the list of registered matchers
		this.matcherList = (List<MagicMatcher>) magicParser.getMatchers();
	}

	protected void addDetectionHelper(ServiceReference detectionHelperRef) {
		throw new RuntimeException("Not implemented!");
	}
	
	protected void removeDetectionHelper(ServiceReference detectionHelperRef) {
		// TODO: 
	}
	
	public void addDetectionHelper(String mimeType, IDetectionHelper detectionHelper) {
		w.lock();
		try {
			Matcher matcher = new Matcher(mimeType,detectionHelper);
			this.helpers.put(mimeType, matcher);
			this.matcherList.add(0, matcher);
		} finally {
			w.unlock();
		}
	}

	public void removeDetectionHelper(String mimeType) {
		w.lock();
		try {
			if (!this.helpers.containsKey(mimeType)) return;

			Matcher matcher = this.helpers.get(mimeType);
			this.matcherList.remove(matcher);
		} finally {
			w.unlock();
		}
	}

	public String getMimeType(File file) throws Exception {
		r.lock();
		try {
			String mimeType = null;
			MagicMatch match = Magic.getMagicMatch(file,false);        

			// if a match was found we can return the new mimeType
			if (match!=null) {
				Collection<?> subMatches = match.getSubMatches();
				if ((subMatches != null) && (!subMatches.isEmpty())) {
					// if there is a sub-match, use it
					mimeType = ((MagicMatch) subMatches.iterator().next()).getMimeType();
				} else {
					mimeType = match.getMimeType();
				}
			}

			return mimeType;
		} catch (Exception e) {
			if (!(e instanceof MagicMatchNotFoundException)) {
				this.logger.warn(String.format("Unexpected '%s' while trying to determine the mime-type of file '%s'.",
						e.getClass().getName(),
						file.getCanonicalFile().toString()
				),e);
				throw e;
			}
			return null;
		} finally {
			r.unlock();
		}
	}
	
	
	public String getMimeType(final byte[] buffer, final String logFileName) throws Exception {
		r.lock();
		try {
			String mimeType = null;
			MagicMatch match = Magic.getMagicMatch(buffer, false);        

			// if a match was found we can return the new mimeType
			if (match != null) {
				Collection<?> subMatches = match.getSubMatches();
				if ((subMatches != null) && (!subMatches.isEmpty())) {
					// if there is a sub-match, use it
					mimeType = ((MagicMatch) subMatches.iterator().next()).getMimeType();
				} else {
					mimeType = match.getMimeType();
				}
			}

			return mimeType;
		} catch (Exception e) {
			if (!(e instanceof MagicMatchNotFoundException)) {
				this.logger.warn(String.format("Unexpected '%s' while trying to determine the mime-type of file '%s'.",
						e.getClass().getName(),
						logFileName
				),e);
				throw e;
			}
			return null;
		} finally {
			r.unlock();
		}
	}
}
