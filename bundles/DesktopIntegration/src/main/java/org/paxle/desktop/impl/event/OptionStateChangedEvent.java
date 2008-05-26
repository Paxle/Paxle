
package org.paxle.desktop.impl.event;

public class OptionStateChangedEvent extends AOptionChangedEvent {
	
	private final Object reason;
	private final MultipleChangesListener source;
	
	public OptionStateChangedEvent(final MultipleChangesListener source, final Object reason, final long when, final boolean changed) {
		super(when, changed);
		this.source = source;
		this.reason = reason;
	}
	
	public MultipleChangesListener getSource() {
		return source;
	}
	
	public Object getReason() {
		return reason;
	}
}
