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
package org.paxle.api.jaxrs.json.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.map.ObjectMapper;
import org.paxle.api.jaxrs.cm.BundleResource;
import org.paxle.api.jaxrs.cm.ConfigurationResource;
import org.paxle.api.jaxrs.cm.PropertyResource;

/**
 * @scr.component 
 * @scr.service interface="java.lang.Object"
 * @scr.property name="javax.ws.rs" type="Boolean" value="true" 
 */  
@SuppressWarnings("unchecked")
@Provider 
@Produces("application/json") 
public class ConfigurationWriter implements MessageBodyWriter {
	private static final Class[] SUPPORTED_CLASSES = new Class[] {
		ConfigurationResource.class,
		PropertyResource.class,
		BundleResource.class
	};
	
	public long getSize(Object arg0, Class arg1, Type arg2, Annotation[] arg3, MediaType arg4) {
		return -1;
	}

	public boolean isWriteable(Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		for (Class supportedClass : SUPPORTED_CLASSES) {
			if (supportedClass.isAssignableFrom(type)) return true;
			if (List.class.isAssignableFrom(type) && genericType instanceof ParameterizedType) {
				 ParameterizedType mapType = (ParameterizedType)genericType;
				 Type[] types = mapType.getActualTypeArguments();
				 Class typeClass = (Class) types[0];
				 if (supportedClass.isAssignableFrom(typeClass)) return true;
			}
		}
		return false;
	}

	public void writeTo(Object object, Class type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap headers, OutputStream out) throws IOException, WebApplicationException {
		ObjectMapper jtm = new ObjectMapper();
		jtm.writeValue(out, object);
		out.flush();
	}
}
