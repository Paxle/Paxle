
package org.paxle.parser.html.impl;

import org.apache.commons.logging.Log;

public class ParserLogger {
	
	private final Log logger;
	private final String location;
	
	public ParserLogger(final Log logger, final String location) {
		this.logger = logger;
		this.location = location;
	}
	
	public void logError(final String msg, final int tagStart) {
		logger.error(String.format("%s at %d of %s", msg, Integer.valueOf(tagStart), location));
	}
	
	public void logError(String msg, final int tagStart, final Exception e) {
		final Integer tagStartValue = Integer.valueOf(tagStart);
		if (e != null && logger.isDebugEnabled()) {
			logger.error(String.format("%s: %s at %d of %s", msg, e.getMessage(), tagStartValue, location), e);
		} else {
			logger.error(String.format("%s at %d of %s", msg, tagStartValue, location));
		}
	}
	
	public void logInfo(String msg, int tagStart) {
		logger.info(String.format("%s at line %d of %s", msg, Integer.valueOf(tagStart), location));
	}
	
	public void logDebug(final String msg, final int tagStart) {
		logger.debug(String.format("%s at line %d of %s", msg, Integer.valueOf(tagStart), location));
	}
}
