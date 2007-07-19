package org.paxle.parser;

import java.util.List;

public interface ISubParser {
	public static final String PROP_MIMETYPES = "MimeTypes";

	/**
	 * @return a list of mime-types supported by this sub-parser
	 */
	public List<String> getMimeTypes();
}
