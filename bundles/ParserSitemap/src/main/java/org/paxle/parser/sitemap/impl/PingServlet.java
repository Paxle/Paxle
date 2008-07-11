package org.paxle.parser.sitemap.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.parser.sitemap.SitemapParser;

public class PingServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final Log logger = LogFactory.getLog(this.getClass());
	private final SitemapParser parser;
	
	public PingServlet(SitemapParser parser) {
		this.parser = parser;
	}
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse rsp) throws IOException {		
		String uriString = null;
		try {
			uriString = req.getParameter("sitemap");
			if (uriString == null || uriString.length() == 0) {
				rsp.setStatus(401);
				return;
			}
			
			// parsing the URI
			URI uri = new URI(uriString);
			
			// TODO downloading and parsing the sitemap
			// TODO: create a new crawling profile
			// TODO: whould we register as data provider?
			
		} catch (URISyntaxException e) {
			this.logger.error(String.format("Invalid syntax: %s", uriString));
			rsp.setStatus(400);
			return;			
		} catch (Throwable e) {
			this.logger.error(String.format(
					"Unexpected '%s' while triggering sitemap parser for URI '%s': %s",
					e.getClass().getName(),
					uriString,
					e.getMessage()
			));
			rsp.setStatus(401);
			return;
		}
	}
}
