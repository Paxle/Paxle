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
package org.paxle.api.jaxrs.logging.impl;

import java.util.HashMap;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.paxle.api.jaxrs.logging.LogResource;
import org.paxle.tools.logging.ILogReader;

@Component
@Service(Object.class)
@Properties({
	@Property(name="javax.ws.rs", boolValue=true, propertyPrivate=true)
})
@Reference(
	name="logReaders",
	referenceInterface=ILogReader.class,
	cardinality=ReferenceCardinality.OPTIONAL_MULTIPLE,
	policy=ReferencePolicy.DYNAMIC,
	bind="addReader",
	unbind="removeReader",
	target="(org.paxle.tools.logging.ILogReader.type=*)"
)
@Path("/log")
public class Logging {
	private HashMap<String, ServiceReference> logReaders = new HashMap<String, ServiceReference>();
	
	/**
	 * The context of this component
	 */
	protected ComponentContext ctx;	
	
	protected void activate(ComponentContext context) {
		this.ctx = context;
	}	
	
	protected void addReader(ServiceReference ref) {
		final String type = (String) ref.getProperty(ILogReader.TYPE);
		this.logReaders.put(type, ref);
	}
	
	protected void removeReader(ServiceReference ref) {
		final String type = (String) ref.getProperty(ILogReader.TYPE);
		this.logReaders.remove(type);
	}
	
	@GET
	public LogResource getLogReader() {
		return this.getLogReader("log4j");
	}
	
	
	@Path("{logType}")
	public LogResource getLogReader(@PathParam("logType") String logType) {
		ServiceReference ref = this.logReaders.get(logType);
		if (ref == null) return null;
		
		final ILogReader logReader = (ILogReader)this.ctx.locateService("logReaders", ref);
		return new LogResource(logReader, logType);
	}
}
