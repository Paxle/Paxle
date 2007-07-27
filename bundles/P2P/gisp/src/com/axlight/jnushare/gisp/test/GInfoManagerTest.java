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
 * $Id: GInfoManagerTest.java,v 1.3 2003/02/13 08:50:44 daishi Exp $
 */

package com.axlight.jnushare.gisp.test;

import com.axlight.jnushare.gisp.GHash;
import com.axlight.jnushare.gisp.GInfo;
import com.axlight.jnushare.gisp.GInfoManager;
import com.axlight.jnushare.gisp.GInfoManagerMemory;

import junit.framework.TestCase;

import org.dom4j.Element;
import org.dom4j.Document;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.DocumentHelper;
import org.dom4j.io.SAXReader;
import org.dom4j.util.NodeComparator;

import java.io.StringReader;

public class GInfoManagerTest extends TestCase{

    private SAXReader saxReader = new SAXReader();

    private NodeComparator nodeComparator = new NodeComparator();
    private GInfo.Comparator comparator = new GInfo.Comparator(){
	    public boolean same(GInfo info1, GInfo info2){
		Element ele1 = (Element)info1.getData();
		Element ele2 = (Element)info2.getData();
		return nodeComparator.compare(ele1, ele2) == 0;
	    }
	};

    private volatile Element resultEle = null;
    private volatile GInfo insertInfo = null;

    public GInfoManagerTest(String s){
	super(s);
    }

    protected void setUp(){
    }

    protected void tearDown(){
    }

    public void test1(){
	try{
	    GInfoManager infoManager = new GInfoManagerMemory();
	    infoManager.start(new MyInitiator(), false);
	    MyListener listener = new MyListener();
	    
	    String pid = "1234567890";
	    String xml = "<item><key>car</key><value>123</value></item>";
	    Document doc = saxReader.read(new StringReader(xml));
	    Element ele = doc.getRootElement();
	    ele.detach();
	    GInfo i1 = new GInfo(GHash.fromPid(pid), ele, 10010, comparator);
	    
	    infoManager.insertGInfo(i1);
	    
	    resultEle = null;
	    infoManager.queryGInfo("/item[key='bar']", listener);
	    assertNull(resultEle);
	    infoManager.queryGInfo("/item[key='car']", listener);
	    assertNotNull(resultEle);
	    assertEquals(xml, resultEle.asXML());
	    
	    insertInfo = null;
	    infoManager.manageGInfo();
	    assertTrue(i1.equals(insertInfo));

	    // with namespace
	    Namespace ns = new Namespace("", "http://test/foo");
	    Element ele2 = DocumentHelper.createElement(new QName("item", ns));
	    ele2.addAttribute("key", "cat");
	    ele2.addText("234");
	    GInfo i2 = new GInfo(GHash.fromPid(pid), ele2, 10020, comparator);
	    
	    infoManager.insertGInfo(i2);
	    
	    resultEle = null;
	    infoManager.queryGInfo("/item[@key='cat']", listener);
	    assertNull(resultEle); // doesn't work
	    infoManager.queryGInfo("/*[local-name()='item' and namespace-uri()='http://test/foo' and @key='cat']", listener);
	    assertNotNull(resultEle);
	    assertEquals(ele2, resultEle);
	    
	}catch(Exception e){
	    fail(e.toString());
	}
    }

    private class MyInitiator implements GInfoManager.Initiator{
	public void remoteInsertGInfo(GInfo info){
	    insertInfo = info;
	}
	public long getCurrentTime(){
	    return 10000;
	}
    }

    private class MyListener implements GInfoManager.Listener{
	public void notifyResult(Object xml){
	    resultEle = (Element)xml;
	}
    }
}
