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
package org.paxle.api.jaxrs.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.ws.rs.Path;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.useradmin.Authorization;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;
import org.wymiwyg.wrhapi.Handler;
import org.wymiwyg.wrhapi.HandlerException;
import org.wymiwyg.wrhapi.HeaderName;
import org.wymiwyg.wrhapi.MessageBody;
import org.wymiwyg.wrhapi.Request;
import org.wymiwyg.wrhapi.Response;
import org.wymiwyg.wrhapi.ResponseStatus;
import org.wymiwyg.wrhapi.filter.Filter;

@Component(immediate=true)
@Service(Filter.class)
@Reference(
	name="resources",
	referenceInterface=Object.class,
	cardinality=ReferenceCardinality.OPTIONAL_MULTIPLE,
	policy=ReferencePolicy.DYNAMIC,
	bind="addResource",
	unbind="removeResource",
	target="(&(javax.ws.rs=true)(org.paxle.api.protected=true))"
)
public class AuthenticatingFilter implements Filter {
	public static final String USER_HTTP_PASSWORD = "http.password";
	public static final String USER_HTTP_LOGIN = "http.login";

	@Reference
	protected UserAdmin userAdmin;
	
	/**
	 * A list containing all registered jax-rs root resources
	 */
	private final List<ServiceReference> resources = new ArrayList<ServiceReference>();
	
	/**
	 * Request-path prefixes that should be protected with a username + password
	 */
	private final List<String> protectedPaths = new CopyOnWriteArrayList<String>();	
	
	/**
	 * The context of this component
	 */
	protected ComponentContext ctx;		
	
	/**
	 * for logging
	 */
	private final Log logger = LogFactory.getLog(this.getClass());

	protected synchronized void activate(ComponentContext context) {
		this.ctx = context;	
		for (ServiceReference ref : resources) {
			String path = this.getPath(ref);
			if (path != null) this.protectedPaths.add(path);
		}
	}
	
	/**
	 * @param ref a reference to the registered service
	 * @return the path of a jax-rs service configured via {@link Path} annotation
	 * TODO: what about path-prefixes managed via {@link org.trialox.triaxrs.prefixmanager.TriaxrsPrefixManager}?
	 */
	protected String getPath(ServiceReference ref) {
		// getting the jax-rs root-resource
		Object resource = this.ctx.locateService("resources",ref);
		if (resource == null) return null;
		
		// getting the configured path
		Path path = resource.getClass().getAnnotation(Path.class);
		return (path == null) ? null : path.value();
	}
		
	/**
	 * A new jax-rs root resource was registered
	 * @param ref a reference to the registered service
	 */
	protected synchronized void addResource(ServiceReference ref) {
		this.resources.add(ref);
		if (this.ctx != null) {
			String path = this.getPath(ref);
			if (path != null) this.protectedPaths.add(path);
		}
	}
	
	/**
	 * A jax-rs root resource was unregistered
	 * @param ref a reference to the registered service
	 */
	protected synchronized void removeResource(ServiceReference ref) {
		this.resources.remove(ref);
		
		String path = this.getPath(ref);
		if (path != null) this.protectedPaths.remove(path);
	}
	
	/**
	 * Function to send a 401 response back to the client
	 * @param response
	 * @param message the error message body
	 * @throws HandlerException
	 */
	private void setUnauthorizedResponse(final Response response, String message) throws HandlerException {
		try {
			response.setResponseStatus(ResponseStatus.UNAUTHORIZED);
			response.addHeader(HeaderName.WWW_AUTHENTICATE, "Basic realm=\"Paxle JaxRS-PAI authentication needed\"");
			response.setHeader(HeaderName.CONTENT_LENGTH, message.getBytes("UTF-8").length);
			response.setBody(new ErrorMsgBody(message));
		} catch (UnsupportedEncodingException e) {
			this.logger.error(e);
		}
}	
	
	public void handle(Request request, Response response, Handler handler) throws HandlerException {
		try {
			String path = request.getRequestURI().getPath();
			for(String protectedPath : this.protectedPaths) {
				if (path.startsWith(protectedPath)) {
					User user = null;
					
					// getting the http-auth header
					String[] auth = request.getHeaderValues(HeaderName.AUTHORIZATION);
					if (auth != null && auth.length == 1) {
						user = this.httpAuth(request, auth[0]);
					}
					
					if (user == null) {
						this.setUnauthorizedResponse(response, "Authentication required");
						return;
					}
				}
			}
			
			handler.handle(request, response);
		} catch (UnsupportedEncodingException e) {
			this.logger.error(e);
		}
	}
	
	private User httpAuth(Request request, String auth) throws UnsupportedEncodingException, HandlerException {		
		if (auth == null || auth.length() <= "Basic ".length()) return null;

		// base64 decode and get username + password
		byte[] authBytes = Base64.decodeBase64(auth.substring("Basic ".length()).getBytes("UTF-8"));
		auth = new String(authBytes,"UTF-8");		
		String[] authData = auth.split(":");
		if (authData.length == 0) {
			this.logger.info(String.format("[%s] No user-authentication data found to access '%s'.", request.getRemoteHost(), request.getRequestURI()));
			return null;
		}
		
		String userName = authData[0];
		String password = authData.length==1?"":authData[1];
	
		return authenticatedAs(request, userName, password);
	}	
	
	private User authenticatedAs(Request request, String userName, String password) throws UnsupportedEncodingException, HandlerException {
		if ("http.login" == null) {
			this.logger.info(String.format("[%s] OSGi UserAdmin service not found", request.getRemoteHost()));
			return null;
		}

		User user = userAdmin.getUser(USER_HTTP_LOGIN,userName);
		if( user == null ) {
			this.logger.info(String.format("[%s] No user found for username '%s'.", request.getRemoteHost(), userName));	
			return null;
		}

		if(!user.hasCredential(USER_HTTP_PASSWORD, password)) {
			this.logger.info(String.format("[%s] Wrong password for username '%s'.", request.getRemoteHost(), userName));
			return null;
		}

		Authorization authorization = userAdmin.getAuthorization(user);
		if(authorization == null) {
			this.logger.info(String.format("[%s] No authorization found for username '%s'.", request.getRemoteHost(), userName));
			return null;
		}
		
		// XXX: do we need a special role here?
		// if (!authorization.hasRole("Administrators")) {
		//
		// }

		return user;		
	}	
	
	private static class ErrorMsgBody implements MessageBody {
		private String error;
		
		public ErrorMsgBody(String error) {
			this.error = error;
		}
		
		public ReadableByteChannel read() throws IOException {
			final InputStream pipedIn = new ByteArrayInputStream(error.getBytes("UTF-8"));
			return Channels.newChannel(pipedIn);
		}

		public void writeTo(WritableByteChannel out) throws IOException {
			PrintWriter writer = new PrintWriter(Channels.newWriter(out, "utf-8"));
			writer.append(this.error);
			writer.flush();
		}
	}
}
