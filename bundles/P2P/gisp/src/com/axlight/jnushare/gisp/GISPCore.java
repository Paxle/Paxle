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
 * $Id: GISPCore.java,v 1.27 2003/05/20 05:58:41 daishi Exp $
 */

package com.axlight.jnushare.gisp;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * GISP (Global Information Sharing Prolocol) Core.
 * 
 * this class adds the protocol itself.
 */

public abstract class GISPCore extends GISP implements GInfoManager.Initiator{
    private static final Log LOG =
        LogFactory.getLog(GISPCore.class.getName());

    private static GInfo.Comparator peerComparator = new GInfo.Comparator(){
	    public boolean same(GInfo peer1, GInfo peer2){
		String addr1 = (String)peer1.getData();
		String addr2 = (String)peer2.getData();
		return addr1.equals(addr2);
	    }
	};

    private volatile boolean running = false;
    private Thread mainThread = null;
    private Thread sendThread = null;
    private Thread queryThread = null;
    private GInfoManager infoManager = null;

    protected int dataReplication = 5;
    protected int queryReplication = 5;
    protected int resultPeerLimit = 5;
    protected int errorCountLimit = 10;

    protected int maxPeerInfo = 10000;

    protected int myPeerLifetime = 1 * 60 * 60 * 1000; // 1hour
    protected int myStrengthMin = 1;
    protected int myStrengthMax = 10;
    protected int myStrength = 0;
    protected GHash myHash = null;
    protected long myPeerExpirationTime = 0;
    protected GPeer myPeer = null;
    
    /**
     * local PeerInfo cache
     * key = address (String), value = GPeer
     */
    private Map peerTable = new HashMap();

    /**
     * send queue table.
     * key = address (String), value = SendItem
     */
    private Map sendQueueTable = new HashMap();

    /**
     * query table.
     * key = queryid (String), value = Query
     */
    private Map queryTable = new HashMap();


    /**
     * constructor.
     */
    public GISPCore(){
    }

    protected void setGInfoManager(GInfoManager gim){
	infoManager = gim;
    }

    /**
     * start GISPCore.
     */
    protected void start(boolean useThread){
	LOG.info("start GISPCore");

	if(infoManager == null){
	    LOG.fatal("infoManager == null");
	    throw new RuntimeException("GISPCore.start: infoManager == null");
	}
	infoManager.start(this, useThread);

	running = true;
	if(useThread){
	    mainThread = new MainThread();
	    mainThread.start();
	    sendThread = new SendThread();
	    sendThread.start();
	    queryThread = new QueryThread();
	    queryThread.start();
	}
    }

    /**
     * stop GISPCore.
     */
    protected void stop(){
	LOG.info("stop GISPCore");

	running = false;

	if(infoManager != null){
	    infoManager.stop();
	}
	flushSendQueue();
    }

    /**
     * get the number of known peers.
     *
     * @return the number of peers
     */
    public int getNumOfPeers(){
	synchronized(peerTable){
	    return peerTable.size();
	}
    }

    /**
     * abstract method to get MyAddress.
     *
     */
    protected abstract String getMyAddress();

    /**
     * abstract method to get seed peer address.
     * it may take some time.
     */
    protected abstract String getSeedAddress(int timeout);

    /**
     * abstract method to get the current time.
     * usually it is the same as System.currentTimeMillis()
     */
    public abstract long getCurrentTime();

    /**
     * remoteInsertGInfo.
     * API for GInfoManager
     *
     * @param info: GInfo to insert
     */
    public void remoteInsertGInfo(GInfo info){
	assert info != null;
	
	List sendingList = new ArrayList();
	int counter = 0;
	for(Iterator ite = orderPeers(info.getHash()); ite.hasNext();){
	    GPeer peer = (GPeer)ite.next();
	    if(!info.isSentTo(peer)){
		sendingList.add(peer);
		info.updateSentPeer(peer, getCurrentTime());
	    }
	    if(peer.getErrorcount() <= 0){
		counter++;
	    }
	    if(counter >= dataReplication){
		break;
	    }

  	    Thread.yield();
	}

	if(!sendingList.isEmpty()){
	    if(myPeer != null){
		info.updateSentPeer(myPeer, getCurrentTime());
	    }
	}
	for(Iterator ite = sendingList.iterator(); ite.hasNext();){
	    GPeer peer = (GPeer)ite.next();
	    addItemToSendQueue(peer.getAddress(), new InsertItem(info, peer));
	}
    }

