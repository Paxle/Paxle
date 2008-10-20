/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 * 
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.crawler.http.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.HttpClientError;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AllSSLProtocolSocketFactory implements ProtocolSocketFactory {

	/**
	 * For logging
	 */
    private final Log logger = LogFactory.getLog(this.getClass());

    /**
     * The {@link SSLContext}-instance to use
     */
    private final SSLContext context;
    
    /**
     * The  {@link SocketFactory} used to create a new socket
     * 
     * @see #createSocket(String, int)
     * @see #createSocket(String, int, InetAddress, int)
     * @see #createSocket(String, int, InetAddress, int, HttpConnectionParams)
     */
    private final SSLSocketFactory socketFactory;
    
    public AllSSLProtocolSocketFactory() {
        try {
            this.context = SSLContext.getInstance("SSL");
            this.context.init(
              null, 
              new TrustManager[] {new AllX509TrustManager()}, 
              new java.security.SecureRandom()
            );
            
            this.socketFactory = this.context.getSocketFactory();
        } catch (Throwable e) {
        	String errorMsg = String.format("Unexpected '%s' while creating a SSL context instance: %s",e.getClass().getName(),e.getMessage());
            this.logger.error(errorMsg, e);
            throw new HttpClientError(errorMsg);
        }
	}    

    /**
     * @see ProtocolSocketFactory#createSocket(String, int)
     */
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return this.socketFactory.createSocket(host,port);
    }    

    /**
     * @see ProtocolSocketFactory#createSocket(String, int, InetAddress, int)
     */
    public Socket createSocket(String host, int port, InetAddress clientHost, int clientPort) throws IOException, UnknownHostException {
        return this.socketFactory.createSocket(host, port, clientHost, clientPort);
    }

    /**
     * @see ProtocolSocketFactory#createSocket(String, int, InetAddress, int, HttpConnectionParams)
     */
    public Socket createSocket(String host, int port, InetAddress localAddress, int localPort, HttpConnectionParams params) throws IOException, UnknownHostException, ConnectTimeoutException {
        if (params == null) throw new NullPointerException("The connection-params must not be null");

        int timeout = params.getConnectionTimeout();
        if (timeout == 0) {
            return this.socketFactory.createSocket(host, port, localAddress, localPort);
        } else {
            Socket socket = this.socketFactory.createSocket();
            SocketAddress localaddr = new InetSocketAddress(localAddress, localPort);
            SocketAddress remoteaddr = new InetSocketAddress(host, port);
            socket.bind(localaddr);
            socket.connect(remoteaddr, timeout);
            return socket;
        }
    }
}
