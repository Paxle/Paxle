
package org.paxle.desktop.impl;

import org.paxle.desktop.DIComponent;
import org.paxle.desktop.IDIServiceEvent;

public class DIServiceEvent implements IDIServiceEvent {
	
	private final DIComponent comp;
	private final Long id;
	
	public DIServiceEvent(final Long id, final DIComponent comp) {
		this.id = id;
		this.comp = comp;
	}
	
	public DIComponent getComponent() {
		return comp;
	}
	
	public Long getID() {
		return id;
	}
}