    /**
     * create GPeer.
     */
    protected GPeer createGPeer(String address, GHash pid,
				       long expirationTime){
	return new GPeer(pid, address, expirationTime,
			 getCurrentTime(), peerComparator);
    }

    /**
     * add Query to queryTable.
     * @return assigned queryid (String)
     */
    protected String addToQueryTable(String key, String queryStr,
			     GInfoManager.Listener listener, long timeout){
	Query query = new Query(key, queryStr, listener, timeout);

	synchronized(queryTable){
	    if(!queryTable.containsValue(query)){
		String queryid = "qid-" + Math.random();
		while(queryTable.containsKey(queryid)){
		    queryid = "qid-" + Math.random();
		}
		query.setQueryId(queryid);
		queryTable.put(queryid, query);
		if(queryThread != null){
		    queryThread.interrupt();
		}
		return queryid;
	    }else{
		LOG.debug("addToQueryTable: the same query is already in");
	    }
	    return null;
	}
    }

    /**
     * get query in query table.
     */
    protected GInfoManager.Listener getQueryListener(String queryid){
	synchronized(queryTable){
	    Query query = (Query)queryTable.get(queryid);
	    if(query != null){
		return query.getListener();
	    }
	    return null;
	}
    }

    /**
     * called if new peer info may be ready for the query
     */
    protected void notifyQueryGotNewResultPeer(String queryid){
	synchronized(queryTable){
	    Query query = (Query)queryTable.get(queryid);
	    if(query != null){
		query.setNewResultPeer(true);
	    }
	}
    }

    /**
     * main Thread.
     * to maintain peerinfo, replytable
     */
    private class MainThread extends Thread{
	public void run(){
	    while(running){
		try{
		    maintainLocalData();
		}catch(Exception e){
		    LOG.error("MainThread: unexpected exception:", e);
		}
		try{
		    Thread.sleep(10 * 1000);
		}catch(InterruptedException e){
		    // ignored
		}
	    }
	}
    }

    /**
     * maintain peerinfo, replytable, ginfo.
     * must be called if useThread=false in start()
     */
    public void maintainLocalData(){
	// check if my peerinfo expired
	// and send my peerinfo to peers that doesn't have it.
	updateMyPeerInfo();
	
	// maintain peertable
	// gather peerinfo if needed
	maintainPeerTable();

	// check peerinfo
	checkPeers();
	
	// check replyTable
	checkReplyTable();
    }

    /**
     * abstract method to check replyTable for timeout.
     */
    protected abstract void checkReplyTable();

    /**
     * maintain GInfo.
     * must be called if useThread=false in start()
     */
    public void maintainGInfo(){
	if(infoManager != null){
	    infoManager.manageGInfo();
	}
    }

    protected void insertGInfo(GInfo info){
	if(infoManager != null){
	    infoManager.insertGInfo(info);
	}
    }

    /**
     * query Thread.
     * to process query list
     */
    private class QueryThread extends Thread{
	public void run(){
	    while(running){
		try{
		    maintainQueryTable();
		}catch(Exception e){
		    LOG.error("QueryThread: unexpected exception:", e);
		}
		try{
		    Thread.sleep(1000);
		}catch(InterruptedException e){
		    // ignored
		}
	    }
	}
    }

