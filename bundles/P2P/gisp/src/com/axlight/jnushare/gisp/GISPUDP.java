/*
 * Copyright (C) 2002-2003 Daishi Kato <daishi@axlight.com>
 * All rights reserved.
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the Sun Project JXTA
 * License Version 1.1 (the "License"); you may not use this
 * file except in compliance with the License. A copy of the
 * License is available at http://www.jxta.org/jxta_license.html.
 * ====================================================================
 *
 * $Id: GISPUDP.java,v 1.10 2003/03/12 18:09:29 daishi Exp $
 */

package com.axlight.jnushare.gisp;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.IOException;
import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

/**
 * GISP (Global Information Sharing Prolocol) for UDP binding
 *
 *
 */

public class GISPUDP extends GISPXML{

    private static final Logger LOG =
	Logger.getLogger(GISPUDP.class.getName());

    private static final int PACKETSIZE = 64 * 1000;

    /**
     * list of addressed for seed
     * item = String (Address)
     */
    private List seedAddressList = new ArrayList();

    private String myAddress = null;
    private DatagramSocket socket = null;
    private boolean running = false;

    public GISPUDP(int port) throws Exception{
	this(InetAddress.getLocalHost().getHostAddress(), port);
    }

    public GISPUDP(String host, int port) throws Exception{
	super();
	messageSize = 60 * 1000; // 60KB
	itemCountPerMessage = 20; // just guessing

	myAddress = host + ":" + port;
	socket = new DatagramSocket(port);
    }

    public void addSeedAddresses(String[] seeds){
	synchronized(seedAddressList){
	    seedAddressList.addAll(Arrays.asList(seeds));
	}
    }

    public void start(String[] argv){
	LOG.info("Start GISP UDP binding");

	if(argv == null){
	    argv = new String[0];
	}

	myStrengthMin = 0;
	myStrengthMax = 10;
	for(int i = 0; i < argv.length; i++){
	    try{
		if(argv[i].startsWith("strength_min=")){
		    myStrengthMin = Integer.parseInt
			(argv[i].substring("strength_min=".length()));
		}else if(argv[i].startsWith("strength_max=")){
		    myStrengthMax = Integer.parseInt
			(argv[i].substring("strength_max=".length()));
		}
	    }catch(NumberFormatException e){
		// ignored
	    }
	}
	if(myStrengthMin < 0){
	    myStrengthMin = 0;
	}
	if(myStrengthMin > 10){
	    myStrengthMin = 10;
	}
	if(myStrengthMax < 0){
	    myStrengthMax = 0;
	}
	if(myStrengthMax > 10){
	    myStrengthMax = 10;
	}
	if(myStrengthMin > myStrengthMax){
	    myStrengthMin = myStrengthMax;
	}

	running = true;
	new UDPServerThread().start();

	GInfoManager infoManager = new GInfoManagerMemory();
	infoManager.start(this, true);
	setGInfoManager(infoManager);
	super.start(true);
    }

    public void stop(){
	LOG.info("Stop GISP UDP binding");
	super.stop();
	running = false;
    }

    protected String getMyAddress(){
	return myAddress;
    }

    public long getCurrentTime(){
	return System.currentTimeMillis();
    }

    protected String getSeedAddress(int timeout){
	String address = null;
	try{
	    synchronized(seedAddressList){
		address = (String)seedAddressList.remove(0);
	    }
	}catch(IndexOutOfBoundsException e){
	    // ignored
	}
	return address;
    }

    protected void sendMessage(byte[] mesgBuf, String address){
	if(LOG.isDebugEnabled()){
	    LOG.debug("sendMessage: mesgBuf=" + new String(mesgBuf)
		      + ", address=" + address);
	}

	try{
	    int colonIndex = address.indexOf(":");
	    String hostStr = address.substring(0, colonIndex);
	    String portStr = address.substring(colonIndex + 1);
	    
	    InetAddress host = InetAddress.getByName(hostStr);
	    int port = Integer.parseInt(portStr);
	    
	    DatagramPacket packet =
		new DatagramPacket(mesgBuf, mesgBuf.length, host, port);
	    socket.send(packet);
	}catch(IndexOutOfBoundsException e){
	    LOG.warn("sendMessage: " + e);
	}catch(UnknownHostException e){
	    LOG.warn("sendMessage: " + e);
	}catch(IOException e){
	    LOG.warn("sendMessage: " + e);
	}
    }

    private class UDPServerThread extends Thread{
	public void run(){
	    byte[] buf = new byte[PACKETSIZE];
	    while(running){
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		try{
		    socket.receive(packet);

		    byte[] mesgBuf = new byte[packet.getLength()];
		    for(int i = 0; i < mesgBuf.length; i++){
			mesgBuf[i] = buf[i];
		    }

		    if(LOG.isDebugEnabled()){
			LOG.debug("received mesgBuf=" + new String(mesgBuf));
		    }

		    processMessage(mesgBuf);
		}catch(IOException e){
		    LOG.warn("UDPServerThread: " + e);
		}
	    }
	}
    }

}

