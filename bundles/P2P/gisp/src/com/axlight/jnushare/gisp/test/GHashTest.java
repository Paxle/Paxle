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
 * $Id: GHashTest.java,v 1.2 2003/05/20 05:58:42 daishi Exp $
 */

package com.axlight.jnushare.gisp.test;

import com.axlight.jnushare.gisp.GHash;

import junit.framework.TestCase;

public class GHashTest extends TestCase{

    public GHashTest(String s){
	super(s);
    }

    protected void setUp(){
    }

    protected void tearDown(){
    }

    public void test1(){
	assertEquals("123456789abcdef0123456789abcdef012345678",
       GHash.fromPid("123456789abcdef0123456789abcdef012345678").toString());
	assertEquals("823456789abcdef0123456789abcdef012345678",
       GHash.fromPid("823456789abcdef0123456789abcdef012345678").toString());
	assertEquals("f23456789abcdef0123456789abcdef012345678",
       GHash.fromPid("f23456789abcdef0123456789abcdef012345678").toString());
	assertEquals("23456789abcdef0123456789abcdef012345678",
       GHash.fromPid("023456789abcdef0123456789abcdef012345678").toString());
	assertEquals("3456789abcdef0123456789abcdef012345678",
       GHash.fromPid("003456789abcdef0123456789abcdef012345678").toString());
	assertEquals("389abcdef0123456789abcdef012345678",
       GHash.fromPid("00389abcdef0123456789abcdef012345678").toString());
	assertEquals("9abcdef01abcdef012345678",
       GHash.fromPid("00009abcdef01abcdef012345678").toString());
	assertEquals("abcdef01abcdef012345678",
       GHash.fromPid("00000abcdef01abcdef012345678").toString());

	GHash h1 = GHash.fromPid("abcdef01abcdef012345678");
	GHash h2 = GHash.fromPid("0abcdef01abcdef012345678");
	GHash h3 = GHash.fromPid("000abcdef01abcdef012345678");
	GHash h4 = GHash.fromPid("00000abcdef01abcdef012345678");
	assertTrue(h1.equals(h2));
	assertTrue(h1.equals(h3));
	assertTrue(h1.equals(h4));
    }

    public void test2(){
	GHash h1 = GHash.fromKey("GHash");
	GHash h2 = GHash.fromPid("37623c8fcade9c049b4670962cb8c6aaf592b291");
	assertTrue(h1.equals(h2));
    }

    public void test3(){
	assertEquals("ac3b", GHash.fromPid("ac3b").toString());
	assertEquals("ac3b", GHash.fromPid("0ac3b").toString());
	assertEquals("ac3b", GHash.fromPid("00ac3b").toString());
	assertEquals("ac3b", GHash.fromPid("00ac3b").toString());
	assertEquals("ac3b", GHash.fromPid("000ac3b").toString());
    }

    public void test4(){
	GHash h1 = GHash.fromPid("ab0052");
	GHash h2 = GHash.fromPid("0b0052");
	GHash h3 = GHash.fromPid("1b0052");
	assertEquals(24, h1.getBitLength());
	assertEquals(20, h2.getBitLength());
	assertEquals(21, h3.getBitLength());
    }

    public void test5(){
	GHash h1 = GHash.fromPid("ab9052");
	assertEquals("ab9053", h1.plusDistance(1).toString());
	assertEquals("ab9050", h1.plusDistance(2).toString());
	assertEquals("ab9056", h1.plusDistance(3).toString());
	assertEquals("ab905a", h1.plusDistance(4).toString());
	assertEquals("ab9042", h1.plusDistance(5).toString());
	assertEquals("ab9072", h1.plusDistance(6).toString());
	assertEquals("ab9012", h1.plusDistance(7).toString());
	assertEquals("ab90d2", h1.plusDistance(8).toString());
	assertEquals("ab9152", h1.plusDistance(9).toString());
	assertEquals("ab9252", h1.plusDistance(10).toString());
	assertEquals("ab9452", h1.plusDistance(11).toString());
	assertEquals("ab9852", h1.plusDistance(12).toString());
	assertEquals("ab8052", h1.plusDistance(13).toString());
	assertEquals("abb052", h1.plusDistance(14).toString());
	assertEquals("abd052", h1.plusDistance(15).toString());
	assertEquals("ab1052", h1.plusDistance(16).toString());
    }

}
