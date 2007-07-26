package org.paxle.core.mimetype;

import java.io.File;

public interface IMimeTypeDetector {
	public String getMimeType(File file) throws Exception;
}
