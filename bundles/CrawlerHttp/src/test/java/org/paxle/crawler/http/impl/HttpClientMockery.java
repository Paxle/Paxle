package org.paxle.crawler.http.impl;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.lib.legacy.ClassImposteriser;

public class HttpClientMockery extends Mockery {
	public HttpClientMockery() {
		super();		 
	}

	private HttpConnectionManager getConnectionManager(final InputStream bodyStream) throws IOException {	
		setImposteriser(ClassImposteriser.INSTANCE);
		
		// create mocks
		final HttpConnectionManager connectionManager = mock(HttpConnectionManager.class);
		final HttpConnection connection = mock(HttpConnection.class);		
		final HttpConnectionManagerParams managerParams = mock(HttpConnectionManagerParams.class);
		final HttpConnectionParams connectionParams = mock(HttpConnectionParams.class);

		// set expectation
		checking(new Expectations(){{
			// return dummy connection-manager params if requested
			allowing(connectionManager).getParams(); 
			will(returnValue(managerParams));

			// allow to set HTTP-parameters
			allowing(managerParams).setDefaults(with(any(org.apache.commons.httpclient.params.HttpClientParams.class)));
			
			// allow to query connection timeout
			allowing(connectionManager).getConnectionWithTimeout(with(any(HostConfiguration.class)), with(any(Long.class)));
			will(returnValue(connection));

			// the HTTP status returned to the client
			allowing(connection).readLine(with(any(String.class)));
			will(new ReadLineAction(bodyStream));

			// the response body
			allowing(connection).getResponseInputStream();	
			will(returnValue(bodyStream));
			
			allowing(connection).getParams();
			will(returnValue(connectionParams));
			
			allowing(connectionParams).getParameter(HttpConnectionParams.SO_TIMEOUT);
			will(returnValue(new Integer(10000)));

			// ignore the rest
			ignoring(connection);
			ignoring(connectionParams);
		}});
		return connectionManager;
	}

	public HttpClient getHttpClient(InputStream bodyStream) throws IOException {
		final HttpClient httpClient = new HttpClient();
		httpClient.setHttpConnectionManager(this.getConnectionManager(bodyStream));
		return httpClient;
	}
}

class ReadLineAction implements Action {
	private InputStream input = null;
	
	public ReadLineAction(InputStream input) {
		this.input = input;
	}

	public void describeTo(Description arg0) { }

	public Object invoke(Invocation invocation) throws Throwable {
		return this.readLine(this.input);
	}
		
	private String readLine(final InputStream bodyStream) throws IOException {
		// reading the Status-line
		final StringBuffer statusLine = new StringBuffer();
		int c = -1;
		while((c = bodyStream.read()) != '\n') {
			statusLine.append((char)c);
		}
		statusLine.append('\n');
		return statusLine.toString();
	}
}