    /**
     * maintain query list.
     * must be called if useThread=false in start()
     */
    public void maintainQueryTable(){
	String[] keys;
	synchronized(queryTable){
	    keys = (String[])queryTable.keySet().toArray(new String[0]);
	}

	for(int i = 0; i < keys.length; i++){
	    Query query;
	    synchronized(queryTable){
		query = (Query)queryTable.get(keys[i]);
	    }
	    if(query == null){
		continue;
	    }

	    if(query.isFirstTime() && infoManager != null){
		// query local data
		infoManager.queryGInfo(query.getQueryStr(),
				       query.getListener());
		
		prepareQueryMesg(query);
		query.setFirstTime(false);
	    }else{
		if(query.getExpirationTime() > getCurrentTime()){
		    prepareQueryMesg(query);
		}else{
		    synchronized(queryTable){
			queryTable.remove(keys[i]);
		    }
		    query.getListener().notifyResult(null);
		    LOG.debug("maintainQueryTable: deleted expired query");
		}
	    }

  	    Thread.yield();
	}
    }

    /**
     * add query item in send queue if there is a target peer.
     */
    private void prepareQueryMesg(Query query){
	assert query != null;
	
	boolean newPeer;
	synchronized(query){
	    newPeer = query.hasNewResultPeer();
	    query.setNewResultPeer(false);
	}

	if(!newPeer && !query.isFirstTime()){
	    return;
	}

	int counter = 0;
	for(Iterator ite = orderPeers(query.getHash()); ite.hasNext();){
	    GPeer peer = (GPeer)ite.next();
	    if(!query.sentPeerList.contains(peer)){
		query.sentPeerList.add(peer);
		addItemToSendQueue(peer.getAddress(),
				   new QueryItem(query, peer));
	    }
	    counter++;
	    if(counter >= queryReplication){
		break;
	    }
	}
    }

    /**
     * add item to send queue.
     */
    protected void addItemToSendQueue(String address, Item item){
	assert address != null;
	assert item != null;
	assert item instanceof SeenItem ||
	    item instanceof PeerItem ||
	    item instanceof SearchItem ||
	    item instanceof InsertItem ||
	    item instanceof QueryItem ||
	    item instanceof ResultItem;

	synchronized(sendQueueTable){
	    Set set = (Set)sendQueueTable.get(address);
	    if(set == null){
		set = new HashSet();
		sendQueueTable.put(address, set);
	    }
	    set.add(item);
	}
    }

    /**
     * send Thread.
     * to send item in sendQueueTable
     */
    private class SendThread extends Thread{
	public void run(){
	    while(running){
		try{
		    flushSendQueue();
		}catch(Exception e){
		    LOG.error("SendThread: unexpected exception:", e);
		}
		try{
		    Thread.sleep(1 * 1000);
		}catch(InterruptedException e){
		    // ignored
		}
	    }
	}
    }

    /**
     * send SendItem really.
     * must be called if useThread=false in start()
     */
    public abstract void flushSendQueue();

    /**
     * get send queue table
     * return clones send queue table
     */
    protected Map getSendQueueTable(){
	synchronized(sendQueueTable){
	    Map map = new HashMap(sendQueueTable);
	    sendQueueTable.clear();
	    return map;
	}
    }

    /**
     * prepare sending back local-cached peerinfo.
     * this is used when received search mesg
     *
     * @param srcAddr source address to return
     * @param limit limit number of peerinfo to send
     * @param pid pid for limiting peerinfo
     *
     */
    protected void preparePeerMesg(String srcAddr, int limit, String pid){
	if(srcAddr == null){
	    LOG.warn("preparePeerMesg: no source addr");
	    return;
	}

	GPeer peer;
	synchronized(peerTable){
	    peer = (GPeer)peerTable.get(srcAddr);
	}

	Iterator ite;
	if(pid == null){
	    synchronized(peerTable){
		ite = new ArrayList(peerTable.values()).iterator();
	    }
	}else{
	    ite = orderPeers(GHash.fromPid(pid));
	}
	
	int counter = 0;
	if(myPeer != null){
	    // always include my peerinfo regardless of pid
	    counter++;
	    myPeer.updateSentPeer(myPeer, getCurrentTime());
	    addItemToSendQueue(srcAddr, new PeerItem(myPeer, peer));
	}

	while(ite.hasNext()){
	    GPeer peer2 = (GPeer)ite.next();
	    if(myPeer != null){
		peer2.updateSentPeer(myPeer, getCurrentTime());
	    }
	    if(peer != null){
		peer2.updateSentPeer(peer, getCurrentTime());
	    }
	    addItemToSendQueue(srcAddr, new PeerItem(peer2, peer));
	    counter++;
	    if(counter >= limit){
		break;
	    }
	}
    }

