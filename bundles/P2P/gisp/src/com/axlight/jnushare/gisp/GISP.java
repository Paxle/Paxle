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
 * $Id: GISP.java,v 1.13 2003/02/15 09:19:44 daishi Exp $
 */

package com.axlight.jnushare.gisp;

import java.io.UnsupportedEncodingException;

/**
 *  Applications' interface to GISP functionality.
 *  GISP, the Global Information Sharing Protocol, provides a distributed
 *  database mapping keys to values.
 *  This interface provides methods for querying the database and for
 *  inserting new mappings. An instance of this object represents a
 *  connection to one distributed database; if more than one database is used,
 *  for example using different JXTA peer groups, <code>GISP</code> objects
 *  need to be created for each of them.
 *  <p>
 *  Queries are made through <code>query</code> methods,
 *  while new key/value pairs are published through <code>insert</code>
 *  methods. Queries, which can naturally be time-consuming, are not made
 *  on the same thread. Therefore, the <code>query</code> methods
 *  actually return <code>boolean</code> to only show the status.
 *  When the query returns a result, <code>ResultListener</code>
 *  associated with the <code>query</code> is notified.
 *  (Note that there is no guarantee that a query will return a result.)
 *  If the query is expired (timeout specified), it will be notified.
 *  <p>
 *  In each key/value pair, the key is a <code>String</code>. The value,
 *  on the other hand, can be either a <code>String</code> or an XML document,
 *  stored in a <code>byte[]</code> array. The simplest <code>insert</code>
 *  methods therefore are <code>insert(String keyword, String strdata)</code>
 *  and <code>insert(String keyword, byte[] xmldata)</code> for publishing
 *  new key/value pairs with <code>String</code> and XML values, respectively.
 *  Both <code>String</code> and XML values should be small: for example,
 *  rather than containing a file, they should contain information about which
 *  peer can be used to download a file.
 *  <p>
 *  When calling a <code>query</code> method, you don't specify whether you
 *  want a <code>String</code> or an XML result; rather, the
 *  <code>stringResult(String data)</code> and
 *  <code>xmlResult(byte[] data)</code> are called, respectively, for each
 *  <code>String</code> and XML value found whose key matches the searched key.
 *  <p>
 *  There are two types of <code>query</code> methods:
 *  query by keyword and query by xpath.
 *  query by keyword simply finds all key/value pairs with
 *  a given key. query by xpath finds all key/value
 *  pairs with a given key and whose value match a specified xpath.
 *  <p>
 *  Both the <code>insert</code> and the <code>query</code>
 *  methods take optional additional arguments: To the parameters of
 *  each <code>insert</code> method, you can add a <code>long</code>
 *  that specifies when the key/value pair expires (is removed
 *  from the database). To the parameters of each <code>query</code>
 *  method, you can add a <code>long</code> specifying the timeout
 *  for the query, in milliseconds (i.e., the time the database will
 *  spend trying to find the requested key/value pair).
 *  Examples: <code>insert("myKeyword", "myData", 1000000)</code>;
 *  <code>queryByKeyword("myKeyword", 10000)</code>.
 *  If no expiration times/timeouts are specified, reasonable
 *  defaults are used.
 *  <p>
 *  For the default implementation of this interface, see
 *  <code>GISPImpl</code> and <code>GISPCore</code>.
 */

public abstract class GISP{

    /**
     * publish String data with keyword
     * with default ttl 30min
     *
     * @param key: keyword for the data
     * @param str: string data
     */
    public void insert(String key, String str){
	insert(key, str, 30 * 60 * 1000);
    }

    /**
     * publish String data with keyword
     * with ttl
     *
     * @param key: keyword for the data
     * @param str: string data
     * @param ttl: milliseconds to live
     */
    public abstract void insert(String key, String str, long ttl);

    /**
     * publish XMLDocument data with keyword
     * with default ttl 30min
     *
     * @param key: keyword for the data
     * @param xml: any bytearray data, typically XML data
     */
    public void insert(String key, byte[] xml){
	insert(key, xml, 30 * 60 * 1000);
    }

    /**
     * publish XMLDocument data with keyword
     * with ttl
     *
     * @param key: keyword for the data
     * @param xml: any bytearray data, typically XML data
     * @param ttl: milliseconds to live
     */
    public abstract void insert(String key, byte[] xml, long ttl);

    /**
     * search String data and XMLDocument with keyword
     * with default timeout 2min
     *
     * @param key: search keyword
     * @param l: ResultListener to get results
     * @return true if the query is started, false if the query is duplicated.
     */
    public boolean query(String key, ResultListener l){
	return query(key, l, 2 * 60 * 1000);
    }

    /**
     * search String data and XMLDocument with keyword
     * with timeout
     *
     * @param key: search keyword
     * @param l: ResultListener to get results
     * @param timeout: effective period for this query (in millisecond)
     * @return true if the query is started, false if the query is duplicated.
     */
    public abstract boolean query(String key, ResultListener l, long timeout);

    /**
     * search String data and XMLDocument with keyword
     * with queryStr
     * with default timeout 2min
     *
     * @param key: search keyword
     * @param queryStr: query string, typically XPath
     * @param l: ResultListener to get results
     * @return true if the query is started, false if the query is duplicated.
     */
    public boolean query(String key, String queryStr, ResultListener l){
	return query(key, queryStr, l, 2 * 60 * 1000);
    }

    /**
     * search String data and XMLDocument with keyword
     * with queryStr
     * with timeout
     *
     * @param key: search keyword
     * @param queryStr: query string, typically XPath
     * @param l: ResultListener to get results
     * @param timeout: effective period for this query (in millisecond)
     * @return true if the query is started, false if the query is duplicated.
     */
    public abstract boolean query(String key, String queryStr,
				  ResultListener l, long timeout);

    /**
     * Get the number of peers that currently participate in the
     * distributed database.
     *
     * @return the number of peers
     */
    public abstract int getNumOfPeers();

    /**
     * Set GInfoManager for this GISP service.
     */
    protected abstract void setGInfoManager(GInfoManager gim);

}

