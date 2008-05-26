
package org.paxle.desktop.impl.event;

public class OptionChangedEvent extends AOptionChangedEvent {
	
	private final Object source;
	private final Object newValue;
	
	public OptionChangedEvent(final Object source, final Object newValue, final long when, final boolean changed) {
		super(when, changed);
		this.source = source;
		this.newValue = newValue;
	}
	
	public Object getNewValue() {
		return newValue;
	}
	
	public Object getSource() {
		return source;
	}
}
