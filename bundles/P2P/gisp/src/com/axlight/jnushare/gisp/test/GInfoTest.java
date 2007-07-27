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
 * $Id: GInfoTest.java,v 1.3 2003/02/13 08:50:44 daishi Exp $
 */

package com.axlight.jnushare.gisp.test;

import com.axlight.jnushare.gisp.GInfo;
import com.axlight.jnushare.gisp.GPeer;
import com.axlight.jnushare.gisp.GHash;

import java.io.StringReader;

import junit.framework.TestCase;

import org.dom4j.Element;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.dom4j.util.NodeComparator;

public class GInfoTest extends TestCase{

    private SAXReader saxReader = new SAXReader();;

    private NodeComparator nodeComparator = new NodeComparator();
    private GInfo.Comparator infoComparator = new GInfo.Comparator(){
	    public boolean same(GInfo info1, GInfo info2){
		Element ele1 = (Element)info1.getData();
		Element ele2 = (Element)info2.getData();
		return nodeComparator.compare(ele1, ele2) == 0;
	    }
	};
    private GInfo.Comparator peerComparator = new GInfo.Comparator(){
	    public boolean same(GInfo peer1, GInfo peer2){
		String addr1 = (String)peer1.getData();
		String addr2 = (String)peer2.getData();
		return addr1.equals(addr2);
	    }
	};

    public GInfoTest(String s){
	super(s);
    }

    protected void setUp(){
    }

    protected void tearDown(){
    }

    public void test1(){
	try{
	    String pid = "37623c8fcade9c049b4670962cb8c6aaf592b291";
	    String xml = "<foo>123</foo>";
	    Document doc = saxReader.read(new StringReader(xml));
	    Element ele = doc.getRootElement();
	    ele.detach();
	    GInfo i1 = new GInfo(GHash.fromPid(pid), ele, 10000, infoComparator);

	    assertEquals(pid, i1.getHash().toString());
	    assertTrue(ele == i1.getData());
	    assertEquals(10000, i1.getExpirationTime());

	    Document doc2 = saxReader.read(new StringReader(xml));
	    Element ele2 = doc2.getRootElement();
	    ele2.detach();
	    GInfo i2 = new GInfo(GHash.fromPid(pid), ele2, 10001, infoComparator);

	    assertTrue(i1.equals(i2));
	    assertTrue(i2.isNewerThan(i1));

	    i1.update(i2);
	    assertFalse(i2.isNewerThan(i1));

	    GInfo i3 = (GInfo)i1.clone();
	    assertTrue(i1.equals(i3));
	}catch(Exception e){
	    fail(e.toString());
	}
    }

    public void test2(){
	try{
	    String pid = "37623c8fcade9c049b4670962cb8c6aaf592b291";
	    String xml = "<foo>123</foo>";
	    Document doc = saxReader.read(new StringReader(xml));
	    Element ele = doc.getRootElement();
	    ele.detach();
	    GInfo i1 = new GInfo(GHash.fromPid(pid), ele, 10000, infoComparator);

	    GPeer p2 = new GPeer(GHash.fromKey("k2"), "addr2", 1000, 10001, peerComparator);
	    GPeer p3 = new GPeer(GHash.fromKey("k3"), "addr3", 1000, 10003, peerComparator);

	    i1.updateSentPeer(p2, 10004);
	    assertTrue(i1.isSentTo(p2));
	    assertFalse(i1.isSentTo(p3));
	    
	    i1.updateSentPeer(p3, 10005);
	    assertTrue(i1.isSentTo(p2));
	    assertTrue(i1.isSentTo(p3));
	    GPeer[] peers = i1.getSentPeers();
	    assertEquals(2, peers.length);
	    assertTrue(p2.equals(peers[0]) && p3.equals(peers[1]) ||
		       p2.equals(peers[1]) && p3.equals(peers[0]));
	    
	    i1.removeSentPeer(p2);
	    assertFalse(i1.isSentTo(p2));
	    assertTrue(i1.isSentTo(p3));

	    i1.removeSentPeer(p3);
	    assertFalse(i1.isSentTo(p2));
	    assertFalse(i1.isSentTo(p3));
	}catch(Exception e){
	    fail(e.toString());
	}
    }

}
