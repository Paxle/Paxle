
package org.paxle.desktop.impl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.http.HttpService;

import org.paxle.core.IMWComponent;
import org.paxle.core.queue.CommandProfile;
import org.paxle.core.queue.ICommandProfile;
import org.paxle.desktop.backend.IDIBackend;
import org.paxle.desktop.backend.tray.IMenuItem;
import org.paxle.desktop.backend.tray.IPopupMenu;
import org.paxle.desktop.backend.tray.ITrayIcon;
import org.paxle.desktop.impl.dialogues.AFinally;
import org.paxle.desktop.impl.dialogues.SmallDialog;

public class SystrayMenu2 implements ActionListener, PopupMenuListener {
	
	private static final Class<IMWComponent> MWCOMP_CLASS = IMWComponent.class;
	private static final String CRAWLER_QUERY = String.format("(%s=org.paxle.crawler)", IMWComponent.COMPONENT_ID);
	
	private static final String CRAWL_PAUSE = "Pause Crawling";
	private static final String CRAWL_RESUME = "Resume Crawling";
	
	private static enum Actions {
		SEARCH, BROWSE, CRAWL, CRAWLPR, RESTART, QUIT
	}
	
	private final Log logger = LogFactory.getLog(SystrayMenu2.class);
	private final ServiceManager manager;
	private final IDIBackend backend;
	private final ITrayIcon ti;
	
	private final IMenuItem searchItem;
	private final IMenuItem browseItem;
	private final IMenuItem crawlItem;
	private final IMenuItem crawlprItem;
	private final IMenuItem quitItem;
	private final IMenuItem restartItem;
	
	// instantiate this here to prevent delays when the popup shows up
	private final Runnable refresh = new Runnable() {
		public void run() {
			try {
				final boolean hasCrawler = manager.hasService(MWCOMP_CLASS, CRAWLER_QUERY);
				crawlItem.setEnabled(hasCrawler);
				crawlprItem.setEnabled(hasCrawler);
			} catch (InvalidSyntaxException e) { e.printStackTrace(); }
			
			final boolean hasWebui = manager.hasService(HttpService.class) && manager.hasService("org.paxle.gui.IServletManager");
			browseItem.setEnabled(canOpenBrowser && hasWebui);
			
			final boolean hasSearch = manager.hasService("org.paxle.se.search.ISearchProviderManager");
			// remove "&& hasWebUi" if we have other methods of displaying the searchresults
			searchItem.setEnabled(canOpenBrowser && hasSearch && hasWebui);
			
			crawlprItem.setText((isCrawlerCorePaused()) ? CRAWL_RESUME : CRAWL_PAUSE);
		}
	};
	
	private boolean canOpenBrowser = true;
	
