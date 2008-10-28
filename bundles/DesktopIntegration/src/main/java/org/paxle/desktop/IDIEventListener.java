
package org.paxle.desktop;

public interface IDIEventListener {
	
	public void serviceRegistered(final IDIServiceEvent event);
	public void serviceUnregistering(final IDIServiceEvent event);
}