    /**
     * prepare result mesg.
     * this is used when received query mesg
     *
     * @param srcAddr source address to return
     * @param queryid this is going to be in "queryid" attribute
     * @param pid PID of query
     * @param queryStr query string, typically xpath
     *
     */
    protected void prepareResultMesg(String srcAddr, String queryid,
				     String pid, String queryStr){
	if(srcAddr == null || queryid == null ||
	   pid == null || queryStr == null){
	    LOG.warn("prepareResultMesg: invalid parameters");
	    return;
	}

	GPeer peer;
	synchronized(peerTable){
	    peer = (GPeer)peerTable.get(srcAddr);
	}

	final ResultItem result = new ResultItem(queryid);
	if(infoManager != null){
	    infoManager.queryGInfo(queryStr, new GInfoManager.Listener(){
		    public void notifyResult(Object xml){
			result.getXmlSet().add(xml);
		    }
		});
	}
	
	Iterator ite = orderPeers(GHash.fromPid(pid));
	int counter = 0;
	while(ite.hasNext()){
	    GPeer peer2 = (GPeer)ite.next();
	    result.getPeerSet().add(peer2);
	    counter++;
	    if(counter >= resultPeerLimit){
		break;
	    }
	}

	addItemToSendQueue(srcAddr, result);
    }

    /**
     * get peerinfo by its address.
     *
     * @param address: peer address to look for.
     * @return GPeer for the address, null if not found.
     */
    protected GPeer getPeerInfo(String address){
	synchronized(peerTable){
	    return (GPeer)peerTable.get(address);
	}
    }

    /**
     * add peerinfo into local cache.
     *
     * @param peer: GPeer to add
     */
    protected void addPeerInfo(GPeer peer){
	assert peer != null;

	if(peer.getAddress().equals(getMyAddress())){
	    LOG.debug("addPeerInfo: mine");
	    return;
	}

	synchronized(peerTable){
	    GPeer old = (GPeer)peerTable.get(peer.getAddress());
	    if(old == null){
		LOG.debug("addPeerInfo: new");
		peerTable.put(peer.getAddress(), peer);
	    }else if(old.equals(peer) &&
		     old.getExpirationTime() < peer.getExpirationTime()){
		if(old.getHash().equals(peer.getHash())){
		    LOG.debug("addPeerInfo: update (etime)");
		    old.update(peer);
		}else{
		    LOG.debug("addPeerInfo: update (all)");
		    peerTable.put(peer.getAddress(), peer);
		}
	    }else{
		LOG.debug("addPeerInfo: known");
	    }
	}
    }

    /**
     * get iterator of peers based on a hash.
     * This function is very slow because it sorts everything.
     * In fact, the previous version should be faster
     * by using a tree. Anyway, this one must be easier to read.
     */
    private Iterator orderPeers(final GHash targetHash){
	assert targetHash != null;

	final List peerList;
	synchronized(peerTable){
	    peerList = new ArrayList(peerTable.values());
	}
	Collections.sort(peerList, new Comparator(){
		public int compare(Object o1, Object o2){
		    assert o1 instanceof GPeer;
		    assert o2 instanceof GPeer;

		    GPeer p1 = (GPeer)o1;
		    GPeer p2 = (GPeer)o2;
		    BigInteger dist1 = p1.getHash().getDistance(targetHash);
		    BigInteger dist2 = p2.getHash().getDistance(targetHash);

		    return dist1.compareTo(dist2);
		}
	    });
	
	return peerList.iterator();
    }

