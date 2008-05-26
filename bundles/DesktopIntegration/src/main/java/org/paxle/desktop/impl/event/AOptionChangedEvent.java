
package org.paxle.desktop.impl.event;

abstract class AOptionChangedEvent {
	
	private final long when;
	private final boolean changed;
	
	public AOptionChangedEvent(final long when, final boolean changed) {
		this.when = when;
		this.changed = changed;
	}
	
	public boolean isChanged() {
		return changed;
	}
	
	public long getWhen() {
		return when;
	}
}
