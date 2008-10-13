
package org.paxle.desktop.impl.dialogues;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.JPanel;

import org.osgi.framework.ServiceRegistration;
import org.paxle.desktop.DIComponent;
import org.paxle.desktop.impl.DesktopServices;

public abstract class DIServicePanel extends JPanel implements DIComponent {
	
	private static final long serialVersionUID = 1L;
	
	public static final String PANEL_SIZE = "windowSize";
	
	protected final DesktopServices services;
	protected Frame frame;
	private final HashMap<Object,ServiceRegistration> regs = new HashMap<Object,ServiceRegistration>();
	
	public DIServicePanel(final DesktopServices services) {
		this(services, null);
	}
	
	public DIServicePanel(final DesktopServices services, final Dimension defaultWindowSize) {
		this.services = services;
		final String dimProp = services.getServiceManager().getServiceProperties().getProperty(getClass().getName() + "_" + PANEL_SIZE);
		Dimension dim = null;
		if (dimProp != null) {
			final int sep = dimProp.indexOf(',');
			if (sep != -1)
				dim = new Dimension(
						Integer.parseInt(dimProp.substring(0, sep)),
						Integer.parseInt(dimProp.substring(sep + 1)));
		}
		if (dim == null)
			dim = defaultWindowSize;
		super.setPreferredSize(dim);
	}
	
	protected synchronized <E> void registerService(final Object key, final E service, final Hashtable<String,?> properties, final Class<? super E>... clazzes) {
		final ServiceRegistration reg = regs.get(key);
		if (reg != null)
			reg.unregister();
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
	
	public void setFrame(final Frame frame) {
		this.frame = frame;
	}
	
	public Container getContainer() {
		return this;
	}
	
	public abstract String getTitle();
	
	public Dimension getWindowSize() {
		return super.getPreferredSize();
	}
	
	public void close() {
		final String dim = String.format("%d,%d", Integer.valueOf(super.getWidth()), Integer.valueOf(super.getHeight()));
		services.getServiceManager().getServiceProperties().setProperty(getClass().getName() + "_" + PANEL_SIZE, dim);
		unregisterServices();
	}
}
