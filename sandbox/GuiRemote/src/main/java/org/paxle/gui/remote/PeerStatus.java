package org.paxle.gui.remote;


public class PeerStatus {
	private long timeStamp;
	private Number value;
	
	public PeerStatus(long timeStamp, Number value) {
		this.timeStamp = timeStamp;
		this.value = value;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public Number getValue() {
		return value;
	}

	public void setValue(Number value) {
		this.value = value;
	}
}
