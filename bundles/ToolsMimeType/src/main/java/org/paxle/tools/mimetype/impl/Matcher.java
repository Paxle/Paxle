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
import java.io.IOException;
import java.util.Collections;

import net.sf.jmimemagic.MagicMatch;
import net.sf.jmimemagic.MagicMatcher;
import net.sf.jmimemagic.UnsupportedTypeException;

import org.paxle.tools.mimetype.IDetectionHelper;

public class Matcher extends MagicMatcher {
	private IDetectionHelper helper = null;
	private Match match = new Match();

	public Matcher(String mimeType, IDetectionHelper helper) {
		this.helper = helper;
		this.match.setMimeType(mimeType);
		this.match.setType("detector");
		this.match.setProperties(Collections.EMPTY_MAP);
	}

	@Override
	public void setMatch(MagicMatch match) {}

	@Override
	public void addSubMatcher(MagicMatcher m) {}

	@Override
	public MagicMatch getMatch() {
		return this.match;
	}

	@Override
	public MagicMatch test(byte[] data, boolean onlyMimeMatch) throws IOException, UnsupportedTypeException {
		throw new RuntimeException("Unsupported detection mode.");
	}
	
	@Override
	public MagicMatch test(File f, boolean onlyMimeMatch) throws IOException, UnsupportedTypeException {
		// todo: call the test method of the detection helper class
		String detectedMimeType = this.helper.getMimeType(f);

		// return the result
		if (detectedMimeType == null) return null;
		
		MagicMatch currentMatch = null;
		if (!this.match.getMimeType().equals(detectedMimeType)) {
			currentMatch = this.match.clone();
			currentMatch.setMimeType(detectedMimeType);
		} else {
			currentMatch = this.match;
		}
		return currentMatch;
	}
	
	class Match extends MagicMatch {
		@Override
		public MagicMatch clone() {
			try {
				return (MagicMatch) super.clone();
			} catch (CloneNotSupportedException e) {
				// should ever occure
				return null;
			}
		}
	}
}
