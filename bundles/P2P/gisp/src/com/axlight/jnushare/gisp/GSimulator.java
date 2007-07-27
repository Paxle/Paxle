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
 * $Id: GSimulator.java,v 1.18 2003/02/25 07:57:46 daishi Exp $
 */

package com.axlight.jnushare.gisp;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.BasicConfigurator;

/**
 * GSimulator
 * 
 * to see how GISPCore works
 *
 */

public class GSimulator implements Runnable{
    private static final Logger LOG =
	Logger.getLogger(GSimulator.class.getName());

    private String scenario = "scenario1";
    private Map peerMap = new HashMap();
    private List sendQueue = new ArrayList();
    private List recvQueue = new ArrayList();
    private volatile long currTime = 1000;

    public static void main(String[] args){
	if(new File("log4j.properties").isFile()){
	    PropertyConfigurator.configure("log4j.properties");
	}else{
	    BasicConfigurator.configure();
	    LOG.setLevel(Level.DEBUG);
	}

	new GSimulator(args);
    }

    public GSimulator(String[] args){
	if(args.length == 1){
	    scenario = args[0];
	}

	Thread thread = new Thread(this);
	thread.setPriority(Thread.NORM_PRIORITY);
	thread.start();
    }

    public void run(){
	if("scenario1".equals(scenario)){
	    LOG.info("starting scenario1");
	    scenario1();
	}else if("scenario2".equals(scenario)){
	    LOG.info("starting scenario2");
	    scenario2();
	}else if("scenario3".equals(scenario)){
	    LOG.info("starting scenario3");
	    scenario3();
	}else{
	    LOG.error("no such scenario: " + scenario);
	}
    }

    private void scenario1(){
	int peercounter = 1000000;
	int datacounter = 10000;
	currTime = 6000;

	createNewPeer(peercounter++);

	while(true){
	    LOG.info("currTime = " + currTime);

	    if(peercounter < 1001000 && currTime % 3000 == 0){
		createNewPeer(peercounter++);
	    }
	    
	    Object[] names;
	    synchronized(peerMap){
		names = peerMap.keySet().toArray();
	    }
	    for(int i = 0; i < names.length; i++){
		GISP peer;
		synchronized(peerMap){
		    peer = (GISP)peerMap.get(names[i]);
		}
		LOG.debug("[" + names[i] + "] " +
			  "num of peers: " + peer.getNumOfPeers());
	    }
	    
	    if(currTime % 6000 == 1000){
		String data = "data" + datacounter;
		String key = "key" + datacounter;
		
		String srcname = getRandomPeerName();
		GISP srcpeer = null;
		synchronized(peerMap){
		    srcpeer = (GISP)peerMap.get(srcname);
		}
		
		LOG.info("[" + srcname + "] " +
			 "inserting: data=" + data + " ,key=" + key);
		srcpeer.insert(key, data, 10 * 1000);
	    }
	    
	    if(currTime % 6000 == 5000){
		String key = "key" + datacounter;
		
		String dstname = getRandomPeerName();
		GISP dstpeer = null;
		synchronized(peerMap){
		    dstpeer = (GISP)peerMap.get(dstname);
		}
		
		LOG.info("[" + dstname + "] " + "searching: key=" + key);
		
		final String finalName = dstname;
		ResultListener listener = new ResultListener(){
			public void stringResult(String data){
			    LOG.info("[" + finalName + "] " +
				     "got result: data=" + data);
			}
			public void xmlResult(byte[] data){
			}
			public void queryExpired(){
			}
		    };
		dstpeer.query(key, listener, 10 * 1000);
		
		datacounter++;
	    }
	    
	    maintainAllPeer();
	    deliverMessage(0.2);
	    
	    currTime += 1000;
	}
    }

    private void scenario2(){
	int peercounter = 1000000;
	int datacounter = 10000;
	currTime = 6000;

	while(peercounter < 1000300){
	    createNewPeer(peercounter++);
	}

	while(true){
	    LOG.info("currTime = " + currTime);

	    Object[] names;
	    synchronized(peerMap){
		names = peerMap.keySet().toArray();
	    }
	    for(int i = 0; i < names.length; i++){
		GISP peer;
		synchronized(peerMap){
		    peer = (GISP)peerMap.get(names[i]);
		}
		LOG.debug("[" + names[i] + "] " +
			  "num of peers: " + peer.getNumOfPeers());
	    }
	    
	    if(currTime % 6000 == 1000){
		String data = "data" + datacounter;
		String key = "key" + datacounter;
		
		String srcname = getRandomPeerName();
		GISP srcpeer = null;
		synchronized(peerMap){
		    srcpeer = (GISP)peerMap.get(srcname);
		}
		
		LOG.info("[" + srcname + "] " +
			 "inserting: data=" + data + " ,key=" + key);
		srcpeer.insert(key, data, 10 * 1000);
	    }
	    
	    if(currTime % 6000 == 5000){
		String key = "key" + datacounter;
		
		String dstname = getRandomPeerName();
		GISP dstpeer = null;
		synchronized(peerMap){
		    dstpeer = (GISP)peerMap.get(dstname);
		}
		
		LOG.info("[" + dstname + "] " + "searching: key=" + key);
		
		final String finalName = dstname;
		ResultListener listener = new ResultListener(){
			public void stringResult(String data){
			    LOG.info("[" + finalName + "] " +
				     "got result: data=" + data);
			}
			public void xmlResult(byte[] data){
			}
			public void queryExpired(){
			}
		    };
		dstpeer.query(key, listener, 10 * 1000);
		
		datacounter++;
	    }
	    
	    maintainAllPeer();
	    deliverMessage(0.2);
	    
	    currTime += 1000;
	}
    }

