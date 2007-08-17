/*
 * Copyright (C) 2001-2003 Daishi Kato <daishi@axlight.com>
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
 * $Id: GISPImpl.java,v 1.40 2004/06/16 23:48:13 daishi Exp $
 */

package com.axlight.jnushare.gisp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.MimeMediaType;
import net.jxta.endpoint.ByteArrayMessageElement;
import net.jxta.endpoint.EndpointAddress;
import net.jxta.endpoint.EndpointListener;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.endpoint.Messenger;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.peergroup.PeerGroup;
import net.jxta.platform.Module;
import net.jxta.platform.ModuleClassID;
import net.jxta.protocol.ModuleSpecAdvertisement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * GISP(Global Information Sharing Prolocol) Module for JXTA.
 * 
 * GISPCore provides platform independent code.
 * GISPXML extends GISPCore for XML messaging.
 * GISPImpl extends GISPXML to be JXTA Module.
 * GISPImpl sends/receives message via EndpointService.
 * GISPImpl publishes ModuleSpecAdvertisement.
 *
 *
 */

public final class GISPImpl extends GISPXML implements Module, EndpointListener{
    private static final Log LOG =
        LogFactory.getLog(GISPImpl.class.getName());

    private static final String serviceName = "GISP-3.4";

    private long lastAdvPublishedTime = 0;
    private long advLifetime = 30 * 60 * 1000; // 30min
    private long publishAdvThreshold = 20;

    private PeerGroup myGroup = null;
    private String serviceParam = null;
    private DiscoveryService discovery = null;

    private ModuleSpecAdvertisement moduleSpecAdv = null;

    private volatile boolean running = false;

    /**
     * list of addressed for seed.
     * item = String (Address)
     */
    private List<String> seedAddressList = new ArrayList<String>();
    private Object lockForNewAddress = new Object();
    
    /**
     * receive message queue.
     * item = byte[] (Message)
     */
    private List<Message> recvQueue = new ArrayList<Message>();

    /**
     * send message queue.
     * item = Object[] {byte[] (Message), String (Address)}
     */
    private List<Object[]> sendQueue = new ArrayList<Object[]>();

    public GISPImpl(){
	super();
    }

    public void init(PeerGroup group, ID id, Advertisement adv){
	LOG.info("Init GISP service");
	myGroup = group;
	serviceParam = myGroup.getPeerGroupID().getUniqueValue().toString();
	discovery = myGroup.getDiscoveryService();

	setGInfoManager(new GInfoManagerMemory());
    }
    
    public int startApp(String argv[]){
	LOG.info("Start GISP service");

	if(myGroup == null){
	    LOG.error("not initialized yet");
	    return 1;
	}

	if(argv == null){
	    argv = new String[0];
	}

	myStrengthMin = 1;
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

	new PublishAdvertisementThread().start();
	new ProcessIncomingMessageThread().start();
	new SendMessageThread().start();
	new SendMessageThread().start();
	new SendMessageThread().start();

	// register endpoint listener
        myGroup.getEndpointService().
	    addIncomingMessageListener(this, serviceName, serviceParam);
	
	super.start(true);

	return 0;
    }

    public void stopApp(){
	LOG.info("Stop GISP service");

	super.stop();

	while(true){
	    int size = 0;
	    synchronized(sendQueue){
		size = sendQueue.size();
	    }
	    if(size <= 0){
		break;
	    }
	    if(LOG.isDebugEnabled()){
		LOG.debug("stopApp: sendQueue.size()=" + size);
	    }
	    try{
		Thread.sleep(100);
	    }catch(InterruptedException e){
		//ignored
	    }
	}
	running = false;

	// unregister endpoint listener
        myGroup.getEndpointService().
	    removeIncomingMessageListener(serviceName, serviceParam);
	
    }
    