    /**
     * update my peerinfo.
     */
    private void updateMyPeerInfo(){
	boolean update = false;

	if(myPeerExpirationTime <= 0){
	    myPeerExpirationTime = getCurrentTime() + myPeerLifetime;
	}
	
	if(myPeerExpirationTime < getCurrentTime()){
	    myPeerExpirationTime = getCurrentTime() + myPeerLifetime;

	    myStrength++;
	    update = true;
	}

	if(myStrength < myStrengthMin){
	    myStrength = myStrengthMin;
	}
	if(myStrength > myStrengthMax){
	    myStrength = myStrengthMax;
	}

	if(myStrength > 0){
	    if(myHash == null){
		myHash = createMyHash();
		update = true;
	    }
	}else{
	    myHash = null;
	}

	if(myHash == null){
	    myPeer = null;
	}else if(update){
	    // send my peerinfo to some peers
	    myPeer = createGPeer(getMyAddress(), myHash, myPeerExpirationTime);
	    remoteInsertPeerInfo(myPeer);
	}
    }

    /**
     * create my PID.
     * a plan is to create a PID based on the overlay network topology.
     */
    private GHash createMyHash(){
	if(myStrength == 0){
	    return GHash.fromKey(getMyAddress()); // temporal hash
	}else{
	    assert myStrength > 0;
	    // Right now, strength is not cared for making PID.
	    return GHash.fromKey(getMyAddress());
	}
    }

    /**
     * remoteInsertPeerInfo.
     *
     * @param dataPeer GPeer to send
     */
    private void remoteInsertPeerInfo(final GPeer dataPeer){
	assert dataPeer != null;

	GPeer[] peers;
	synchronized(peerTable){
	    peers = (GPeer[])peerTable.values().toArray(new GPeer[0]);
	}

	for(int i = 0; i < peers.length; i++){
	    GPeer peer = peers[i];

	    if(!peer.isPrimary()){
		continue;
	    }
	    if(peer.equals(dataPeer)){
		continue;
	    }
	    if(dataPeer.isSentTo(peer)){
		continue;
	    }
	    dataPeer.updateSentPeer(peer, getCurrentTime());
	    if(myPeer != null){
		dataPeer.updateSentPeer(myPeer, getCurrentTime());
	    }
	    addItemToSendQueue(peer.getAddress(),
			       new PeerItem(dataPeer, peer));

	    Thread.yield();
	}
    }

    /**
     * maintain peertable.
     */
    private void maintainPeerTable(){
	// bootstrap
	if(getNumOfPeers() < 3){
	    String address = getSeedAddress(60 * 1000);
	    if(address != null){
		boolean newAddress = true;
		synchronized(peerTable){
		    if(peerTable.containsKey(address)){
			newAddress = false;
		    }
		}
		if(newAddress){
		    addItemToSendQueue(address, new SearchItem(null));
		    if(LOG.isDebugEnabled()){
			LOG.debug("maintainPeerTable: search item to "
				  + address);
		    }
		}
	    }
	}

	for(int i = 1; i < 160; i++){ // XXX hard-coded number
	    GPeer peer;
	    GHash hash = myHash;
	    if(hash == null){
		hash = createMyHash();
	    }
	    Iterator ite = orderPeers(hash.plusDistance(i));
	    if(ite.hasNext()){
		peer = (GPeer)ite.next();
	    }else{
		break; // no peer
	    }
	    
	    peer.setPrimary(true);

	    if(!peer.isDonePeerSearch()){
		addItemToSendQueue(peer.getAddress(), new SearchItem(peer));
		peer.setDonePeerSearch(true);
	    }
	    Thread.yield();
	}

	// delete some peers if the number exceeds
	if(getNumOfPeers() > maxPeerInfo){
	    LOG.error("the number of peer exceeds the max (not implemented)");
	    //TODO
	}
    }

    /**
     * checkPeers.
     * - delete expired peers
     * - delete dead peers
     * - send peerinfo to remote peers (including mine)
     */
    private void checkPeers(){
	if(myPeer != null){
	    remoteInsertPeerInfo(myPeer);
	}

	GPeer[] peers;
	synchronized(peerTable){
	    peers = (GPeer[])peerTable.values().toArray(new GPeer[0]);
	}

	for(int i = 0; i < peers.length; i++){
	    GPeer peer = peers[i];

	    // XXX not sure if this is good.
	    if(peer.isPrimary()){
		remoteInsertPeerInfo(peer);
	    }
	    
  	    if(peer.getExpirationTime() < getCurrentTime() &&
	       getNumOfPeers() > maxPeerInfo){
  		LOG.debug("deleteOldPeers: deleting expired peer");
		synchronized(peerTable){
		    peerTable.remove(peer.getAddress());
		}
  	    }else if(peer.getErrorcount() > errorCountLimit){
  		LOG.debug("deleteOldPeers: deleting errored peer");
		synchronized(peerTable){
		    peerTable.remove(peer.getAddress());
		}
  	    }

  	    Thread.yield();
  	}
    }

