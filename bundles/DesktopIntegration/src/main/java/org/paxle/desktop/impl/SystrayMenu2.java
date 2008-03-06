
package org.paxle.desktop.impl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;

import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;

import org.paxle.core.data.IDataSink;
import org.paxle.core.queue.Command;
import org.paxle.core.queue.ICommand;
import org.paxle.desktop.backend.IDIBackend;
import org.paxle.desktop.backend.tray.IMenuItem;
import org.paxle.desktop.backend.tray.IPopupMenu;
import org.paxle.desktop.backend.tray.ITrayIcon;
import org.paxle.desktop.impl.dialogues.AFinally;
import org.paxle.desktop.impl.dialogues.SmallDialog;
import org.paxle.se.search.ISearchProviderManager;

public class SystrayMenu2 implements ActionListener {
	
	private static final Class<IDataSink> CRAWLER_SINK_CLASS = IDataSink.class;
	private static final String CRAWLER_SINK_QUERY = "(org.paxle.core.data.IDataSink.id=org.paxle.crawler.sink)";
	private static final Class<ISearchProviderManager> SEARCHER_CLASS = ISearchProviderManager.class;
	
	private static final String CRAWL = "crawl";
	private static final String CRAWLPR = "crawlpr";
	private static final String SEARCH = "search";
	private static final String RESTART = "restart";
	private static final String QUIT = "quit";
	
	private final ServiceManager manager;
	private final IDIBackend backend;
	private final ITrayIcon ti;
	
	private final IMenuItem searchItem;
	private final IMenuItem crawlItem;
	private final IMenuItem crawlprItem;
	private final IMenuItem quitItem;
	private final IMenuItem restartItem;
	
	public SystrayMenu2(ServiceManager manager, IDIBackend backend, URL iconResource) {
		this.manager = manager;
		this.backend = backend;
		
		final IPopupMenu pm = backend.createPopupMenu(
				this.searchItem 	= backend.createMenuItem("Search...", 		SEARCH, 	this),
				null,
				this.crawlItem 		= backend.createMenuItem("Crawl...", 		CRAWL, 		this),
				this.crawlprItem 	= backend.createMenuItem("Pause Crawling", 	CRAWLPR, 	this),
				null,
				this.restartItem 	= backend.createMenuItem("Restart", 		RESTART, 	this),
				this.quitItem 		= backend.createMenuItem("Quit", 			QUIT, 		this));
		
		this.ti = backend.createTrayIcon(new ImageIcon(iconResource), "Paxle Tray", pm);
		backend.getSystemTray().add(this.ti);
	}
	
	public void shutdown() {
		this.backend.getSystemTray().remove(this.ti);
	}
	
	protected void refresh() {
		try {
			final boolean hasCrawler = this.manager.hasService(CRAWLER_SINK_CLASS, CRAWLER_SINK_QUERY);
			this.crawlItem.setEnabled(hasCrawler);
			this.crawlprItem.setEnabled(hasCrawler);
		} catch (InvalidSyntaxException e) { e.printStackTrace(); }
		this.searchItem.setEnabled(this.manager.hasService(SEARCHER_CLASS));
	}
	
	public void actionPerformed(ActionEvent e) {
		final String cmd = e.getActionCommand();
		
		if (cmd == CRAWL) {
			new SmallDialog(new CrawlFinally(), "Enter URL:", "Crawl").setVisible(true);
		} else if (cmd == CRAWLPR) {
			// TODO: pause/resume crawler input queue
		} else if (cmd == SEARCH) {
			new SmallDialog(new SearchFinally(), "Enter query:", "Search").setVisible(true);
		} else try {
			if (cmd == RESTART) {
				this.manager.restartFramework();
			} else if (cmd == QUIT) {
				this.manager.shutdownFramework();
			}
		} catch (BundleException ee) {
			ee.printStackTrace();
		}
	}
	
	private class SearchFinally extends AFinally {
		@Override
		public void run() {
			final String port = SystrayMenu2.this.manager.getProperty("org.osgi.service.http.port");
			if (port != null) try {
				if (!SystrayMenu2.this.backend.getDesktop().browse(String.format("http://localhost:%s/search?query=%s", port, super.data))) {
					// TODO: log error message: couldn't start browser
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private class CrawlFinally extends AFinally {
		@Override
		@SuppressWarnings("unchecked")
		public void run() {
			try {
				final IDataSink<ICommand>[] sink = SystrayMenu2.this.manager.getServices(CRAWLER_SINK_CLASS, CRAWLER_SINK_QUERY);
				if (sink != null)
					sink[0].putData(Command.createCommand(super.data));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
