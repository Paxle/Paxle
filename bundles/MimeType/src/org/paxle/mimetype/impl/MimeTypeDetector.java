package org.paxle.mimetype.impl;

import java.io.File;
import java.util.Collection;

import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicMatch;

import org.paxle.core.mimetype.IMimeTypeDetector;

public class MimeTypeDetector implements IMimeTypeDetector {

	public String getMimeType(File file) throws Exception {
		try {
			String mimeType = null;
			MagicMatch match = Magic.getMagicMatch(file,false);        

			// if a match was found we can return the new mimeType
			if (match!=null) {
				Collection subMatches = match.getSubMatches();
				if ((subMatches != null) && (!subMatches.isEmpty())) {
					mimeType = ((MagicMatch) subMatches.iterator().next()).getMimeType();
				} else {
					mimeType = match.getMimeType();
				}
			}

			return mimeType;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

}
