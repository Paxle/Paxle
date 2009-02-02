/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package org.paxle.core.filter.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.metadata.IMetaData;
import org.paxle.core.metadata.IMetaDataProvider;
import org.paxle.core.queue.ICommand;

public class AscendingPathUrlExtractionFilter implements IFilter<ICommand>, IMetaDataProvider {

	private Log logger = LogFactory.getLog(this.getClass());
	
	public void filter(ICommand command, IFilterContext filterContext) {
		
		String url = command.getLocation().toASCIIString();
		IParserDocument doc = command.getParserDocument();
		if (doc == null) {
			logger.warn("ParserDocument is null");
			return;
		}
		
		//We can do this easily, because URL is normalized
		String [] urlsegments = url.split("/");

		int x = 0;
		while (x < urlsegments.length) {
			urlsegments[x] = urlsegments[x] + "/";
			x++;
		}
		
		// http:/ + / + subdomain.host.tld/ 
		String base = urlsegments[0] +  urlsegments[1] +  urlsegments[2];
		
		int append_fields = 0;
		
		try {
			while (append_fields < (urlsegments.length-3)) {
				StringBuffer result = new StringBuffer(base);
				int temp = 0;
				
				while (temp < append_fields) {
					result.append(urlsegments[temp+3]);
					temp++;
				}
				
				doc.addReference(new URI(result.toString()), null, "AscendingPathUrlExtractionFilter");
				logger.debug("Extracted URL " + result +" out of URL " + command.getLocation());
				
				append_fields++;
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	public IMetaData getMetadata(Locale locale) {
		final ResourceBundle rb = ResourceBundle.getBundle("OSGI-INF/l10n/" + AscendingPathUrlExtractionFilter.class.getSimpleName(), locale);
		return new IMetaData() {
			
			public String getDescription() {
				return rb.getString("metadata.desc");
			}
			
			public InputStream getIcon(int size) throws IOException {
				return null;
			}
			
			public String getName() {
				return rb.getString("metadata.name");
			}
			
			public String getVersion() {
				return null;
			}
		};
	}
}
