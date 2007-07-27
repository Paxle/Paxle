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
 * $Id: GHash.java,v 1.12 2003/05/20 05:58:41 daishi Exp $
 */

package com.axlight.jnushare.gisp;

import java.util.Map;
import java.util.HashMap;
import java.util.WeakHashMap;
import java.math.BigInteger;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * GHash.
 * 
 * hash function is SHA-1
 * hence, hash value is typically 160bit number,
 * but it does not have to be.
 * 
 */

public class GHash{
    
    private static String PLUSSEP = "+";

    private static final BigInteger TWO = BigInteger.valueOf(2);

    private static Map keyMap = null;
    private static Map pidMap = null;
    static{
	try{
	    keyMap = new WeakHashMap();
	    pidMap = new WeakHashMap();
	}catch(Exception e){
	    keyMap = new HashMap();
	    pidMap = new HashMap();
	}
    }

    private BigInteger baseint = null;
    private String str = null;

    /**
     * private constructor.
     */
    private GHash(){
    }

    /**
     * create hash from String to be hashed.
     * 
     */
    public static synchronized GHash fromKey(String s){
	assert s != null;

	String idstr = s;
	if(keyMap.containsKey(idstr)){
	    return (GHash)keyMap.get(idstr);
	}

	byte[] outbuf;
	try{
	    MessageDigest md = MessageDigest.getInstance("SHA1");
	    byte[] inbuf = s.getBytes("UTF-8");
	    outbuf = md.digest(inbuf);
	}catch(NoSuchAlgorithmException e){
	    throw new RuntimeException("GHash: NoSuchAlgorithmException:"
				       + e.getMessage());
	}catch(UnsupportedEncodingException e){
	    throw new RuntimeException("GHash: UnsupportedEncodingException:"
				       + e.getMessage());
	}
	if(outbuf.length != 20){
	    throw new RuntimeException("GHash: SHA1 length is not 20");
	}

	BigInteger bi = new BigInteger(1, outbuf);
	    
	GHash hash = new GHash();
	hash.baseint = bi;
	keyMap.put(idstr, hash);
	return hash;
    }

    /**
     * create hash from hash String expression.
     * which is like "123456789abcdef0123456789abcdef012345678"
     */
    public static synchronized GHash fromPid(String s){
	assert s != null;

	String idstr = s;
	if(pidMap.containsKey(idstr)){
	    return (GHash)pidMap.get(idstr);
	}

	BigInteger bi = makeBigIntFromStr(s);
	    
	GHash hash = new GHash();
	hash.baseint = bi;
	pidMap.put(idstr, hash);
	return hash;
    }

    /**
     * make BigInteger from hex representation.
     * ex. "abcd1234cdef5678"
     */
    private static BigInteger makeBigIntFromStr(String s){
	assert s != null;
	if(s.length() % 2 == 1){
	    s = "0" + s;
	}

	int size = s.length() / 2;
	byte[] buf = new byte[size];
	for(int i = 0; i < size ; i++){
	    buf[i] = (byte)Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16);
	}

	return new BigInteger(1, buf);
    }

    /**
     * equals method.
     * (needed for Hashtable key)
     */
    public boolean equals(Object object){
        if(this == object){
            return true;
	}

        if(object == null || !(object instanceof GHash)){
	    return false;
	}

	GHash target = (GHash)object;
	return baseint.equals(target.baseint);
    }

    /**
     * return String expression of this hash.
     * which is like "123456789abcdef0123456789abcdef012345678"
     */
    public synchronized String toString(){
        if(str == null){
	    str = baseint.toString(16);
        }
        return str;
    }

    /**
     * hashCode method.
     * (needed for Hashtable key)
     */
    public int hashCode(){
	return baseint.hashCode();
    }

    /**
     * get bit length.
     */
    public int getBitLength(){
	return baseint.bitLength();
    }

    /**
     * return GHash based on the distance from this.
     * @param index to specify the distance 2^(index-1)
     */
    public GHash plusDistance(int index){
	assert index > 0;

	String idstr = toString() + PLUSSEP + index;
	if(pidMap.containsKey(idstr)){
	    return (GHash)pidMap.get(idstr);
	}
	
	GHash hash = new GHash();
	hash.baseint = baseint.xor(TWO.pow(index - 1));
	pidMap.put(idstr, hash);
	return hash;
    }

    /**
     * return the distance (XOR) between this and the GHash.
     */
    public BigInteger getDistance(GHash targetHash){
	assert baseint.signum() >= 0;
	assert targetHash.baseint.signum() >= 0;

	return baseint.xor(targetHash.baseint);
    }

}
