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
 * $Id: GInfo.java,v 1.18 2003/02/25 07:55:32 daishi Exp $
 */

package com.axlight.jnushare.gisp;

import java.util.Map;
import java.util.HashMap;
import java.util.WeakHashMap;

/**
 * Information Structure for GISP.
 *
 * also base class for GPeer
 *
 */

public class GInfo implements Cloneable{

    /**
     * PID of this info.
     */
    protected GHash pid = null;

    /**
     * data of this info.
     *   GInfo: xml (ex. Element)
     *   GPeer: addres (String)
     */
    protected Object data = null;

    /**
     * expire time of this info.
     */
    protected long expirationTime = 0;

    /**
     * custom comparator.
     */
    protected Comparator comparator = null;

    /**
     * last sent peers.
     * key = GPeer, value = Long (last sent time)
     */
    protected Map lastSentTable = null;


    protected GInfo(){
    }

    public GInfo(GHash p, Object d, long t, Comparator c){
	assert p != null;
	assert d != null;
	assert t >=0;
	assert c != null;

	pid = p;
	data = d;
	expirationTime = t;
	comparator = c;
	try{
	    lastSentTable = new WeakHashMap();
	}catch(Exception e){
	    lastSentTable = new HashMap();
	}
    }

    public synchronized void update(GInfo newInfo){
	assert newInfo != null;
	assert this.equals(newInfo);

	expirationTime = newInfo.expirationTime;
	lastSentTable = newInfo.lastSentTable;
    }

    public GHash getHash(){
	return pid;
    }

    public Object getData(){
	return data;
    }
    
    public long getExpirationTime(){
	return expirationTime;
    }

    public boolean equals(Object object){
        if(this == object){
            return true;
	}

        if(object == null || !(object instanceof GInfo)){
	    return false;
	}

	GInfo target = (GInfo)object;
	return comparator.same(this, target);
    }

    public int hashCode(){
	return pid.hashCode();
    }

    public boolean isNewerThan(GInfo target){
	assert target != null;
	assert this.equals(target);

	return target.expirationTime < expirationTime;
    }

    public synchronized void updateSentPeer(GPeer peer, long currTime){
	assert peer != null;

	lastSentTable.put(peer, new Long(currTime));
    }

    public synchronized void removeSentPeer(GPeer peer){
	assert peer != null;

	lastSentTable.remove(peer);
    }

    public synchronized GPeer[] getSentPeers(){
	return (GPeer[])lastSentTable.keySet().toArray(new GPeer[0]);
    }

    public synchronized boolean isSentTo(GPeer peer){
	assert peer != null;

	long lastSentStamp = 0;
	Long longObject = (Long)lastSentTable.get(peer);
	if(longObject != null){
	    lastSentStamp = longObject.longValue();
	}
	if(lastSentStamp >= peer.getLastModifiedTime()){
	    return true;
	}else{
	    return false;
	}
    }

    public synchronized Object clone(){
	GInfo gi = new GInfo();
	gi.pid = pid;
	gi.data = data;
	gi.expirationTime = expirationTime;
	gi.comparator = comparator;
	try{
	    gi.lastSentTable = new WeakHashMap(lastSentTable);
	}catch(Exception e){
	    gi.lastSentTable = new HashMap(lastSentTable);
	}
	return gi;
    }

    public interface Comparator{
	public boolean same(GInfo o1, GInfo o2);
    }

}
