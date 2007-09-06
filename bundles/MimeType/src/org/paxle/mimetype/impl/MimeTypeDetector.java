package org.paxle.mimetype.impl;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicMatch;
import net.sf.jmimemagic.MagicMatcher;
import net.sf.jmimemagic.MagicParser;

import org.paxle.core.mimetype.IMimeTypeDetector;
import org.paxle.mimetype.IDetectionHelper;

public class MimeTypeDetector implements IMimeTypeDetector {

	private HashMap<String,Matcher> helpers = new HashMap<String,Matcher>();
	private List<MagicMatcher> matcherList = null;
	
	public MimeTypeDetector(File jMimeMagicFile) {
		try {
			// configure jMimeMagic to use our custom magic file
			if (jMimeMagicFile != null) {		
				// load the class
				Class magicClass = this.getClass().getClassLoader().loadClass("net.sf.jmimemagic.MagicParser");
				
				// change the file name used by jMimeMagic
				Field magicFile = magicClass.getDeclaredField("magicFile");
				magicFile.setAccessible(true);
				magicFile.set(null, jMimeMagicFile.getCanonicalFile().toString());
			}
			
			// init jMimeMagic
			Magic.initialize();
			
			// get the jMimeMagic magic parser object
			Field magicParserField = Magic.class.getDeclaredField("magicParser");
			magicParserField.setAccessible(true);
			MagicParser magicParser = (MagicParser) magicParserField.get(null);
			
			// get the list of registered matchers
			this.matcherList = (List<MagicMatcher>) magicParser.getMatchers();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addDetectionHelper(String mimeType, IDetectionHelper detectionHelper) {
		Matcher matcher = new Matcher(mimeType,detectionHelper);
		this.helpers.put(mimeType, matcher);
		this.matcherList.add(0, matcher);
	}

	public void removeDetectionHelper(String mimeType) {
		if (!this.helpers.containsKey(mimeType)) return;
		
		Matcher matcher = this.helpers.get(mimeType);
		this.matcherList.remove(matcher);
	}
	
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
