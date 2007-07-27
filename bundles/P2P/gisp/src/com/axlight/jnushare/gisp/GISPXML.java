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
 * $Id: GISPXML.java,v 1.6 2003/05/20 05:58:41 daishi Exp $
 */

package com.axlight.jnushare.gisp;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.io.IOException;

import org.apache.log4j.Logger;

import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.DocumentHelper;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.io.OutputFormat;
import org.dom4j.util.NodeComparator;

/**
 * GISP (Global Information Sharing Prolocol) XML implementation.
 * 
 * GISP by dom4j library.
 */

public abstract class GISPXML extends GISPCore{
    private static final Logger LOG =
	Logger.getLogger(GISPXML.class.getName());

    private static final Namespace GISPNS =
	new Namespace("", "http://gisp.jxta.org/protocol/3.4/beta4");
    private static final QName MESSAGE = new QName("message", GISPNS);
    private static final QName SRC = new QName("src", GISPNS);
    private static final QName SEEN = new QName("seen", GISPNS);
    private static final QName PEER = new QName("peer", GISPNS);
    private static final QName PID = new QName("pid", GISPNS);
    private static final QName ADDR = new QName("addr", GISPNS);
    private static final QName TTL = new QName("ttl", GISPNS);
    private static final QName SENT = new QName("sent", GISPNS);
    private static final QName SEARCH = new QName("search", GISPNS);
    private static final QName LIMIT = new QName("limit", GISPNS);
    private static final QName INSERT = new QName("insert", GISPNS);
    private static final QName XML = new QName("xml", GISPNS);
    private static final QName QUERY = new QName("query", GISPNS);
    private static final QName QSTR = new QName("qstr", GISPNS);
    private static final QName RESULT = new QName("result", GISPNS);
    private static final QName IDATTR = new QName("id");
    private static final QName MESSAGEATTR = new QName("message");
    private static final QName QUERYATTR = new QName("query");
    // for (key, value) style
    private static final QName ITEM = new QName("item", GISPNS);
    private static final QName KEYATTR = new QName("key");

    private static NodeComparator nodeComparator = new NodeComparator();
    private static GInfo.Comparator infoComparator = new GInfo.Comparator(){
	    public boolean same(GInfo info1, GInfo info2){
		Element ele1 = (Element)info1.getData();
		Element ele2 = (Element)info2.getData();
		return nodeComparator.compare(ele1, ele2) == 0;
	    }
	};

    private static SAXReader saxReader = new SAXReader();
    private static OutputFormat outputFormat = null;

    static{
	outputFormat = OutputFormat.createCompactFormat();
	outputFormat.setEncoding("UTF-8");
	outputFormat.setOmitEncoding(true);
    }

    protected int messageSize = 1 * 1000 * 1000; // 1MB
    protected int itemCountPerMessage = 100; // just guessing
    protected int replyTimeout = 2 * 60 * 1000; // 2min

    /**
     * reply table.
     * key = mesgid (String) , value = SendItem
     */
    private Map replyTable = new HashMap();

    /**
     * constructor.
     */
    public GISPXML(){
    }

    /**
     * publish String data with keyword
     * with ttl
     */
    public void insert(String key, String str, long ttl){
	if(key == null || str == null || ttl <= 0){
	    throw new IllegalArgumentException("null argument(s)");
	}
	Element ele = DocumentHelper.createElement(ITEM);
	ele.addAttribute(KEYATTR, key);
	ele.addText(str);
	GInfo info = new GInfo(GHash.fromKey(key), ele,
			       getCurrentTime() + ttl, infoComparator);
	insertGInfo(info);
    }

