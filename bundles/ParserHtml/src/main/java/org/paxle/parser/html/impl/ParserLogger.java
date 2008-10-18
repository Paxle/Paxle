
package org.paxle.parser.html.impl;

import java.net.URI;

import org.apache.commons.logging.Log;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.ParserFeedback;

public class ParserLogger implements ParserFeedback {
	
	private final Log logger;
	private URI location;
	
	public ParserLogger(final Log logger, final URI location) {
		this.logger = logger;
		this.location = location;
	}
	
	public ParserLogger(final Log logger) {
		this.logger = logger;
	}
	
	public void setLocation(final URI location) {
		this.location = location;
	}
	
	public void error(String message, ParserException e) {
		logError(message, e);
	}
	
	public void info(String message) {
		logInfo(message);
	}
	
	public void warning(String message) {
		logWarn(message);
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
	
	public void logWarn(String msg) {
		logger.warn(String.format("'%s': %s", location, msg));
	}
	
	public void logInfo(String msg, int tagStart) {
		logger.info(String.format("%s at line %d of %s", msg, Integer.valueOf(tagStart), location));
	}
	
	public void logInfo(String msg) {
		logger.info(String.format("'%s': %s", location, msg));
	}
	
	public void logDebug(final String msg, final int tagStart) {
		logger.debug(String.format("%s at line %d of %s", msg, Integer.valueOf(tagStart), location));
	}
	
	public void logDebug(final String msg) {
		logger.debug(String.format("'%s': %s", location, msg));
	}
}