    private void scenario3(){
	int peercounter = 1000000;
	int datacounter = 10000;
	currTime = 6000;
	List keyList = new ArrayList();

	createNewPeer(peercounter++);

	while(true){
	    LOG.info("currTime = " + currTime);

	    if(peercounter < 1001000 && currTime % 6000 == 0){
		createNewPeer(peercounter++);
	    }
	    
	    Object[] names;
	    synchronized(peerMap){
		names = peerMap.keySet().toArray();
	    }
	    for(int i = 0; i < names.length; i++){
		GISP peer;
		synchronized(peerMap){
		    peer = (GISP)peerMap.get(names[i]);
		}
		LOG.debug("[" + names[i] + "] " +
			  "num of peers: " + peer.getNumOfPeers());
	    }
	    
	    if(currTime % 18000 == 0000){
		for(int i = 0; i < peerMap.size(); i++){
		    String data = "data" + datacounter;
		    String key = "key" + datacounter;
		    
		    String srcname = getRandomPeerName();
		    GISP srcpeer = null;
		    synchronized(peerMap){
			srcpeer = (GISP)peerMap.get(srcname);
		    }
		    
		    LOG.info("[" + srcname + "] " +
			     "inserting: data=" + data + " ,key=" + key);
		    srcpeer.insert(key, data, 30 * 1000);

		    keyList.add(key);
		    datacounter++;
		}
	    }
	    
	    if(currTime % 18000 == 9000){
		for(Iterator ite = keyList.iterator(); ite.hasNext();){
		    String key = (String)ite.next();
		
		    String dstname = getRandomPeerName();
		    GISP dstpeer = null;
		    synchronized(peerMap){
			dstpeer = (GISP)peerMap.get(dstname);
		    }
		    
		    LOG.info("[" + dstname + "] " + "searching: key=" + key);
		    
		    final String finalName = dstname;
		    ResultListener listener = new ResultListener(){
			    public void stringResult(String data){
				LOG.info("[" + finalName + "] " +
					 "got result: data=" + data);
			    }
			    public void xmlResult(byte[] data){
			    }
			    public void queryExpired(){
			    }
			};
		    dstpeer.query(key, listener, 20 * 1000);
		}
		keyList.clear();
	    }
	    
	    maintainAllPeer();
	    deliverMessage(0.02);
	    
	    currTime += 3000;
	}
    }

    ////////////////////////////////////////

    private void createNewPeer(int i){
	String peername = "p" + i;
	NormalPeer nr = new NormalPeer(peername);
	synchronized(peerMap){
	    peerMap.put(peername, nr);
	}
	LOG.info("created peername=" + peername);
    }

    private String getRandomPeerName(){
	try{
	    Object[] names;
	    synchronized(peerMap){
		names = peerMap.keySet().toArray();
	    }
	    int index = (int)(Math.random() * names.length);
	    return (String)names[index];
	}catch(ArrayIndexOutOfBoundsException e){
	    LOG.error("there exists no peers!");
	    return null;
	}
    }

    private void deliverMessage(double dropRate){
	synchronized(recvQueue){
	    sendQueue.addAll(recvQueue);
	    recvQueue.clear();
	}

	int size = sendQueue.size();
	LOG.debug("send queue size=" + size);
	if(size == 0){
	    return;
	}

	while(!sendQueue.isEmpty()){
	    Object[] item = (Object[])sendQueue.remove(0);
	    String name = (String)item[0];
	    String address = (String)item[1];
	    byte[] mesgBuf = (byte[])item[2];
	    
	    GISPXML target;
	    synchronized(peerMap){
		target = (GISPXML)peerMap.get(address);
	    }
	    if(target == null){
		LOG.info("[" + name + "] " +
			 "failed to send message: address=" + address);
	    }else if(Math.random() < dropRate){
		LOG.info("[" + name + "] " +
			 "dropped message: address=" + address);
	    }else{
		LOG.info("[" + name + "] " +
			 "sending message: address=" + address);
		try{
		    LOG.debug("mesg size=" + mesgBuf.length +
			      ", content=" +new String(mesgBuf, "UTF-8"));
		}catch(UnsupportedEncodingException e){
		    //ignored
		}
		
		target.processMessage(mesgBuf);
	    }
	}
    }
    
    private void maintainAllPeer(){
	Object[] peers;
	synchronized(peerMap){
	    peers = peerMap.values().toArray();
	}
	for(int i = 0; i < peers.length; i++){
	    GISPXML peer = (GISPXML)peers[i];
	    peer.maintainGInfo();
	    peer.maintainQueryTable();
	    peer.maintainLocalData();
	    peer.flushSendQueue();
	}
    }

    class NormalPeer extends GISPXML{
	String name;

	NormalPeer(String name){
	    super();
	    this.name = name;
	    replyTimeout = 30 * 1000; // 30sec
	    myPeerLifetime = 10 * 60 * 1000; // 10min

	    GInfoManager infoManager = new GInfoManagerMemory();
	    infoManager.start(this, false);
	    setGInfoManager(infoManager);
	    start(false);
	}

	protected void sendMessage(byte[] mesgBuf, String address){
	    synchronized(recvQueue){
		recvQueue.add(new Object[]{name, address, mesgBuf});
	    }
	}

	protected String getMyAddress(){
	    return name;
	}

	public long getCurrentTime(){
	    return currTime;
	}

	protected String getSeedAddress(int timeout){
	    String addr = null;
	    for(int i = 0; i < 10; i++){
		addr = getRandomPeerName();
		if(name.equals(addr)){
		    addr = null;
		}
		if(addr != null){
		    break;
		}
	    }
	    return addr;
	}

    }
}
