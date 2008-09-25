
package org.paxle.desktop.impl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osgi.service.http.HttpService;

import org.paxle.core.IMWComponent;
import org.paxle.desktop.DIComponent;
import org.paxle.desktop.IDesktopServices.Dialogues;
import org.paxle.desktop.backend.IDIBackend;
import org.paxle.desktop.backend.tray.IMenuItem;
import org.paxle.desktop.backend.tray.IPopupMenu;
import org.paxle.desktop.backend.tray.ISystemTray;
import org.paxle.desktop.backend.tray.ITrayIcon;
import org.paxle.desktop.impl.dialogues.SmallDialog;

public class SystrayMenu implements ActionListener {
	
	private static final String CRAWL_PAUSE = Messages.getString("systrayMenu.crawlPause"); //$NON-NLS-1$
	private static final String CRAWL_RESUME = Messages.getString("systrayMenu.crawlResume"); //$NON-NLS-1$
	
	private static enum Actions {
		SEARCH, BROWSE, CRAWL, CRAWLPR, BUNDLES, STATS, CCONSOLE, SETTINGS, RESTART, QUIT
	}
	
	private static final Log logger = LogFactory.getLog(SystrayMenu.class);
	private final DesktopServices services;
	private final ITrayIcon ti;
	private final ISystemTray systray;
	
	private final IMenuItem searchItem;
	private final IMenuItem browseItem;
	private final IMenuItem crawlItem;
	private final IMenuItem crawlprItem;
	
	private final Timer tooltipTimer = new Timer("DI-TooltipTimer"); //$NON-NLS-1$
	
	// instantiate this here to prevent delays when the popup shows up
	private class PopupMenuUpdater implements PopupMenuListener, ActionListener, Runnable {
		
		private final int startIdx;
		private int end = 0;
		
		public PopupMenuUpdater(final int startIdx) {
			this.startIdx = startIdx;
		}
		
		public void popupMenuCanceled(PopupMenuEvent e) {
		}
		
		public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			final IPopupMenu pm = (IPopupMenu)e.getSource();
			for (int i=startIdx; i<end; i++)
				pm.remove(startIdx);
		}
		
		public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			logger.debug("popup becomes visible, refreshing menu-items"); //$NON-NLS-1$
			SwingUtilities.invokeLater(this);
			