	public SystrayMenu2(ServiceManager manager, IDIBackend backend, URL iconResource) {
		this.manager = manager;
		this.backend = backend;
		
		final IPopupMenu pm = backend.createPopupMenu(
				this.searchItem 	= backend.createMenuItem("Search...", 		Actions.SEARCH.name(), 	this),
				null,
				this.browseItem		= backend.createMenuItem("Webinterface", 	Actions.BROWSE.name(),	this),
				null,
				this.crawlItem 		= backend.createMenuItem("Crawl...", 		Actions.CRAWL.name(), 	this),
				this.crawlprItem 	= backend.createMenuItem("Pause Crawling", 	Actions.CRAWLPR.name(), this),
				null,
				this.restartItem 	= backend.createMenuItem("Restart", 		Actions.RESTART.name(), this),
				this.quitItem 		= backend.createMenuItem("Quit", 			Actions.QUIT.name(), 	this));
		pm.addPopupMenuListener(this);
		
		this.ti = backend.createTrayIcon(new ImageIcon(iconResource), "Paxle Tray", pm);
		backend.getSystemTray().add(this.ti);
		
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
				backend.getSystemTray().remove(ti);
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
			case BROWSE: // fall through
			case CRAWLPR: // fall through
			case RESTART: // fall through
			case QUIT:
				new Thread(dr).start();
				break;
				
			default:
				throw new RuntimeException("switch-statement does not cover action '" + action + "'");
		}
	}
	
	private ICommandProfile profile = null;
	private String profileName = "tray-crawl";
	private int crawlDepth = 3;
	
	private class TrayRunnable extends AFinally implements Runnable {
		
		private final Actions action;
		
		public TrayRunnable(final Actions action) {
			this.action = action;
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public void run() {
			switch (action) {
				case SEARCH:
					if (data != null && data.length() > 0)
						browseUrl(getPaxleURL("/search?query=", data));
					break;
					
				case CRAWL:
					if (data != null && data.length() > 0) try {
						final URI uri = new URI(data);
						
						final Object commandDB = manager.getService("org.paxle.data.db.ICommandDB");
						if (commandDB == null) throw new Exception("Command-DB not available");
						final Method enqueueCommand = commandDB.getClass().getMethod("enqueue", URI.class, int.class, int.class);
						
						final Object profileDB = manager.getService("org.paxle.core.queue.ICommandProfileManager");
						if (profileDB == null) throw new Exception("Profile-DB not available");
						final Method storeProfile = profileDB.getClass().getMethod("storeProfile", ICommandProfile.class);
						
						final Object robotsManager = manager.getService("org.paxle.filter.robots.IRobotsTxtManager");
						if (robotsManager != null) {
							final Method isDisallowed = robotsManager.getClass().getMethod("isDisallowed", URI.class);
							final Object result = isDisallowed.invoke(robotsManager, uri);
							if (((Boolean)result).booleanValue()) {
								logger.info("Domain does not allow crawling of '" + uri + "' due to robots.txt blockage");
								Utilities.showURLErrorMessage(
										"This URI is blocked by the domain's robots.txt, see",
										uri.resolve(URI.create("/robots.txt")).toString());
								break;
							}
						}
						
						logger.info("Initiated crawl of URL '" + uri + "'");
						if (profile == null) {
							// create a new profile
							profile = new CommandProfile();
							profile.setMaxDepth(crawlDepth);
							profile.setName(profileName);
							
							// store it into the profile-db
							storeProfile.invoke(profileDB, profile);
						}
						
						enqueueCommand.invoke(commandDB, uri, Integer.valueOf(profile.getOID()), Integer.valueOf(0));
					} catch (Exception ee) {
						Utilities.showURLErrorMessage("Starting crawl failed: " + ee.getMessage(), data);
						logger.error("Starting crawl of URL '" + data + "' failed: " + ee.getMessage(), ee);
					}
					break;
					
				case BROWSE:
					browseUrl(getPaxleURL("/"));
					break;
					
				case CRAWLPR:
					toggleCrawlersPR();
					break;
					
				case RESTART: try {
					manager.restartFramework();
				} catch (BundleException e) {
					Utilities.showExceptionBox("error restarting framework", e);
					logger.error("error restarting framework", e);
				} break;
				
				case QUIT: try {
					manager.shutdownFramework();
				} catch (BundleException e) {
					Utilities.showExceptionBox("error shutting down framework", e);
					logger.error("error shutting down framework", e);
				} break;
					
				default:
					throw new RuntimeException("switch-statement does not cover action '" + action + "'");
			}
		}
	}
	
	private boolean isCrawlerCorePaused() {
		try {
			final IMWComponent<?>[] crawlers = manager.getServices(MWCOMP_CLASS, CRAWLER_QUERY);
			if (crawlers == null || crawlers.length == 0)
				return false;
			return crawlers[0].isPaused();
		} catch (InvalidSyntaxException e) {
			Utilities.showExceptionBox(e);
			e.printStackTrace();
		}
		return false;
	}
	
	private void toggleCrawlersPR() {
		try {
			final IMWComponent<?>[] crawlers = manager.getServices(MWCOMP_CLASS, CRAWLER_QUERY);
			if (crawlers == null || crawlers.length == 0)
				return;
			final IMWComponent<?> crawler = crawlers[0];
			if (crawler.isPaused()) {
				crawler.resume();
			} else {
				crawler.pause();
			}
		} catch (InvalidSyntaxException e) {
			Utilities.showExceptionBox(e);
			e.printStackTrace();
		}
	}
	
	private String getPaxleURL(String... path) {
		final String port = SystrayMenu2.this.manager.getProperty("org.osgi.service.http.port");
		if (port == null)
			return null;
		final StringBuffer sb = new StringBuffer("http://localhost:").append(port);
		if (path.length == 0 || path[0].charAt(0) != '/')
			sb.append('/');
		for (final String s : path)
			sb.append(s);
		return sb.toString();
	}
	
	private boolean browseUrl(final String url) {
		if (url == null) {
			JOptionPane.showMessageDialog(null, "HTTP service not accessible", "Error", JOptionPane.ERROR_MESSAGE);
		} else try {
			if ((canOpenBrowser = backend.getDesktop().browse(url))) {
				return true;
			} else {
				Utilities.showURLErrorMessage(
						"Couldn't launch system browser due to an error in Paxle's system integration\n" +
						"bundle. Please review the log for details. The requested URL was:", url);
			}
		} catch (MalformedURLException e) {
			Utilities.showExceptionBox("Generated mal-formed URL", e);
			logger.error("Generated mal-formed URL '" + url + "': " + e.getMessage(), e);
		}
		return false;
	}
}