    /**
     * get endpoint address for this peer.
     *
     * @return String of this EndpointAddress
     */
    protected String getMyAddress(){
        return "jxta://" + 
	    myGroup.getPeerID().getUniqueValue().toString() +
	    "/" + serviceName +"/" + serviceParam;
    }
    
    /**
     * to get seed peer address.
     * it may take some time.
     */
    protected String getSeedAddress(int timeout){
	String address = null;
	try{
	    synchronized(seedAddressList){
		address = (String)seedAddressList.remove(0);
	    }
	}catch(IndexOutOfBoundsException e){
	    // ignored
	}
	if(address == null){
	    synchronized(lockForNewAddress){
		lockForNewAddress.notify();
	    }
	    try{
		synchronized(seedAddressList){
		    seedAddressList.wait(timeout);
		}
	    }catch(InterruptedException e){
		// ignored
	    }
	    try{
		synchronized(seedAddressList){
		    address = (String)seedAddressList.remove(0);
		}
	    }catch(IndexOutOfBoundsException e){
		// ignored
	    }
	}
	return address;
    }

    public long getCurrentTime(){
	return System.currentTimeMillis();
    }

    /**
     * to send GMessage to specific address.
     * return false if it failed immediately.
     */
    protected void sendMessage(byte[] mesgBuf, String address){
	synchronized(sendQueue){
	    sendQueue.add(new Object[]{mesgBuf, address});
	    sendQueue.notify();
	}
    }

    /**
     * thread for sending message.
     */
    class SendMessageThread extends Thread{
	public void run(){
	    while(running){
		try{
		    Object[] data = null;
		    try{
			synchronized(sendQueue){
			    data = (Object[])sendQueue.remove(0);
			}
		    }catch(IndexOutOfBoundsException e){
			//ignored
		    }
		    if(data != null){
			sendOneMessage((byte[])data[0], (String)data[1]);
		    }else{
			try{
			    synchronized(sendQueue){
				sendQueue.wait(1000);
			    }
			}catch(InterruptedException e){
			    //ignored
			}
		    }
		}catch(Exception e){
		    LOG.error("SendMessageThread: unexpected exception: ", e);
		}
	    }
	}
	
	public void sendOneMessage(byte[] mesgBuf, String address){
	    if(LOG.isDebugEnabled()){
		LOG.debug("sendOneMessage: mesgBuf=" + new String(mesgBuf)
			  + ", address=" + address);
	    }

	    Message message = new Message();
	    
	    message.addMessageElement(new ByteArrayMessageElement(
			  serviceName, MimeMediaType.XMLUTF8, mesgBuf, null));
	    
	    EndpointAddress destAddr = new EndpointAddress(address);
	    Messenger messenger =
		myGroup.getEndpointService().getMessenger(destAddr, null);
	    if(messenger == null){
		LOG.warn("sendMessage: couldn't get messenger: " + address);
		return;
	    }
	    
	    try{
		messenger.sendMessage(message);
		messenger.close();
	    }catch(IOException e){
		LOG.warn("sendMessage: couldn't send message: " + e);
		return;
	    }
	}
    }

    /**
     * implements EndpointListener.
     */
    public void processIncomingMessage(Message message,         
				       EndpointAddress srcAddr, 
				       EndpointAddress dstAddr){
	synchronized(recvQueue){
	    recvQueue.add(message);
	    recvQueue.notify();
	}
    }

    /**
     * thread for processIncomingMessage
     */
    class ProcessIncomingMessageThread extends Thread{

	public void run(){
	    while(running){
		try{
		    Message mesg = null;
		    try{
			synchronized(recvQueue){
			    mesg = (Message)recvQueue.remove(0);
			}
		    }catch(IndexOutOfBoundsException e){
			//ignored
		    }
		    if(mesg != null){
			processOneMessage(mesg);
		    }else{
			try{
			    synchronized(recvQueue){
				recvQueue.wait(1000);
			    }
			}catch(InterruptedException e){
			    //ignored
			}
		    }
		}catch(Exception e){
		    LOG.error("ProcessIncomingMessageThread: " +
			      "unexpected exception: ", e);
		}
	    }
	}

