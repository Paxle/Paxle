
package org.paxle.parser.html.impl;

import java.net.URI;

import org.apache.commons.logging.Log;

public class ParserLogger {
	
	private final Log logger;
	private final URI location;
	
	public ParserLogger(final Log logger, final URI location) {
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
	
	public void logError(final String msg, final Exception e) {
		if (logger.isDebugEnabled()) {
			logger.error(String.format("%s while processing '%s'", msg, location), e);
		} else {
			logger.error(String.format("%s while processing '%s': %s", msg, location, e.getMessage()));
		}
	}
	
	public void logInfo(String msg, int tagStart) {
		logger.info(String.format("%s at line %d of %s", msg, Integer.valueOf(tagStart), location));
	}
	
	public void logDebug(final String msg, final int tagStart) {
		logger.debug(String.format("%s at line %d of %s", msg, Integer.valueOf(tagStart), location));
	}
}
