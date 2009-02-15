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
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeReference;
import org.paxle.api.jaxrs.cm.PropertyResource;

/**
 * @scr.component 
 * @scr.service interface="java.lang.Object"
 * @scr.property name="javax.ws.rs" type="Boolean" value="true" 
 */  
@SuppressWarnings("unchecked")
@Provider 
@Consumes("application/json") 
public class ConfigurationReader implements MessageBodyReader {
	private static final Class[] SUPPORTED_CLASSES = new Class[] {
		PropertyResource.class
	};

	public boolean isReadable(Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
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

	public Object readFrom(Class type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {		
		// reading List<PropertyResource>
		if (List.class.isAssignableFrom(type) && genericType instanceof ParameterizedType) {
			ParameterizedType mapType = (ParameterizedType)genericType;
			Type[] types = mapType.getActualTypeArguments();
			Class typeClass = (Class) types[0];
			if (PropertyResource.class.isAssignableFrom(typeClass)) return readPropertyList(entityStream);
		}
		
		return null;
	}
		
	private List<PropertyResource> readPropertyList(InputStream entityStream) throws JsonParseException, IOException {		
		ObjectMapper om = new ObjectMapper();
		return om.readValue(entityStream, new TypeReference<List<PropertyResource>>(){});		
	}
}