    /**
     * Query.
     * for QueryTable
     */
    private class Query{
  	private GHash hash = null;
	private String queryStr = null;
	private GInfoManager.Listener listener = null;
	private long expirationTime = 0;

	private String queryId = null;
	private List sentPeerList =
	    Collections.synchronizedList(new ArrayList());
	private boolean firsttime = true;
	private boolean newResultPeer = false;

	public Query(String k, String q, GInfoManager.Listener l, long t){
	    this(GHash.fromKey(k), q, l, t);
	}

	public Query(GHash h, String q, GInfoManager.Listener l, long timeout){
	    assert h != null;
	    assert q != null;
	    assert l != null;
	    assert timeout > 0;

	    hash = h;
	    queryStr = q;
	    listener = l;
	    expirationTime = getCurrentTime() + timeout;
	}

	public GHash getHash(){
	    return hash;
	}

	public String getQueryStr(){
	    return queryStr;
	}

	public GInfoManager.Listener getListener(){
	    return listener;
	}

	public long getExpirationTime(){
	    return expirationTime;
	}

	public String getQueryId(){
	    return queryId;
	}

	public void setQueryId(String qid){
	    queryId = qid;
	}

	public List getSentPeerList(){
	    return sentPeerList;
	}

	public boolean isFirstTime(){
	    return firsttime;
	}

	public void setFirstTime(boolean b){
	    firsttime = b;
	}

	public boolean hasNewResultPeer(){
	    return newResultPeer;
	}

	public void setNewResultPeer(boolean b){
	    newResultPeer = true;
	}

	public boolean equals(Object object){
	    if(this == object){
		return true;
	    }
	    
	    if(object == null || !(object instanceof Query)){
		return false;
	    }
	    
	    Query target = (Query)object;
	    if(!hash.equals(target.hash)){
		return false;
	    }
	    if(!queryStr.equals(target.queryStr)){
		return false;
	    }
	    return true;
	}

	public int hashCode(){
	    return hash.hashCode() + queryStr.hashCode();
	}
    }

    /**
     * Item for SendQueue.
     */
    protected interface Item{
	public void replyReceived();
	public void replyTimeout();
    }

    /**
     * SeenItem.
     * for <seen> element
     */
    protected class SeenItem implements Item{
	private String seenMesgid = null;

	public SeenItem(String s){
	    assert s != null;
	    seenMesgid = s;
	}

	public String getSeenMesgid(){
	    return seenMesgid;
	}

	public boolean equals(Object object){
	    if(this == object){
		return true;
	    }
	    
	    if(object == null || !(object instanceof SeenItem)){
		return false;
	    }
	    
	    SeenItem target = (SeenItem)object;
	    return seenMesgid.equals(target.seenMesgid);
	}

	public int hashCode(){
	    return seenMesgid.hashCode();
	}

	public void replyReceived(){
	}
	public void replyTimeout(){
	}
    }

    /**
     * PeerItem.
     * for <peer> element
     */
    protected class PeerItem implements Item{
	private GPeer peer = null;
	private GPeer targetPeer = null;

	public PeerItem(GPeer p, GPeer tp){
	    assert p != null;
	    peer = p;
	    targetPeer = tp; // can be null
	}

	public GPeer getPeer(){
	    return peer;
	}

	public boolean equals(Object object){
	    if(this == object){
		return true;
	    }
	    
	    if(object == null || !(object instanceof InsertItem)){
		return false;
	    }
	    
	    PeerItem target = (PeerItem)object;
	    return peer.equals(target.peer);
	}

	public int hashCode(){
	    return peer.hashCode();
	}

