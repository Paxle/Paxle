
package org.paxle.desktop.impl;

import java.awt.Dimension;
import java.awt.Frame;
import java.util.HashMap;
import java.util.Hashtable;

import javax.swing.JPanel;

import org.osgi.framework.ServiceRegistration;
import org.paxle.desktop.DIComponent;
import org.paxle.desktop.impl.DesktopServices;

public abstract class DIServicePanel extends JPanel implements DIComponent {
	
	private static final long serialVersionUID = 1L;
	
	// package private
	Frame thisFrame = null;
	
	protected final DesktopServices services;
	private final HashMap<Object,ServiceRegistration> regs = new HashMap<Object,ServiceRegistration>();
	
	public DIServicePanel(final DesktopServices services) {
		this(services, true);
	}
	
	public DIServicePanel(final DesktopServices services, final boolean doubleBuffered) {
		super(doubleBuffered);
		this.services = services;
	}
	
	protected <E> void registerService(final Object key, final E service, final Hashtable<String,?> properties, final Class<? super E>... clazzes) {
		regs.put(key, services.getServiceManager().registerService(service, properties, clazzes));
	}
	
	protected void unregisterService(final Object key) {
		final ServiceRegistration reg = regs.remove(key);
		if (reg != null)
			reg.unregister();
	}
	
	private void unregisterServices() {
		for (final ServiceRegistration reg : regs.values()) try {
			reg.unregister();
		} catch (IllegalStateException e) { e.printStackTrace(); }
		regs.clear();
	}
	
	public void shutdown() {
		unregisterServices();
	}
	
	public abstract String getTitle();
	public abstract Dimension getWindowSize();
}
