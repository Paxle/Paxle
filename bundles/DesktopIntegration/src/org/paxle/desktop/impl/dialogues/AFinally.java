
package org.paxle.desktop.impl.dialogues;

public abstract class AFinally implements Runnable {
	
	protected String data;
	
	public void init(String data) {
		this.data = data;
	}
	
	public abstract void run();
}
