
package org.paxle.desktop.impl.event;

import java.util.EventListener;

public interface OptionChangeListener extends EventListener {
	
	public void optionChanged(OptionChangedEvent e);
	public void optionStateChanged(OptionStateChangedEvent e);
}
