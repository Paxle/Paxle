/*
 * Copyright (C) 2003 Daishi Kato <daishi@axlight.com>
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
 * $Id: GInfoManagerMemory.java,v 1.3 2003/02/25 08:09:27 daishi Exp $
 */

package com.axlight.jnushare.gisp;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.dom4j.Element;
import org.dom4j.DocumentHelper;

/**
 * GInfoManager
 * 
 * - insert data
 * - query data
 * - delete expired data
 * - remote insert data
 * - delete if max num exceeded (TODO)
 *
 */

public class GInfoManagerMemory implements GInfoManager, Runnable{
    private static final Log LOG =
        LogFactory.getLog(GInfoManagerMemory.class.getName());
    
    private List datahouse = new ArrayList();
    private Initiator initiator = null;
    private volatile boolean running = false;

    public GInfoManagerMemory(){
    }
    
    public void start(Initiator i, boolean useThread){
	if(i == null){
	    LOG.fatal("GInfoManagerMemory: i == null");
	}
	initiator = i;

	LOG.info("start GInfoManagerMemory");
	if(running){
	    return;
	}
	running = true;
	if(useThread){
	    new Thread(this).start();
	}
    }

    public void stop(){
	LOG.info("stop GInfoManagerMemory");
	running = false;

	// deliver all info to the peers
	synchronized(datahouse){
	    for(Iterator ite = datahouse.iterator(); ite.hasNext();){
		Object obj = ite.next();
		if(obj == null || !(obj instanceof GInfo)){
		    continue;
		}
		GInfo info = (GInfo)obj;
		initiator.remoteInsertGInfo(info);
	    }
	}

    }

    /**
     * main thread to manage GInfo
     */
    public void run(){
	while(running){

	    try{
		manageGInfo();
	    }catch(Exception e){
		LOG.error("GInfoManager.run(): unexpected exception:", e);
	    }

	    try{
		Thread.sleep(10 * 1000);
	    }catch(InterruptedException e){
		// ignored
	    }
	}
    }

    /**
     * manage GInfo
     */
    public void manageGInfo(){
	Object[] objs = null;
	synchronized(datahouse){
	    objs = datahouse.toArray();
	}

	for(int i = 0; i < objs.length && running; i++){
	    Object obj = objs[i];
	    if(obj == null || !(obj instanceof GInfo)){
		continue;
	    }
	    GInfo info = (GInfo)obj;
		
	    // delete if expired
	    if(info.getExpirationTime() < initiator.getCurrentTime()){
		synchronized(datahouse){
		    datahouse.remove(info);
		}
	    }else{
		// notify info to GISPimpl
		initiator.remoteInsertGInfo(info);
	    }
	    
	    Thread.yield();
	}
    }

    /**
     * insert data
     */
    public void insertGInfo(GInfo newInfo){
	if(newInfo == null){
	    LOG.error("insertGInfo: newInfo == null");
	    return;
	}
	Element ele = (Element)newInfo.getData();
	if(ele.getDocument() == null){
	    DocumentHelper.createDocument(ele);
	}
	
	if(LOG.isDebugEnabled()){
	    LOG.debug("insertGInfo: element=" + ele.asXML());
	}

	synchronized(datahouse){
	    int index = datahouse.indexOf(newInfo);
	    if(index == -1){
		datahouse.add(newInfo);
	    }else{
		GInfo info = (GInfo)datahouse.get(index);
		if(newInfo.isNewerThan(info)){
		    // update
		    info.update(newInfo);
		}
	    }
	}
    }

    /**
     * query GInfo
     * 
     * results will be notified through listener
     */
    public void queryGInfo(String xpath, Listener listener){
	if(xpath == null || listener == null){
	    LOG.error("queryGInfo: xpath == null || listener == null");
	    return;
	}

	Object[] objs = null;
	synchronized(datahouse){
	    objs = datahouse.toArray();
	}

	for(int i = 0; i < objs.length && running; i++){
	    Object obj = objs[i];
	    if(obj == null || !(obj instanceof GInfo)){
		continue;
	    }
	    GInfo info = (GInfo)obj;
	    Element element = (Element)info.getData();
	    if(element != null){
		List list = element.selectNodes(xpath);
		for(Iterator ite = list.iterator(); ite.hasNext();){
		    Object item = ite.next();
		    if(item != null && item instanceof Element){
			Element ele = (Element)item;
			if(LOG.isDebugEnabled()){
			    LOG.debug("queryGInfo: result ele=" + ele.asXML());
			}
			listener.notifyResult(ele);
		    }
		}
	    }
	}
    }

}

