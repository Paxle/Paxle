/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2010 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

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
