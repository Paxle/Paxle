package org.paxle.p2p.services;

public interface IService {
	public long getReceivedMessageCount();
	
	public long getRecievedBytesCount();
	
	public long getSentMessageCount();
	
	public long getSentBytesCount();
}
