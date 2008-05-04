
package org.paxle.desktop.impl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osgi.framework.BundleException;
import org.osgi.service.http.HttpService;

import org.paxle.desktop.backend.IDIBackend;
import org.paxle.desktop.backend.tray.IMenuItem;
import org.paxle.desktop.backend.tray.IPopupMenu;
import org.paxle.desktop.backend.tray.ISystemTray;
import org.paxle.desktop.backend.tray.ITrayIcon;
import org.paxle.desktop.impl.dialogues.AFinally;
import org.paxle.desktop.impl.dialogues.SmallDialog;

public class SystrayMenu implements ActionListener, PopupMenuListener {
	
	private static final String CRAWL_PAUSE = "Pause Crawling";
	private static final String CRAWL_RESUME = "Resume Crawling";
	
	private static enum Actions {
		SEARCH, BROWSE, CRAWL, CRAWLPR, SETTINGS, RESTART, QUIT
	}
	
	private final Log logger = LogFactory.getLog(SystrayMenu.class);
	private final DesktopServices services;
	private final ITrayIcon ti;
	private final ISystemTray systray;
	
	private final IMenuItem searchItem;
	private final IMenuItem browseItem;
	private final IMenuItem crawlItem;
	private final IMenuItem crawlprItem;
	private final IMenuItem settingsItem;
	private final IMenuItem quitItem;
	private final IMenuItem restartItem;
	
	// instantiate this here to prevent delays when the popup shows up
	private final Runnable refresh = new Runnable() {
		public void run() {
			final boolean hasCrawler = services.isCrawlerServiceAvailable();
			crawlItem.setEnabled(hasCrawler);
			crawlprItem.setEnabled(hasCrawler);
			
			final boolean hasWebui = (services.getServiceManager().hasService(HttpService.class) &&
					services.getServiceManager().hasService("org.paxle.gui.IServletManager"));
			browseItem.setEnabled(services.isBrowserOpenable() && hasWebui);
			
			final boolean hasSearch = services.getServiceManager().hasService("org.paxle.se.search.ISearchProviderManager");
			// remove "&& hasWebUi" if we have other methods of displaying the searchresults
			searchItem.setEnabled(services.isBrowserOpenable() && hasSearch && hasWebui);
			
			crawlprItem.setText((services.isCrawlerCorePaused()) ? CRAWL_RESUME : CRAWL_PAUSE);
		}
	};
	
	public SystrayMenu(final DesktopServices services, final URL iconResource) {
		this.services = services;
		final IDIBackend backend = services.getBackend();
		final IPopupMenu pm = backend.createPopupMenu(
				this.searchItem 	= backend.createMenuItem("Search...", 		Actions.SEARCH.name(), 	 this),
				null,
				this.browseItem		= backend.createMenuItem("Webinterface", 	Actions.BROWSE.name(),	 this),
				null,
				this.crawlItem 		= backend.createMenuItem("Crawl...", 		Actions.CRAWL.name(), 	 this),
				this.crawlprItem 	= backend.createMenuItem("Pause Crawling", 	Actions.CRAWLPR.name(),  this),
				null,
				this.settingsItem   = backend.createMenuItem("Settings",        Actions.SETTINGS.name(), this),
				this.restartItem 	= backend.createMenuItem("Restart", 		Actions.RESTART.name(),  this),
				this.quitItem 		= backend.createMenuItem("Quit", 			Actions.QUIT.name(), 	 this));
		pm.addPopupMenuListener(this);

		systray = backend.getSystemTray();
		ti = backend.createTrayIcon(new ImageIcon(iconResource), "Paxle Tray", pm);
		systray.add(this.ti);
		
		// desktop integration needs to be quite responsive, therefore we impudently increase
		// the EventQueue's dispatcher thread's priority
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Thread.currentThread().setPriority(8);
			}
		});
	}
	
	// private long tmBecameVisible = 0;
	
	public void popupMenuCanceled(PopupMenuEvent e) { /* ignore */ }
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { /* ignore */ }
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		logger.debug("popup becomes visible, refreshing menu-items");
		SwingUtilities.invokeLater(refresh);
	}
	
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
	public void shutdown() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				logger.debug("removing systray icon");
				systray.remove(ti);
				logger.debug("removed systray icon successfully");
			}
		});
	}
	
	/* TODO:
	 * - set location and size of icon to display the dialog at the correct position next to the icon
	 *   - what to do about the JRE6-backend? How to determine the position of the icon on the screen? */
	public void actionPerformed(ActionEvent e) {
		final String cmd = e.getActionCommand();
		final Actions action = Actions.valueOf(cmd);
		if (action == null)
			throw new RuntimeException("unknown action '" + cmd + "'");
		
		final TrayRunnable dr = new TrayRunnable(action);
		
		switch (action) {
			case SEARCH:
				new SmallDialog(dr, "Enter query:", "Search").setVisible(true);
				break;
			case CRAWL:
				new SmallDialog(dr, "Enter URL:", "Crawl").setVisible(true);
				break;
			
			default:
				new Thread(dr).start();
				break;
		}
	}
	
	private class TrayRunnable extends AFinally implements Runnable {
		
		private final Actions action;
		
		public TrayRunnable(final Actions action) {
			this.action = action;
		}
		
		@Override
		public void run() {
			switch (action) {
				case SEARCH:
					if (data != null && data.length() > 0)
						services.browseUrl(services.getPaxleUrl("/search?query=", data));
					break;
					
				case CRAWL:
					if (data != null && data.length() > 0) try {
						services.startDefaultCrawl(data);
					} catch (Exception ee) {
						Utilities.showURLErrorMessage("Starting crawl failed: " + ee.getMessage(), data);
						logger.error("Starting crawl of URL '" + data + "' failed: " + ee.getMessage(), ee);
					}
					break;
					
				case BROWSE:
					services.browseUrl(services.getPaxleUrl("/"));
					break;
					
				case CRAWLPR:
					services.toggleCrawlersPR();
					break;
				
				case SETTINGS: {
					services.openSettingsDialog();
				} break;
					
				case RESTART: try {
					services.restartFramework();
				} catch (BundleException e) {
					Utilities.showExceptionBox("error restarting framework", e);
					logger.error("error restarting framework", e);
				} break;
				
				case QUIT: try {
					services.shutdownFramework();
				} catch (BundleException e) {
					Utilities.showExceptionBox("error shutting down framework", e);
					logger.error("error shutting down framework", e);
				} break;
					
				default:
					throw new RuntimeException("switch-statement does not cover action '" + action + "'");
			}
		}
	}
}
