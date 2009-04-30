/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.desktop.impl;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import org.paxle.desktop.DIComponent;
import org.paxle.desktop.IDIEventListener;
import org.paxle.desktop.IDesktopServices;
import org.paxle.desktop.IDesktopServices.Dialogues;
import org.paxle.desktop.impl.dialogues.bundles.BundlePanel;
import org.paxle.desktop.impl.dialogues.cconsole.CrawlingConsole;
import org.paxle.desktop.impl.dialogues.settings.SettingsPanel;
import org.paxle.desktop.impl.dialogues.stats.StatisticsPanel;


public class DialogueServices implements ServiceListener {
	
	/**
	 * A {@link WindowAdapter} which silently removes the associated {@link DIComponent} from the map of active
	 * {@link DesktopServices#serviceFrames}. It is attached to all frames
	 * {@link DesktopServices#createDefaultFrame(DIComponent, Long) created} for displaying an (DI-internal or
	 * external) {@link DIComponent}s.
	 * @see DesktopServices#serviceFrames
	 * @see DesktopServices#show(Long)
	 * @see DesktopServices#close(Long)
	 */
	private class FrameDICloseListener extends WindowAdapter {
		
		private Long id;
		private DIComponent c;
		
		/**
		 * When {@link #windowClosed(WindowEvent)} is invoked by the associated frame, this listener will
		 * first look up the {@link DIComponent} which registered under the <code>id</code> and will then
		 * remove it from {@link DesktopServices#serviceFrames}.
		 * This constructor is used for {@link DIComponent}s which are not defined in this bundle but have
		 * been {@link DesktopServices#serviceChanged(ServiceReference, int) registered} to this bundle.
		 * @see DesktopServices#servicePanels
		 * @param id the {@link Constants#SERVICE_ID} of the {@link DIComponent}
		 */
		public FrameDICloseListener(final Long id) {
			this.id = id;
		}
		
		/**
		 * When {@link #windowClosed(WindowEvent)} is invoked by the associated frame, this listener will
		 * remove the given {@link DIComponent} from {@link DesktopServices#serviceFrames}.
		 * This constructor shall be used when {@link DesktopServices#servicePanels} is known to not contain
		 * the specific {@link DIComponent} as is the case with the {@link IDesktopServices.Dialogues dialogues}
		 * provided by this bundle.
		 * @see DesktopServices#valueOf(org.paxle.desktop.IDesktopServices.Dialogues)
		 * @param c the {@link DIComponent} to remove
		 */
		public FrameDICloseListener(final DIComponent c) {
			this.c = c;
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.awt.event.WindowAdapter#windowClosed(java.awt.event.WindowEvent)
		 */
		@Override
		public void windowClosed(WindowEvent e) {
			final DIComponent c = (this.c != null) ? this.c : servicePanels.get(id);
			if (c != null) {
				c.close();
				serviceFrames.remove(c);
			}
		}
	}
	
	private static final String FILTER = String.format("(%s=%s)", Constants.OBJECTCLASS, DIComponent.class.getName());	// TODO
	
	private static final Log logger = LogFactory.getLog(DialogueServices.class);
	
	private final Set<IDIEventListener> listeners = new HashSet<IDIEventListener>();
	
	private final Hashtable<Long,DIComponent> servicePanels = new Hashtable<Long,DIComponent>();
	private final HashMap<DIComponent,Frame> serviceFrames = new HashMap<DIComponent,Frame>();
	
	private final ServiceManager manager;
	
	public DialogueServices(final ServiceManager manager) {
		this.manager = manager;
	}
	
	public void init() {
		// catch all ServiceEvents for DIComponents
		try {
			manager.addServiceListener(this, FILTER);
			logger.info("added desktop-integration as service-listener for '" + FILTER + "'");
		} catch (InvalidSyntaxException e) { e.printStackTrace(); }
		
		// look for services which already have registered as DIComponents and record them in the map
		try {
			final ServiceReference[] refs = manager.getServiceReferences(null, FILTER);
			if (refs != null)
				for (final ServiceReference ref : refs)
					serviceChanged(ref, ServiceEvent.REGISTERED);
		} catch (InvalidSyntaxException e) { e.printStackTrace(); }
	}
	
