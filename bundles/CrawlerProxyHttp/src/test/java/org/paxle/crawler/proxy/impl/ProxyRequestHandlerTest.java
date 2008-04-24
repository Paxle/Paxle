package org.paxle.crawler.proxy.impl;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import org.apache.commons.codec.binary.Base64;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.useradmin.Authorization;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.xsocket.connection.http.HttpRequest;
import org.xsocket.connection.http.HttpRequestHeader;
import org.xsocket.connection.http.server.IHttpResponseContext;

public class ProxyRequestHandlerTest extends MockObjectTestCase {
	
	private IHttpResponseContext responseCtx;
	private ProxyRequestHandler handler;
	
	private BundleContext bundelCtx;
	private Bundle bundle;
	
	private ServiceTracker tracker;
	private ServiceReference userAdminRef;	
	private Filter userAdminFilter;
	
	private UserAdmin userAdmin;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		// init osgi stuff
		this.bundle = mock(Bundle.class);
		this.bundelCtx = mock(BundleContext.class);
		this.userAdminFilter = mock(Filter.class);
		this.userAdminRef = mock(ServiceReference.class);
		this.userAdmin = mock(UserAdmin.class);
		this.tracker = new ServiceTracker(this.bundelCtx,this.userAdminFilter,null);
		
		checking(new Expectations(){{
			allowing(bundelCtx).addServiceListener(with(any(ServiceListener.class)),with(any(String.class)));
			allowing(bundelCtx).getService(userAdminRef); will(returnValue(userAdmin));
			
			// return the reference to the userAdmin service
			allowing(bundelCtx).getServiceReferences(with(aNull(String.class)), with(any(String.class)));
			will(returnValue(new ServiceReference[]{userAdminRef}));
			
			allowing(userAdminRef).getBundle(); will(returnValue(bundle));
		}});
		
		this.tracker.open();
		
		// init proxy stuff
		this.responseCtx = mock(IHttpResponseContext.class);
		this.handler = new ProxyRequestHandler(this.tracker, Boolean.TRUE);
	}
	
	private HttpRequest createHttpAuthRequest(String userName, String password) throws UnsupportedEncodingException, URISyntaxException {
		final String userAuth = new String(Base64.encodeBase64((userName + ":" + password).getBytes("UTF-8")),"UTF-8");
		
		HttpRequestHeader reqHeader = new HttpRequestHeader("GET", "http://xxx.yyy");
		reqHeader.addHeader("Proxy-Authorization", "Basic " + userAuth);		
		return new HttpRequest(reqHeader);
	}
	
	private Expectations createExpectation(final String userName, final String password, final boolean hasAuthorization) {		
		final User user = mock(User.class);
		final Authorization auth = mock(Authorization.class);
		
		return new Expectations(){{
			// return the user for the given username
			if (userName != null) {
				allowing(userAdmin).getUser("http.login",userName); will(returnValue(user));
			}
			allowing(userAdmin).getUser(with(any(String.class)), with(any(String.class))); will(returnValue(null));
			
			if (hasAuthorization) {
				allowing(userAdmin).getAuthorization(user); will(returnValue(auth));
			}
			allowing(userAdmin).getAuthorization(with(any(User.class))); will(returnValue(null));
			
			// check if pwd is valid
			if (password != null) {
				allowing(user).hasCredential("http.password", password); will(returnValue(true));
			}
			allowing(user).hasCredential(with(any(String.class)), with(any(String.class))); will(returnValue(false));
			
			ignoring(responseCtx);
		}};
	}
	
	public void testAuthenticationUser() throws URISyntaxException, UnsupportedEncodingException {
		final String userName = "test";
		final String userPwd = "test";
			
		HttpRequest req = this.createHttpAuthRequest(userName, userPwd);
		
		// define username and password
		checking(this.createExpectation(userName, userPwd, true));
		
		boolean ok = this.handler.authenticationUser(req, this.responseCtx);
		assertTrue(ok);
	}

	public void testUnknownUser() throws UnsupportedEncodingException, URISyntaxException {
		HttpRequest req = this.createHttpAuthRequest("xxxx","yyyy");
		checking(this.createExpectation(null, null, true));
		
		boolean ok = this.handler.authenticationUser(req, this.responseCtx);
		assertFalse(ok);
	}
	
	public void testInvalidPassword() throws UnsupportedEncodingException, URISyntaxException {
		HttpRequest req = this.createHttpAuthRequest("xxxx","yyyy");
		checking(this.createExpectation("xxxx", null, true));
		
		boolean ok = this.handler.authenticationUser(req, this.responseCtx);
		assertFalse(ok);
	}
	
	public void testNoAuthorization() throws UnsupportedEncodingException, URISyntaxException {
		HttpRequest req = this.createHttpAuthRequest("xxxx","yyyy");
		checking(this.createExpectation("xxxx", "yyyy", false));
		
		boolean ok = this.handler.authenticationUser(req, this.responseCtx);
		assertFalse(ok);
	}

	public void testNoAuthorizationHeader() throws UnsupportedEncodingException, URISyntaxException {
		HttpRequest req = new HttpRequest("GET", "http://xxx.yyy/");
		checking(new Expectations() {{ ignoring(responseCtx); }});
		
		boolean ok = this.handler.authenticationUser(req, this.responseCtx);
		assertFalse(ok);
	}
}
