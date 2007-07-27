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
 * $Id: GPeerTest.java,v 1.2 2003/02/13 08:50:44 daishi Exp $
 */

package com.axlight.jnushare.gisp.test;

import com.axlight.jnushare.gisp.GHash;
import com.axlight.jnushare.gisp.GInfo;
import com.axlight.jnushare.gisp.GPeer;

import junit.framework.TestCase;

public class GPeerTest extends TestCase{

    private GInfo.Comparator peerComparator = new GInfo.Comparator(){
	    public boolean same(GInfo peer1, GInfo peer2){
		String addr1 = (String)peer1.getData();
		String addr2 = (String)peer2.getData();
		return addr1.equals(addr2);
	    }
	};

    public GPeerTest(String s){
	super(s);
    }

    protected void setUp(){
    }

    protected void tearDown(){
    }

    public void test1(){
	GPeer p1 = new GPeer(GHash.fromKey("k1"), "addr1", 1000, 10000, peerComparator);
	GPeer p2 = new GPeer(GHash.fromKey("k2"), "addr1", 1000, 10001, peerComparator);
	GPeer p3 = new GPeer(GHash.fromKey("k2"), "addr3", 1000, 10000, peerComparator);
	assertTrue(p1.equals(p2));
	assertFalse(p1.equals(p3));
    }

    public void test2(){
	GPeer p1 = new GPeer(GHash.fromKey("k1"), "addr1", 1000, 10000, peerComparator);
	GPeer p2 = new GPeer(GHash.fromKey("k2"), "addr2", 1000, 10001, peerComparator);
	GPeer p3 = new GPeer(GHash.fromKey("k3"), "addr3", 1000, 10003, peerComparator);

	p1.updateSentPeer(p2, 10004);
	assertTrue(p1.isSentTo(p2));
	assertFalse(p1.isSentTo(p3));

	p1.updateSentPeer(p3, 10005);
	assertTrue(p1.isSentTo(p2));
	assertTrue(p1.isSentTo(p3));
	GPeer[] peers = p1.getSentPeers();
	assertEquals(2, peers.length);
	assertTrue(p2.equals(peers[0]) && p3.equals(peers[1]) ||
		   p2.equals(peers[1]) && p3.equals(peers[0]));

	p1.removeSentPeer(p2);
	assertFalse(p1.isSentTo(p2));
	assertTrue(p1.isSentTo(p3));

	p1.removeSentPeer(p3);
	assertFalse(p1.isSentTo(p2));
	assertFalse(p1.isSentTo(p3));
    }

}