	public void replyReceived(){
	}
	public void replyTimeout(){
	    if(targetPeer != null){
		peer.removeSentPeer(targetPeer);
	    }
	}
    }

    /**
     * SearchItem.
     * for <search> element
     */
    protected class SearchItem implements Item{
	private int limit = -1;
	private GHash hash = null;
	private GPeer targetPeer = null;

	public SearchItem(GPeer tp){
	    targetPeer = tp; // can be null
	}
	
	public int getLimit(){
	    return limit;
	}

	public GHash getHash(){
	    return hash;
	}

	public boolean equals(Object object){
	    if(this == object){
		return true;
	    }
	    
	    if(object == null || !(object instanceof SearchItem)){
		return false;
	    }
	    
	    SearchItem target = (SearchItem)object;

	    if(limit != target.limit){
		return false;
	    }else if(hash == null && target.hash != null){
		return false;
	    }else if(hash != null && !hash.equals(target.hash)){
		return false;
	    }else{
		return true;
	    }

	}

	public int hashCode(){
	    if(hash == null){
		return limit;
	    }else{
		return hash.hashCode() + limit;
	    }
	}

	public void replyReceived(){
	}
	public void replyTimeout(){
	    if(targetPeer != null){
		targetPeer.setDonePeerSearch(false);
	    }
	}
    }

    /**
     * InsertItem.
     * for <insert> element
     */
    protected class InsertItem implements Item{
	private GInfo info = null;
	private GPeer targetPeer = null;

	public InsertItem(GInfo i, GPeer tp){
	    assert i != null;
	    info = i;
	    targetPeer = tp; // can be null
	}

	public GInfo getInfo(){
	    return info;
	}

	public boolean equals(Object object){
	    if(this == object){
		return true;
	    }
	    
	    if(object == null || !(object instanceof InsertItem)){
		return false;
	    }
	    
	    InsertItem target = (InsertItem)object;
	    return info.equals(target.info);
	}

	public int hashCode(){
	    return info.hashCode();
	}

	public void replyReceived(){
	}
	public void replyTimeout(){
	    if(targetPeer != null){
		info.removeSentPeer(targetPeer);
	    }
	}
    }

    /**
     * QueryItem.
     * for <query> element
     */
    protected class QueryItem implements Item{
  	private Query query = null;
	private GPeer targetPeer = null;

	public QueryItem(Query q, GPeer tp){
	    assert q != null;
	    query = q;
	    targetPeer = tp;
	}

	public GHash getHash(){
	    return query.getHash();
	}

	public String getQueryStr(){
	    return query.getQueryStr();
	}

	public String getQueryId(){
	    return query.getQueryId();
	}


	public boolean equals(Object object){
	    if(this == object){
		return true;
	    }
	    
	    if(object == null || !(object instanceof QueryItem)){
		return false;
	    }
	    
	    QueryItem target = (QueryItem)object;
	    return query.equals(target.query);
	}

	public int hashCode(){
	    return query.hashCode();
	}

	public void replyReceived(){
	}
	public void replyTimeout(){
	    if(targetPeer != null){
		query.sentPeerList.remove(targetPeer);
	    }
	}
    }

    /**
     * ResultItem.
     * for <result> element
     */
    protected class ResultItem implements Item{
	private String queryid = null;
	private Set xmlSet = new HashSet();
	private Set peerSet = new HashSet();

	public ResultItem(String id){
	    assert id != null;
	    queryid = id;
	}

	public String getQueryId(){
	    return queryid;
	}

	public Set getXmlSet(){
	    return xmlSet;
	}

	public Set getPeerSet(){
	    return peerSet;
	}

	public boolean equals(Object object){
	    if(this == object){
		return true;
	    }
	    
	    if(object == null || !(object instanceof ResultItem)){
		return false;
	    }
	    
	    ResultItem target = (ResultItem)object;
	    return queryid.equals(target.queryid) &&
		xmlSet.equals(target.xmlSet) &&
		peerSet.equals(target.peerSet);
	}

	public int hashCode(){
	    return queryid.hashCode();
	}
	
	public void replyReceived(){
	}
	public void replyTimeout(){
	}
    }

}

