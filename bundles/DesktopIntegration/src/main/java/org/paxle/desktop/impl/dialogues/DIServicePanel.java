
package org.paxle.desktop.impl.dialogues;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.JPanel;

import org.osgi.framework.ServiceRegistration;
import org.paxle.desktop.DIComponent;
import org.paxle.desktop.impl.DesktopServices;

public abstract class DIServicePanel extends JPanel implements DIComponent {
	
	private static final long serialVersionUID = 1L;
	
	protected final DesktopServices services;
	private final HashMap<Object,ServiceRegistration> regs = new HashMap<Object,ServiceRegistration>();
	
	public DIServicePanel(final DesktopServices services) {
		this.services = services;
	}
	
	protected synchronized <E> void registerService(final Object key, final E service, final Hashtable<String,?> properties, final Class<? super E>... clazzes) {
		regs.put(key, services.getServiceManager().registerService(service, properties, clazzes));
	}
	
	protected synchronized void unregisterService(final Object key) {
		final ServiceRegistration reg = regs.remove(key);
		if (reg != null)
			reg.unregister();
	}
	
	private synchronized void unregisterServices() {
		final Iterator<ServiceRegistration> it = regs.values().iterator();
		while (it.hasNext()) {
			try {
				it.next().unregister();
			} catch (IllegalStateException e) { e.printStackTrace(); }
			it.remove();
		}
	}
	
	public abstract String getTitle();
	
	public Dimension getWindowSize() {
		return super.getPreferredSize();
	}
	
	public void close() {
		unregisterServices();
	}
}
