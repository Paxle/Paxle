package org.paxle.mimetype.impl;

import java.io.File;
import java.util.Map;

import net.sf.jmimemagic.MagicDetector;

public class MagicDetectorContainer implements MagicDetector {
	private DetectionHelperManager manager = null;
	
	public MagicDetectorContainer() {
		// TODO Auto-generated constructor stub
	}

	public String getDisplayName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getHandledExtensions() {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getHandledTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] process(byte[] data, int offset, int length, long bitmask,
			char comparator, String mimeType, Map params) {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] process(File file, int offset, int length, long bitmask,
			char comparator, String mimeType, Map params) {
		// TODO Auto-generated method stub
		return null;
	}

}