    /**
     * publish XMLDocument data with keyword.
     * with ttl
     */
    public void insert(String key, byte[] xml, long ttl){
	if(key == null || xml == null || ttl <= 0){
  	    throw new IllegalArgumentException("null argument(s)");
	}
	Element element = convertByteArrayToElement(xml);
	if(element == null){
	    throw new IllegalArgumentException("invalid xml");
	}
	GInfo info = new GInfo(GHash.fromKey(key), element,
			       getCurrentTime() + ttl, infoComparator);
  	insertGInfo(info);
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
    public boolean query(String key, final ResultListener listener,
			 long timeout){
	if(key == null || key.length() <= 0){
	    throw new IllegalArgumentException("invalid keyword");
	}
	if(listener == null || timeout <= 0){
	    throw new IllegalArgumentException("null argument(s)");
	}

	String queryStr = "/*[local-name()='item' and @key='" + key + "']";
	GInfoManager.Listener l = new GInfoManager.Listener(){
		public void notifyResult(Object xml){
		    if(xml != null){
			Element ele = (Element)xml;
			if(ele.getQName().equals(ITEM)){
			    listener.stringResult(ele.getText());
			}
		    }else{
			listener.queryExpired();
		    }
		}
	    };

  	String qid =
	    addToQueryTable(key, queryStr, l, timeout);
	if(qid != null){
	    return true;
	}else{
	    return false;
	}
    }

    /**
     * search XMLDocument data with keyword.
     * with query string
     * with timeout
     */
    public boolean query(String key, String queryStr,
			 final ResultListener listener, long timeout){
  	if(key == null || key.length() <= 0){
  	    throw new IllegalArgumentException("invalid keyword");
  	}
  	if(queryStr == null || queryStr.length() <= 0){
  	    throw new IllegalArgumentException("invalid queryStr");
  	}
	if(listener == null || timeout <= 0){
  	    throw new IllegalArgumentException("null argument(s)");
	}
	
	GInfoManager.Listener l = new GInfoManager.Listener(){
		public void notifyResult(Object xml){
		    if(xml != null){
			byte[] bytes = convertElementToByteArray((Element)xml);
			listener.xmlResult(bytes);
		    }else{
			listener.queryExpired();
		    }
		}
	    };

  	String qid =
	    addToQueryTable(key, queryStr, l, timeout);
	if(qid != null){
	    return true;
	}else{
	    return false;
	}
    }

    /**
     * abstract method to send message to specific address.
     */
    protected abstract void sendMessage(byte[] message, String address);

    /**
     * convert byte[] -> Element.
     */
    private Element convertByteArrayToElement(byte[] xml){
	assert xml != null;

	try{
	    InputStream is = new ByteArrayInputStream(xml);
	    Reader r = new InputStreamReader(is, "UTF-8");
	    Document doc = saxReader.read(r);
	    Element ele = doc.getRootElement();
	    ele.detach();
	    return ele;
	}catch(UnsupportedEncodingException e){
	    LOG.fatal("could not convert (UnsupportedEncodingException)");
	    throw new RuntimeException("UnsupportedEncodingException");
	}catch(DocumentException e){
	    LOG.warn("could not convert: " + e);
	    LOG.debug("convertByteArrayToElement: xml=" + new String(xml));
	    return null;
	}
    }

    /**
     * convert Element -> byte[].
     */
    private byte[] convertElementToByteArray(Element element){
	assert element != null;

	try{
	    ByteArrayOutputStream os = new ByteArrayOutputStream();
	    XMLWriter w = new XMLWriter(os, outputFormat);
	    w.write(element);
	    w.flush();
	    return os.toByteArray();
	}catch(UnsupportedEncodingException e){
	    LOG.fatal("could not convert (UnsupportedEncodingException)");
	    throw new RuntimeException("UnsupportedEncodingException");
	}catch(IOException e){
	    LOG.error("could not convert (IOException)");
	    throw new RuntimeException("IOException");
	}
    }

    /**
     * send SendItem really.
     * must be called if useThread=false in start()
     */
    public void flushSendQueue(){
	Map map = getSendQueueTable();
	for(Iterator ite = map.entrySet().iterator(); ite.hasNext();){
	    Map.Entry entry = (Map.Entry)ite.next();
	    String address = (String)entry.getKey();
	    Set itemSet = (Set)entry.getValue();
	    
	    if(itemSet.size() <= itemCountPerMessage){
		sendItemSet(itemSet, address);
	    }else{
		Set set = new HashSet();
		int count = 0;
		for(Iterator ite2 = itemSet.iterator(); ite2.hasNext();){
		    set.add(ite2.next());
		    count++;
		    if(count >= itemCountPerMessage || !ite2.hasNext()){
			sendItemSet(set, address);
			set.clear();
			count = 0;
			Thread.yield();
		    }
		}
	    }
	}
	Thread.yield();
    }

    /**
     * send item set to the address.
     */
    private void sendItemSet(Set itemSet, String address){
	GPeer targetPeer = getPeerInfo(address); // can be null
	SendItem si = new SendItem(targetPeer, itemSet);

	boolean needReply = false;
	
	Element mesgEle = DocumentHelper.createElement(MESSAGE);
	mesgEle.addElement(SRC).addText(getMyAddress());
	
	for(Iterator ite = si.itemSet.iterator(); ite.hasNext();){
	    Object obj = ite.next();
	    if(obj instanceof SeenItem){
		String seen = ((SeenItem)obj).getSeenMesgid();
		mesgEle.addElement(SEEN).addAttribute(MESSAGEATTR, seen);
	    }
	}

	for(Iterator ite = si.itemSet.iterator(); ite.hasNext();){
	    Object obj = ite.next();
	    if(obj instanceof PeerItem){
		GPeer peer = ((PeerItem)obj).getPeer();
		needReply = true;
		appendGPeerToElement(peer, mesgEle);
	    }
	}
	 
	for(Iterator ite = si.itemSet.iterator(); ite.hasNext();){
	    Object obj = ite.next();
	    if(obj instanceof SearchItem){
		SearchItem search = (SearchItem)obj;
		needReply = true;
		Element searchEle = mesgEle.addElement(SEARCH);
		if(search.getHash() != null){
		    searchEle.addElement(PID)
			.addText(search.getHash().toString());
		}
		if(search.getLimit() > 0){
		    searchEle.addElement(LIMIT)
			.addText("" + search.getLimit());
		}
	    }
	}

	for(Iterator ite = si.itemSet.iterator(); ite.hasNext();){
	    Object obj = ite.next();
	    if(obj instanceof InsertItem){
		GInfo info = ((InsertItem)obj).getInfo();
		long ttl = info.getExpirationTime() - getCurrentTime();
		if(ttl <= 0){
		    continue;
		}
		needReply = true;
		Element insertEle = mesgEle.addElement(INSERT);
		insertEle.addElement(PID).addText(info.getHash().toString());
		Element ele = (Element)info.getData();
		insertEle.addElement(XML)
		    .add((Element)ele.clone());
		insertEle.addElement(TTL).addText("" + ttl);
		GPeer[] sentPeers = info.getSentPeers();
		for(int j = 0; j < sentPeers.length; j++){
		    insertEle.addElement(SENT)
			.addText(sentPeers[j].getAddress());
		}
	    }
	}

	for(Iterator ite = si.itemSet.iterator(); ite.hasNext();){
	    Object obj = ite.next();
	    if(obj instanceof QueryItem){
		QueryItem qi = (QueryItem)obj;
		needReply = true;
		Element queryEle = mesgEle.addElement(QUERY);
		queryEle.addAttribute(IDATTR, qi.getQueryId());
		queryEle.addElement(PID).addText(qi.getHash().toString());
		queryEle.addElement(QSTR).addText(qi.getQueryStr());
	    }
	}
	
	for(Iterator ite = si.itemSet.iterator(); ite.hasNext();){
	    Object obj = ite.next();
	    if(obj instanceof ResultItem){
		ResultItem result = (ResultItem)obj;
		appendResultToElement(result, mesgEle);
	    }
	}

	if(needReply && si.getPeer() != null){
	    si.setSentTime(getCurrentTime());
	    String mesgid = addToReplyTable(si);
	    mesgEle.addAttribute(IDATTR, mesgid);
	}

	byte[] mesgBuf = convertElementToByteArray(mesgEle);
	if(mesgBuf.length <= messageSize){
	    sendMessage(mesgBuf, address);
	}else{
	    LOG.warn("couldn't send message: mesgBuf.length > messageSize");
	    if(LOG.isInfoEnabled()){
		LOG.info("BIG mesgBuf=" + mesgBuf);
	    }
	    int itemSize = mesgBuf.length / itemCountPerMessage; // average
	    itemCountPerMessage -= (mesgBuf.length - messageSize) / itemSize;
	    if(itemCountPerMessage < 1){
		LOG.error("couldn't reduce itemCountPerMessage");
		itemCountPerMessage = 1;
	    }else{
		if(LOG.isInfoEnabled()){
		    LOG.info("itemCountPerMessage reduced to: " +
			     itemCountPerMessage);
		}
	    }
	}
    }

    /**
     * append GPeer to Element.
     */
    private void appendGPeerToElement(GPeer peer, Element ele){
	long ttl = peer.getExpirationTime() - getCurrentTime();
	if(ttl <= 0){
	    return;
	}
	Element peerEle = ele.addElement(PEER);
	peerEle.addElement(PID).addText(peer.getHash().toString());
	peerEle.addElement(ADDR).addText(peer.getAddress());
	peerEle.addElement(TTL).addText("" + ttl);
	GPeer[] sentPeers = peer.getSentPeers();
	for(int i = 0; i < sentPeers.length; i++){
	    peerEle.addElement(SENT).addText(sentPeers[i].getAddress());
	}
    }

    /**
     * append ResultItem to Element.
     */
    private void appendResultToElement(ResultItem result, Element ele){
	Element resultEle = ele.addElement(RESULT);
	resultEle.addAttribute(QUERYATTR, result.getQueryId());
	for(Iterator ite = result.getXmlSet().iterator(); ite.hasNext();){
	    Element xml = (Element)ite.next();
	    resultEle.addElement(XML).add((Element)xml.clone());
	}
	for(Iterator ite = result.getPeerSet().iterator(); ite.hasNext();){
	    GPeer peer = (GPeer)ite.next();
	    appendGPeerToElement(peer, resultEle);
	}
    }

    /**
     * receive Message and process it.
     *
     */
    protected void processMessage(byte[] mesgBuf){
	Element mesgEle = convertByteArrayToElement(mesgBuf);
	if(mesgEle == null){
	    LOG.warn("processMessage: invalid message (couldn't parse)");
	    return;
	}
	if(!mesgEle.getQName().equals(MESSAGE)){
	    LOG.warn("processMessage: invalid message (version mismatch)");
	    return;
	}

	String mesgid = mesgEle.attributeValue(IDATTR);
	String srcAddr = mesgEle.elementText(SRC);

	if(mesgid != null && srcAddr != null){
	    GPeer peer = getPeerInfo(srcAddr);
	    addItemToSendQueue(srcAddr, new SeenItem(mesgid));
	}
	    
	// seen item
	for(Iterator ite = mesgEle.elementIterator(SEEN); ite.hasNext();){
	    Element seenEle = (Element)ite.next();
	    String seenMesgid = seenEle.attributeValue(MESSAGEATTR);
	    replyReceived(seenMesgid);
	}

	// peer item
	for(Iterator ite = mesgEle.elementIterator(PEER); ite.hasNext();){
	    Element peerEle = (Element)ite.next();
	    GPeer peer = makeGPeerFromElement(peerEle);
	    if(peer != null){
		addPeerInfo(peer);
	    }
	}
	// peer item sent item
	for(Iterator ite = mesgEle.elementIterator(PEER); ite.hasNext();){
	    Element peerEle = (Element)ite.next();
	    String address = peerEle.elementText(ADDR);
	    GPeer peer = getPeerInfo(address);
	    if(peer == null){
		continue;
	    }
	    for(Iterator ite2 = peerEle.elementIterator(SENT);
		ite2.hasNext();){
		Element sentElement = (Element)ite2.next();
		String sent = sentElement.getText();
		GPeer sentPeer = getPeerInfo(sent);
		if(sentPeer != null){
		    peer.updateSentPeer(sentPeer, getCurrentTime());
		}
	    }
	}

	// search item
	for(Iterator ite = mesgEle.elementIterator(SEARCH); ite.hasNext();){
	    Element searchEle = (Element)ite.next();
	    LOG.debug("processMessage: search item");
	    int limit = -1;
	    String limitStr = searchEle.elementText(LIMIT);
	    try{
		if(limitStr != null){
		    limit = Integer.parseInt(limitStr);
		}
	    }catch(NumberFormatException e){
		LOG.warn("NumberFormatException: limit");
	    }
	    String pid = searchEle.elementText(PID);
	    preparePeerMesg(srcAddr, limit, pid);
	}

	// insert item
	for(Iterator ite = mesgEle.elementIterator(INSERT); ite.hasNext();){
	    Element insertEle = (Element)ite.next();
	    LOG.debug("processMessage: insert item");
	    GInfo info = makeGInfoFromElement(insertEle);
	    if(info != null){
		insertGInfo(info);
	    }
	}

	// query item
	for(Iterator ite = mesgEle.elementIterator(QUERY); ite.hasNext();){
	    Element queryEle = (Element)ite.next();
	    LOG.debug("processMessage: query item");
	    String queryid = queryEle.attributeValue(IDATTR);
	    String pid = queryEle.elementText(PID);
	    String queryStr = queryEle.elementText(QSTR);
	    prepareResultMesg(srcAddr, queryid, pid, queryStr);
	}

	// result item
	for(Iterator ite = mesgEle.elementIterator(RESULT); ite.hasNext();){
	    Element resultEle = (Element)ite.next();
	    LOG.debug("processMessage: result item");
	    String queryid = resultEle.attributeValue(QUERYATTR);
	    GInfoManager.Listener listener = getQueryListener(queryid);
	    if(listener == null){
		LOG.debug("processMessage: result no matching query");
		continue;
	    }
	    for(Iterator ite2 = resultEle.elementIterator(XML);
		ite2.hasNext();){
		Element xmlEle = (Element)ite2.next();
		try{
		    Element xml = (Element)xmlEle.elements().get(0);
		    listener.notifyResult(xml);
		}catch(NullPointerException e){
		    LOG.warn("processMessage: no xml in element");
		}catch(IndexOutOfBoundsException e){
		    LOG.warn("processMessage: no xml in element");
		}
	    }
	    for(Iterator ite2 = resultEle.elementIterator(PEER);
		ite2.hasNext();){
		Element peerEle = (Element)ite2.next();
		GPeer peer = makeGPeerFromElement(peerEle);
		// XXX ignoring <sent> information
		if(peer != null){
		    addPeerInfo(peer);
		    notifyQueryGotNewResultPeer(queryid);
		}
	    }
	}
    }

    /**
     * make GPeer from Element.
     * ignore <sent> for this time
     */
    private GPeer makeGPeerFromElement(Element ele){
	assert ele != null;
	assert ele.getQName().equals(PEER);

	String pid = ele.elementText(PID);
	String addr = ele.elementText(ADDR);

	if(pid == null || addr == null){
	    LOG.warn("makeGPeerFromElement: no pid nor addr");
	    return null;
	}

	String ttlStr = ele.elementText(TTL);
	long ttl = 0;
	try{
	    ttl = Long.parseLong(ttlStr);
	}catch(NullPointerException e){
	    LOG.warn("makeGPeerFromElement: no ttl");
	    return null;
	}catch(NumberFormatException e){
	    LOG.warn("makeGPeerFromElement: invalid ttl");
	    return null;
	}
	if(ttl <= 0){
	    LOG.warn("makeGPeerFromElement: ttl <= 0");
	    return null;
	}
	// XXX should also check too big ttl

	return createGPeer(addr, GHash.fromPid(pid), getCurrentTime() + ttl);
    }

    /**
     * make GInfo from Element.
     */
    private GInfo makeGInfoFromElement(Element ele){
	assert ele != null;
	assert ele.getQName().equals(INSERT);

	String pid = ele.elementText(PID);
	if(pid == null){
	    LOG.warn("makeGInfoFromElement: no pid");
	    return null;
	}

	Element xml = null;
	try{
	    xml = (Element)ele.element(XML).elements().get(0);
	}catch(NullPointerException e){
	    LOG.warn("makeGInfoFromElement: no xml in element");
	    return null;
	}catch(IndexOutOfBoundsException e){
	    LOG.warn("makeGInfoFromElement: no xml in element");
	    return null;
	}
	xml.detach();

	String ttlStr = ele.elementText(TTL);
	long ttl = 0;
	try{
	    ttl = Long.parseLong(ttlStr);
	}catch(NullPointerException e){
	    LOG.warn("makeGPeerFromElement: no ttl");
	    return null;
	}catch(NumberFormatException e){
	    LOG.warn("makeGPeerFromElement: invalid ttl");
	    return null;
	}
	if(ttl <= 0){
	    LOG.warn("makeGPeerFromElement: ttl <= 0");
	    return null;
	}
	// XXX should also check too big ttl

 	GInfo info = new GInfo(GHash.fromKey(pid), xml,
			       getCurrentTime() + ttl, infoComparator);

	for(Iterator ite = ele.elementIterator(SENT); ite.hasNext();){
	    Element sentElement = (Element)ite.next();
	    String sent = sentElement.getText();
	    GPeer sentPeer = getPeerInfo(sent);
	    if(sentPeer != null){
		info.updateSentPeer(sentPeer, getCurrentTime());
	    }
	}
	return info;
    }

    /**
     * add send item to reply table.
     * @param si SendItem to add
     * @return mesgid (String)
     */
    private String addToReplyTable(SendItem si){
	String mesgid = "mid-" + Math.random();
	synchronized(replyTable){
	    while(replyTable.containsKey(mesgid)){
		mesgid = "mid-" + Math.random();
	    }
	    replyTable.put(mesgid, si);
	}
	return mesgid;
    }

    /**
     * reply message received.
     * @param mesgid (String) is a received message id.
     */
    private void replyReceived(String mesgid){
	synchronized(replyTable){
	    SendItem si = (SendItem)replyTable.get(mesgid);
	    if(si != null){
		LOG.debug("replyReceived: seen item received");
		si.replyReceived();
		replyTable.remove(mesgid);
	    }else{
		LOG.debug("replyReceived: seen item already cleared");
	    }
	}
    }

    /**
     * check replyTable for timeout.
     */
    protected void checkReplyTable(){
	Object[] objs;
	synchronized(replyTable){
	    objs = replyTable.keySet().toArray();
	}

  	for(int i = 0; i < objs.length; i++){
	    synchronized(replyTable){
		SendItem si = (SendItem)replyTable.get(objs[i]);
		if(si == null){
		    continue;
		}
		if(si.getSentTime() + replyTimeout > getCurrentTime()){
		    continue;
		}
		si.replyTimeout();
		replyTable.remove(objs[i]);
	    }
	    Thread.yield();
  	}
    }

    /**
     * SendItem.
     * for replyTable
     */
    private class SendItem{
	private GPeer peer;
	private Set itemSet;

	private long sentTime = 0;

	public SendItem(GPeer p, Set set){
	    assert set != null;

	    peer = p; // can be null
	    itemSet = set;
	}

	public GPeer getPeer(){
	    return peer;
	}

	public long getSentTime(){
	    return sentTime;
	}

	public void setSentTime(long st){
	    sentTime = st;
	}

  	public synchronized void replyReceived(){
	    if(peer != null){
		peer.replyReceived(getCurrentTime());
	    }
	    for(Iterator ite = itemSet.iterator(); ite.hasNext();){
		Item item  = (Item)ite.next();
		item.replyReceived();
	    }
	}

  	public synchronized void replyTimeout(){
	    if(peer != null){
		peer.replyTimeout(sentTime);
	    }
	    for(Iterator ite = itemSet.iterator(); ite.hasNext();){
		Item item  = (Item)ite.next();
		item.replyTimeout();
	    }
	}
    }

}