	public void processOneMessage(Message message){
	    MessageElement me = message.getMessageElement(serviceName);
	    if(me != null && me instanceof ByteArrayMessageElement){
		byte[] mesgBuf = ((ByteArrayMessageElement)me).getBytes();

		if(LOG.isDebugEnabled()){
		    LOG.debug("processOneMessage: mesgBuf="
			      + new String(mesgBuf));
		}

		processMessage(mesgBuf);
	    }else{
		LOG.warn("processOneMessage: no byte array message element");
	    }
	}
    }

    /**
     * thread for publishing advertisement.
     */
    class PublishAdvertisementThread extends Thread{

	public void run(){
	    while(running){
		try{
		    
		    int seedAddressListSize;
		    synchronized(seedAddressList){
			seedAddressListSize = seedAddressList.size();
		    }
		    
		    Enumeration enum1 = null;
		    try{
			enum1 = discovery.getLocalAdvertisements
			    (DiscoveryService.ADV, "Name", serviceName);
		    }catch(IOException e){
			LOG.warn("IOException while getting local adv");
			//ignored
		    }
		    
		    int localAdvCount = 0;
		    if(enum1 != null && enum1.hasMoreElements()){
			while(enum1.hasMoreElements()){
			    Object element = enum1.nextElement();
			    if(element instanceof ModuleSpecAdvertisement){
				ModuleSpecAdvertisement adv =
				    (ModuleSpecAdvertisement)element;
				String seedAddr = adv.getDescription();
				if(seedAddr.equals(getMyAddress())){
				    continue;
				}
				localAdvCount++;
				if(seedAddressListSize <= 0){
				    LOG.info("adding a new seed address");
				    synchronized(seedAddressList){
					seedAddressList.add(seedAddr);
					seedAddressList.notify();
				    }
				}
			    }
			}
		    }

		    if(localAdvCount < publishAdvThreshold){
			LOG.info("looking for remote advertisements");
			discovery.getRemoteAdvertisements(null,
			  DiscoveryService.ADV, "Name", serviceName, 10);
		    }
		    
		    if(localAdvCount < publishAdvThreshold &&
		       lastAdvPublishedTime + advLifetime
		       < System.currentTimeMillis()){
			publishMyAdvertisement();
			lastAdvPublishedTime = System.currentTimeMillis();
		    }
		    
		    try{
			synchronized(lockForNewAddress){
			    if(localAdvCount <= 0){
				lockForNewAddress.wait(5 * 1000);
			    }else if(localAdvCount < publishAdvThreshold){
				lockForNewAddress.wait(60 * 1000);
			    }else{
				lockForNewAddress.wait(30 * 60 * 1000);
			    }
			}
		    }catch(InterruptedException e){
			//ignored
		    }

		}catch(Exception e){
		    LOG.error("PublishAdvThread: unexpected exception: ", e);
		}
	    }
	}
    }

    private synchronized void publishMyAdvertisement(){

	if(moduleSpecAdv == null){
	    LOG.info("creating my advertisement");

	    // is this OK?
	    ModuleClassID classID = IDFactory.newModuleClassID();

	    moduleSpecAdv = (ModuleSpecAdvertisement)
		AdvertisementFactory.newAdvertisement
		(ModuleSpecAdvertisement.getAdvertisementType());

	    moduleSpecAdv.setName(serviceName);
	    moduleSpecAdv.setModuleSpecID(IDFactory.newModuleSpecID(classID));
	    moduleSpecAdv.setDescription(getMyAddress());
	}

	LOG.info("publishing my advertisement");
	try{
	    discovery.publish(moduleSpecAdv, advLifetime, advLifetime);
	}catch(IOException e){
	    LOG.error("IOException while publishing my adv");
	}
	discovery.remotePublish(moduleSpecAdv, advLifetime);
    }

}

