package org.paxle.crawler.proxy.impl;

import java.io.IOException;
import java.net.ConnectException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.useradmin.Authorization;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.xsocket.Execution;
import org.xsocket.ILifeCycle;
import org.xsocket.connection.http.HttpRequest;
import org.xsocket.connection.http.HttpResponse;
import org.xsocket.connection.http.HttpResponseHeader;
import org.xsocket.connection.http.client.HttpClient;
import org.xsocket.connection.http.server.IHttpRequestHandler;
import org.xsocket.connection.http.server.IHttpResponseContext;

public class ProxyRequestHandler implements IHttpRequestHandler, ILifeCycle {

	/**
	 * For logging ...
	 */
	private Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * HTTP-client component to forward {@link HttpRequest}s to a remote server
	 */
	private HttpClient httpClient = null;	
	
	/**
	 * OSGi Service to tracke the {@link UserAdmin} service.
	 */
	private ServiceTracker userAgentTracker = null;
	
	/**
	 * If <code>true</code> the user needs to authenticate itself against the proxy.
	 * Authentication is done via OSGi {@link UserAdmin} service.
	 */
	private boolean enableProxyAuthentication = true;
	
	/**
	 * @param userAgentTracker a {@link ServiceTracker} to track the OSGi {@link UserAdmin} service
	 * @param enableProxyAuthentication if <code>true</code> or <code>null</code> the user needs to authenticate itself against the proxy.<br/>
	 * Authentication is done via OSGi {@link UserAdmin} service.
	 */
	public ProxyRequestHandler(ServiceTracker userAgentTracker, Boolean enableProxyAuthentication) {
		if (userAgentTracker == null) throw new NullPointerException("The user-agent-tracker must not be null.");
		
		this.userAgentTracker = userAgentTracker;
		this.enableProxyAuthentication = (enableProxyAuthentication == null) ? true : enableProxyAuthentication;
	}
	
	/**
	 * @see IHttpRequestHandler#onRequest(HttpRequest, IHttpResponseContext)
	 */
	@Execution(Execution.MULTITHREADED)
	public void onRequest(HttpRequest request, IHttpResponseContext responseCtx) throws IOException {
		try {			
			if (!authenticationUser(request, responseCtx)) {
				this.logger.info(String.format(
						"[%s] Proxy authentication failed. Request was '%s'.", 
						request.getRemoteHost(), 
						request.getRequestURI())
				);
				return;
			}
			
			// remove the named hop-by-hop headers
			request.removeHopByHopHeaders("CONNECTION", "PROXY-AUTHORIZATION", "TRAILER", "UPGRADE");

			// remove accept encoding for now
			request.removeHeader("Accept-Encoding");	      

			// .. and forward the request
			httpClient.send(request, new ProxyResponseHandler(request, responseCtx));
		} catch (ConnectException ce) {
			responseCtx.sendError(502, ce.toString());
		}
	}
	
	/**
	 * Function to authenticate proxy users using the OSGi {@link UserAdmin} service. 
	 * 
	 * @param request 
	 * @param responseCtx
	 * @return <code>true</code> if the user was authenticated successfully.
	 * 
	 * @see org.paxle.gui.impl.HttpContextAuth#USER_HTTP_LOGIN
	 * @see org.paxle.gui.impl.HttpContextAuth#USER_HTTP_PASSWORD
	 */
	boolean authenticationUser(HttpRequest request, IHttpResponseContext responseCtx) {
		if (!this.enableProxyAuthentication) return true;

		try {			
			UserAdmin userAdmin = (UserAdmin) this.userAgentTracker.getService();
			if (userAdmin == null) {
				this.logger.warn(String.format("[%s] OSGi UserAdmin service not found", request.getRemoteHost()));
				this.sendProxyAuthenticateResponse(responseCtx, "OSGi UserAdmin service not found");
				return false;
			}

			String auth = request.getHeader("Proxy-Authorization");
			if (auth == null) {
				logger.info(String.format("[%s] Proxy-authentication needed to access '%s'.", request.getRemoteHost(), request.getRequestURI()));
				this.sendProxyAuthenticateResponse(responseCtx, "Proxy Authentication required");
				return false;
			} else if (auth.length() <= "Basic ".length()) {
				logger.info(String.format("[%s] Invalid proxy-authorization request for URI '%s'.", request.getRemoteHost(), request.getRequestURI()));
				this.sendProxyAuthenticateResponse(responseCtx, "Invalid proxy authorization request");
				return false;
			}

			// base64 decode and get username + password
			byte[] authBytes = Base64.decodeBase64(auth.substring("Basic ".length()).getBytes("UTF-8"));
			auth = new String(authBytes,"UTF-8");		
			String[] authData = auth.split(":");
			String userName = authData[0];
			String password = authData.length==1?"":authData[1];

			User user = userAdmin.getUser("http.login",userName);
			if( user == null ) {
				this.logger.info(String.format("[%s] No user found for username '%s'.", request.getRemoteHost(), userName));	
				this.sendProxyAuthenticateResponse(responseCtx, "Unknown user or invalid password");
				return false;
			}

			if(!user.hasCredential("http.password", password)) {
				this.logger.info(String.format("[%s] Wrong password for username '%s'.", request.getRemoteHost(), userName));
				this.sendProxyAuthenticateResponse(responseCtx, "Unknown user or invalid password");
				return false;
			}

			Authorization authorization = userAdmin.getAuthorization(user);
			if(authorization == null) {
				this.logger.info(String.format("[%s] No authorization found for username '%s'.", request.getRemoteHost(), userName));
				this.sendProxyAuthenticateResponse(responseCtx, "Unknown user or invalid password");
				return false;
			}

//			if (!authorization.hasRole("Administrators")) {
//				// XXX: do we need a special role? 
//			}				
			
			// user authenticated
			return true;
		} catch (Throwable e) {
			String errorMsg = String.format(
					"Unexpected '%s' while trying to authenticate user: %s",
					e.getClass().getName(),
					e.getMessage()
			);
			this.logger.error(errorMsg);
			
			try { this.sendProxyAuthenticateResponse(responseCtx, errorMsg); } catch (Exception ex) { ex.printStackTrace(); }
			return false;
		}
	}
	
	private void sendProxyAuthenticateResponse(IHttpResponseContext responseCtx, String responseMessage) throws IOException {
		HttpResponseHeader responseHeaders = new HttpResponseHeader(407, "text/plain");
		responseHeaders.addHeader("Proxy-Authenticate", "Basic realm=\"Paxle Proxy log-in\"");
		
		HttpResponse response = new HttpResponse(responseHeaders);
		if (responseMessage != null) response.setBodyDataSource(responseMessage);
		
		responseCtx.send(response);
	}

	/**
	 * @see ILifeCycle#onDestroy()
	 */
	public void onDestroy() throws IOException {
		this.httpClient.close();
	}

	/**
	 * @see ILifeCycle#onInit()
	 */
	public void onInit() {
		this.httpClient = new HttpClient();
	}

}
