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
package org.paxle.parser.tika.impl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.parser.Parser;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.paxle.parser.ISubParser;

@Component(immediate=true)
@Service(Object.class)
public class ParserManager {
	/**
	 * For logging
	 */
	protected final Log logger = LogFactory.getLog(this.getClass());	
	
	/**
	 * The apache tika configuration
	 */
	protected TikaConfig tikaConfig;
	
	/**
	 * All registered parsers
	 */
	protected List<ServiceRegistration> services = new ArrayList<ServiceRegistration>();
	
	protected void activate(ComponentContext context) throws TikaException {
		BundleContext bc = context.getBundleContext();
		
		// determining all available tika parsers
		this.tikaConfig = TikaConfig.getDefaultConfig();
		
		// loop through all parsers and register them as paxle-parsers
		final Map<String, Parser> parserMap = this.tikaConfig.getParsers();
		if (parserMap != null) {
			for (Entry<String, Parser> parser : parserMap.entrySet()) {
				final String mimeType = parser.getKey();
				final Parser tikaParser = parser.getValue();
				
				// create a paxle parser
				final ParserWrapper paxleParser = this.createPaxleParser(mimeType, tikaParser);
				
				// specify the service properties
				final Hashtable<String, Object> paxleParserProps = new Hashtable<String, Object>();
				paxleParserProps.put(ISubParser.PROP_MIMETYPES, new String[]{mimeType});
				paxleParserProps.put(Constants.SERVICE_PID, tikaParser.getClass().getName()+"_"+mimeType.hashCode());
				
				final ServiceRegistration reg = bc.registerService(ISubParser.class.getName(), paxleParser, paxleParserProps);
				this.services.add(reg);
			}
		}
		
		this.logger.info("Apache Tika framework started");		
	}
	
	void setTikaConfig(TikaConfig tikaConfig) {
		this.tikaConfig = tikaConfig;
	}
	
	Parser getTikaParser(String mimeType) {
		if (this.tikaConfig == null) return null;
		
		// getting all supported parsers
		final Map<String, Parser> parserMap = this.tikaConfig.getParsers();
		return parserMap.get(mimeType);
	}
	
	ParserWrapper createPaxleParser(String mimeType) {
		final Parser tikaParser = this.getTikaParser(mimeType);
		if (tikaParser == null) return null;
		return this.createPaxleParser(mimeType, tikaParser);
	}	
	
	ParserWrapper createPaxleParser(String mimeType, Parser tikaParser) {
		// creating a dummy parser
		final ParserWrapper paxleParser = new ParserWrapper(mimeType, tikaParser);
		return paxleParser;
	}
	
	protected void deactivate(ComponentContext context) {
		for (ServiceRegistration reg : this.services) {
			reg.unregister();		
		}
		this.services.clear();
		
		this.logger.info("Apache Tika framework stopped");
	}
}
