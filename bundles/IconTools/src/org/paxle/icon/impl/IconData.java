package org.paxle.icon.impl;

public class IconData {
	public String mimeType = "image/png";
	public byte[] data = null;
	
	public IconData(byte[] data) {
		this.data = data;
	}
	
	public IconData(String mimeType, byte[] data) {
		this.mimeType = mimeType;
		this.data = data;
	}
}