			final IPopupMenu pm = (IPopupMenu)e.getSource();
			final Map<Long,DIComponent> additionalComps = services.getAdditionalComponents();
			end = startIdx;
			if (additionalComps != null && additionalComps.size() > 0) {
				pm.insertSeparator(end++);
				for (final Map.Entry<Long,DIComponent> en : additionalComps.entrySet())
					pm.insert(services.getBackend().createMenuItem(en.getValue().getTitle(), en.getKey().toString(), this), end++);
			}
		}
		
		public void actionPerformed(ActionEvent e) {
			Long id = null;
			try {
				id = Long.valueOf(e.getActionCommand());
			} catch (NumberFormatException ee) {  }
			if (id == null) {
				logger.warn("actionPerformed for unknown actionCommand '" + e.getActionCommand() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
				return;
			}
			
			if (services.show(id) == null) {
				logger.warn("actionPerformed for unknown di-component #" + id.toString() + ""); //$NON-NLS-1$ //$NON-NLS-2$
				return;
			}
		}
		
		public void run() {
			final IMWComponent<?> crawler = services.getMWComponent(DesktopServices.MWComponents.CRAWLER);
			final boolean hasCrawler = (crawler != null);
			crawlItem.setEnabled(hasCrawler);
			crawlprItem.setEnabled(hasCrawler);
			
			final ServiceManager manager = services.getServiceManager();
			final boolean hasWebServer = manager.hasService(HttpService.class);
			final boolean hasWebui = manager.hasService("org.paxle.gui.IServletManager") && hasWebServer; //$NON-NLS-1$
			browseItem.setEnabled(services.isBrowserOpenable() && hasWebui);
			
			final boolean hasSearch = manager.hasService("org.paxle.se.search.ISearchProviderManager"); //$NON-NLS-1$
			// remove "&& hasWebUi" if we have other methods of displaying the searchresults
			searchItem.setEnabled(services.isBrowserOpenable() && hasSearch && hasWebui);
			
			if (hasCrawler)
				crawlprItem.setText((crawler.isPaused()) ? CRAWL_RESUME : CRAWL_PAUSE);
		}
	}
	
	public SystrayMenu(final DesktopServices services, final URL iconResource) {
		this.services = services;
		final IDIBackend backend = services.getBackend();
		final IPopupMenu pm = backend.createPopupMenu(
				this.searchItem 	= backend.createMenuItem(Messages.getString("systrayMenu.search"), Actions.SEARCH.name(), this),		//  0 //$NON-NLS-1$
				null,																														//  1
				this.browseItem		= backend.createMenuItem(Messages.getString("systrayMenu.webinterface"), Actions.BROWSE.name(), this),	//  2 //$NON-NLS-1$
				null,																														//  3
				this.crawlItem 		= backend.createMenuItem(Messages.getString("systrayMenu.crawl"), Actions.CRAWL.name(), this),			//  4 //$NON-NLS-1$
				this.crawlprItem 	= backend.createMenuItem(CRAWL_PAUSE,    Actions.CRAWLPR.name(),  this),								//  5
				backend.createMenuItem(Messages.getString("systrayMenu.crawlingConsole"), Actions.CCONSOLE.name(), this),					//  6 //$NON-NLS-1$
				null,																														//  7
				backend.createMenuItem(Messages.getString("systrayMenu.bundles"),         Actions.BUNDLES.name(),  this),					//  8 //$NON-NLS-1$
				backend.createMenuItem(Messages.getString("systrayMenu.statistics"),      Actions.STATS.name(),    this),					//  9 //$NON-NLS-1$
				backend.createMenuItem(Messages.getString("systrayMenu.settings"),        Actions.SETTINGS.name(), this),					// 10 //$NON-NLS-1$
				null,																														// 11
				backend.createMenuItem(Messages.getString("systrayMenu.restart"), 		  Actions.RESTART.name(),  this), //$NON-NLS-1$
				backend.createMenuItem(Messages.getString("systrayMenu.quit"), 			  Actions.QUIT.name(),     this)); //$NON-NLS-1$
		pm.addPopupMenuListener(new PopupMenuUpdater(11));
		
		systray = backend.getSystemTray();
		systray.add(ti = backend.createTrayIcon(new ImageIcon(iconResource), Messages.getString("systrayMenu.tray"), pm)); //$NON-NLS-1$
		
		tooltipTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				final IMWComponent<?> indexer = services.getMWComponent(DesktopServices.MWComponents.INDEXER);
				if (indexer != null) {
					final StringBuilder sb = new StringBuilder("Paxle"); //$NON-NLS-1$
					final IMWComponent<?> crawler = services.getMWComponent(DesktopServices.MWComponents.CRAWLER);
					
					if (crawler == null || !crawler.isPaused() && crawler.getEnqueuedJobCount() == 0) {
						sb.append(Messages.getString("systrayMenu.idle")); //$NON-NLS-1$
					} else if (crawler.isPaused()) {
						sb.append(Messages.getString("systrayMenu.crawlingPaused")); //$NON-NLS-1$
					} else {
						sb.append(Messages.getString("systrayMenu.crawling")).append(indexer.getPPM()).append(Messages.getString("systrayMenu.ppm")); //$NON-NLS-1$ //$NON-NLS-2$
					}
					ti.setToolTip(sb.toString());
				}
			}
		}, 0L, 1000L);
		
		// desktop integration needs to be quite responsive, therefore we impudently increase
		// the EventQueue's dispatcher thread's priority
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Thread.currentThread().setPriority(8);
			}
		});
	}
	
	// private long tmBecameVisible = 0;
	
	/*
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		EventQueue queue = Toolkit.getDefaultToolkit().getSystemEventQueue();
		AWTEvent event = queue.peekEvent();
		System.out.println("##### invisible -> next event: " + event);
		boolean canceled = false;
		if (event == null)
			canceled = true;
		else {
			if (event instanceof InvocationEvent) try {
				queue.getNextEvent();
				canceled = true;
			} catch (Exception ee) {
				ee.printStackTrace();
			} else if (event instanceof KeyEvent) {
				if (((KeyEvent)event).getKeyChar() == 27)
					canceled = true;
			}
		}
		
		System.out.println("##### Popup menu has been canceled: " + canceled);
	}
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		logger.debug("popup becomes visible, refreshing menu-items");
		EventQueue queue = Toolkit.getDefaultToolkit().getSystemEventQueue();
		try {
			AWTEvent event = queue.peekEvent();
			System.out.println("##### visible -> next event: " + event);
		} catch (Exception ee) {
			ee.printStackTrace();
		}
		SwingUtilities.invokeLater(refresh);
		System.out.println("becoming visible: " + tmBecameVisible);
		Thread.dumpStack();
		if (tmBecameVisible < 0) {
			tmBecameVisible = 0;
		} else {
			tmBecameVisible = System.currentTimeMillis();
		}
	}
	 */
	public void close() {
		tooltipTimer.cancel();
		logger.debug("removing systray icon"); //$NON-NLS-1$
		systray.remove(ti);
		logger.debug("removed systray icon successfully"); //$NON-NLS-1$
	}
	
	/* TODO:
	 * - set location and size of icon to display the dialog at the correct position next to the icon
	 *   - what to do about the JRE6-backend? How to determine the position of the icon on the screen? */
	public void actionPerformed(ActionEvent e) {
		final String cmd = e.getActionCommand();
		final Actions action = Actions.valueOf(cmd);
		if (action == null)
			throw new RuntimeException("unknown action '" + cmd + "'"); //$NON-NLS-1$ //$NON-NLS-2$
		new TrayThread(action).start();
	}
	
	private class TrayThread extends Thread {
		
		private final Actions action;
		
		public TrayThread(final Actions action) {
			this.action = action;
		}
		
		@Override
		public void run() {
			switch (action) {
				case SEARCH: {
					final String data = SmallDialog.showDialog(Messages.getString("systrayMenu.enterQuery"), Messages.getString("systrayMenu.searchButton")); //$NON-NLS-1$ //$NON-NLS-2$
					if (data != null && data.length() > 0)
						services.browseUrl(services.getPaxleUrl("/search?query=", data)); //$NON-NLS-1$
				} break;
					
				case CRAWL: {
					final String data = SmallDialog.showDialog(Messages.getString("systrayMenu.enterUrl"), Messages.getString("systrayMenu.crawlButton")); //$NON-NLS-1$ //$NON-NLS-2$
					if (data != null && data.length() > 0)
						services.startDefaultCrawl(data);
				} break;
					
				case BROWSE:
					services.browseUrl(services.getPaxleUrl("/")); //$NON-NLS-1$
					break;
					
				case CRAWLPR: {
					final IMWComponent<?> crawler = services.getMWComponent(DesktopServices.MWComponents.CRAWLER);
					if (crawler != null) {
						if (crawler.isPaused()) {
							crawler.resume();
						} else {
							crawler.pause();
						}
					}
				} break;
				
				case BUNDLES:
					services.openDialogue(Dialogues.BUNDLES);
					break;
				
				case STATS:
					services.openDialogue(Dialogues.STATS);
					break;
				
				case CCONSOLE:
					services.openDialogue(Dialogues.CCONSOLE);
					break;
					
				case SETTINGS:
					services.openDialogue(Dialogues.SETTINGS);
					break;
				
				case RESTART:
					services.restartFramework();
					break;
				
				case QUIT:
					services.shutdownFramework();
					break;
				
				default:
					throw new RuntimeException("switch-statement does not cover action '" + action + "'"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}
}
