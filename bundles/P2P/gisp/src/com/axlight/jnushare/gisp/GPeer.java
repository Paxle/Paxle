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
 * $Id: GPeer.java,v 1.16 2003/12/23 04:21:33 daishi Exp $
 */

package com.axlight.jnushare.gisp;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.Comparator;

/**
 * GISP Peer Information.
 * 
 *
 */


public final class GPeer extends GInfo implements Cloneable{

    private long lastModifiedTime = 0;

    private int errorcount = 0;
    private boolean donePeerSearch = false;
    private boolean primary = false;
    private long lastReplyReceivedTime = 0;

    /**
     * null constructor
     */
    private GPeer(){
    }

    public GPeer(GHash p, String addr, long t, long l, Comparator c){
	super(p, addr, t, c);
	assert l > 0;

	lastModifiedTime = l;
    }

    public void update(GPeer peer){
	assert peer != null;
	assert this.equals(peer);
	assert pid.equals(peer.pid);

	super.update(peer);

	primary = false;
	
	donePeerSearch = false;
	if(errorcount > 0){
	    errorcount = 0;
	    lastReplyReceivedTime = 0;
	    lastModifiedTime = peer.lastModifiedTime;
	}
    }

    public String getAddress(){
	return (String)data;
    }

    public long getLastModifiedTime(){
	return lastModifiedTime;
    }

    public int getErrorcount(){
    	return errorcount;
    }

    public boolean isDonePeerSearch(){
	return donePeerSearch;
    }

    public void setDonePeerSearch(boolean b){
	donePeerSearch = b;
    }

    public boolean isPrimary(){
	return primary;
    }

    public void setPrimary(boolean b){
	primary = b;
    }

    public void replyReceived(long currTime){
	if(errorcount > 0){
	    errorcount = 0;
	    lastModifiedTime = currTime;
	}
	lastReplyReceivedTime = currTime;
    }
    
    public void replyTimeout(long createdTime){
	if(lastReplyReceivedTime < createdTime){
	    errorcount++;
	}
    }

    public boolean equals(Object object){
        if(this == object){
            return true;
	}

        if(object == null || !(object instanceof GPeer)){
	    return false;
	}

	GPeer target = (GPeer)object;
	return comparator.same(this, target);
    }

    public int hashCode(){
	return data.hashCode();
    }

    public synchronized Object clone(){
	GPeer gp = (GPeer)super.clone();
	gp.lastModifiedTime = lastModifiedTime;
	gp.errorcount = errorcount;
	gp.donePeerSearch = donePeerSearch;
	gp.primary = primary;
	gp.lastReplyReceivedTime = lastReplyReceivedTime;
	return gp;
    }

}