	public void shutdown() {
		// close all open dialogues
		for (final Frame frame : serviceFrames.values())
			frame.dispose();
		serviceFrames.clear();
		
		manager.removeServiceListener(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.ServiceListener#serviceChanged(org.osgi.framework.ServiceEvent)
	 */
	public void serviceChanged(ServiceEvent event) {
		serviceChanged(event.getServiceReference(), event.getType());
	}
	
	private void serviceChanged(final ServiceReference ref, final int type) {
		final Long id = (Long)ref.getProperty(Constants.SERVICE_ID);
		logger.debug("received service changed event for " + ref + ", type: " + type);
		if (id == null) {
			logger.error("(un)registered DIComponent has no valid service-id: " + ref);
			return;
		}
		
		switch (type) {
			case ServiceEvent.REGISTERED: {
				// retrieve the service and put it under it's id in the servicePanels-map
				// to be able to access it via this id later
				final DIComponent panel = manager.getService(ref, DIComponent.class);
				if (panel == null) {
					logger.error("tried to register DIComponent with null-reference");
					break;
				}
				servicePanels.put(id, panel);
				final DIServiceEvent event = new DIServiceEvent(id, panel);
				synchronized (this) {
					for (final IDIEventListener l : listeners)
						l.serviceRegistered(event);
				}
				logger.info("registered DIComponent '" + panel.getTitle() + "' with service-ID " + id);
			} break;
			
			case ServiceEvent.UNREGISTERING: {
				// close possibly open dialogue and remove it from the servicePanels-map
				close(id);
				DIComponent panel = servicePanels.get(id);
				if (panel == null) {
					logger.warn("unregistering DIComponent which is unknown to DesktopServices: " + ref);
					break;
				}
				final DIServiceEvent event = new DIServiceEvent(id, panel);
				synchronized (this) {
					for (final IDIEventListener l : listeners)
						l.serviceUnregistering(event);
				}
				servicePanels.remove(id);
				logger.info("unregistered DIComponent '" + panel.getTitle() + "' with service-ID " + id);
				panel = null;
				manager.ungetService(ref);
			} break;
			
			case ServiceEvent.MODIFIED: {
			} break;
		}
	}
	
	public synchronized void addDIEventListener(IDIEventListener listener) {
		listeners.add(listener);
	}
	
	public synchronized void removeDIEventListener(IDIEventListener listener) {
		listeners.remove(listener);
	}
	
	public Map<Long,DIComponent> getAdditionalComponents() {
		return Collections.unmodifiableMap(servicePanels);
	}
	
	/* ========================================================================== *
	 * Dialogue handling
	 * ========================================================================== */
	
	private Frame createDefaultFrame(final DIComponent container, final Long id) {
		return Utilities.instance.setFrameProps(
				new JFrame(),
				container.getContainer(),
				container.getTitle(),
				Utilities.SIZE_PACK,
				true,
				Utilities.LOCATION_BY_PLATFORM,
				null,
				false,
				(id.longValue() < 0L) ? new FrameDICloseListener(container) : new FrameDICloseListener(id));
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.paxle.desktop.IDesktopServices#openDialogue(org.paxle.desktop.IDesktopServices.Dialogues)
	 */
	public void openDialogue(final Dialogues d) {
		final DIComponent c;
		switch (d) {
			case CCONSOLE: c = new CrawlingConsole(manager); break;
			case SETTINGS: c = new SettingsPanel(manager); break;
			case STATS: c = new StatisticsPanel(manager); break;
			case BUNDLES: c = new BundlePanel(manager); break;
			
			default:
				throw new RuntimeException("switch-statement does not cover " + d);
		}
		show(valueOf(d), c);
	}
	
	public Frame show(final Long id) {
		final DIComponent c = servicePanels.get(id);
		if (c == null)
			return null;
		return show(id, c);
	}
	
	public Frame show(final Long id, final DIComponent c) {
		Frame frame = serviceFrames.get(c);
		if (frame == null)
			serviceFrames.put(c, frame = createDefaultFrame(c, id));
		c.setFrame(frame);
		show(frame);
		return frame;
	}
	
	public void close(final Long id) {
		final DIComponent c = servicePanels.get(id);
		if (c != null) {
			final Frame frame = serviceFrames.remove(c);
			if (frame != null)
				frame.dispose();
		}
	}
	
	private static Long valueOf(final Dialogues d) {
		return Long.valueOf(-d.ordinal() - 1L);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.paxle.desktop.IDesktopServices#closeDialogue(org.paxle.desktop.IDesktopServices.Dialogues)
	 */
	public void closeDialogue(final Dialogues d) {
		close(valueOf(d));
	}
	
	private static void show(final Frame frame) {
		final int extstate = frame.getExtendedState();
		if ((extstate & Frame.ICONIFIED) == Frame.ICONIFIED)
			frame.setExtendedState(extstate ^ Frame.ICONIFIED);
		if (!frame.isVisible())
			frame.setVisible(true);
		frame.toFront();
	}

}
