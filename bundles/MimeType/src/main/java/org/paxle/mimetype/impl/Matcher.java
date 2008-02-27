package org.paxle.mimetype.impl;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import net.sf.jmimemagic.MagicMatch;
import net.sf.jmimemagic.MagicMatcher;
import net.sf.jmimemagic.UnsupportedTypeException;

import org.paxle.mimetype.IDetectionHelper;

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
