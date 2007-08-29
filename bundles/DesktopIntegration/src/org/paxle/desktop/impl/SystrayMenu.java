
package org.paxle.desktop.impl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.jdesktop.jdic.desktop.Desktop;
import org.jdesktop.jdic.desktop.DesktopException;

import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;

import org.paxle.core.data.IDataSink;
import org.paxle.core.queue.Command;
import org.paxle.core.queue.ICommand;
import org.paxle.desktop.impl.dialogues.AFinally;
import org.paxle.desktop.impl.dialogues.SmallDialog;
import org.paxle.se.search.ISearchProviderManager;

public class SystrayMenu extends JPopupMenu implements ActionListener {
	
	private static final long serialVersionUID = 1L;
	
	private static final Class<IDataSink> CRAWLER_SINK_CLASS = IDataSink.class;
	private static final String CRAWLER_SINK_QUERY = "(org.paxle.core.data.IDataSink.id=org.paxle.crawler.sink)";
	private static final Class<ISearchProviderManager> SEARCHER_CLASS = ISearchProviderManager.class;
	
	private static JMenuItem createMenuItem(String text, ImageIcon icon, String actionCommand, ActionListener listener) {
		final JMenuItem item = new JMenuItem();
		if (text != null) item.setText(text);
		if (icon != null) item.setIcon(icon);
		if (listener != null) item.addActionListener(listener);
		if (actionCommand != null) item.setActionCommand(actionCommand);
		return item;
	}
	
	private static boolean openBrowser(String url) throws MalformedURLException {
		try {
			Desktop.browse(new URL(url));
		} catch (DesktopException e1) {
			final String browserPath = System.getenv("BROWSER");
			if (browserPath == null)
				return false;
			try {
				Runtime.getRuntime().exec(browserPath + " " + url);
			} catch (IOException e) {
				return false;
			}
		}
		return true;
	}
	
	private void add(JMenuItem... items) {
		for (final JMenuItem item : items) {
			if (item == null) {
				super.addSeparator();
			} else {
				super.add(item);
			}
		}
	}
	
	private class SearchFinally extends AFinally {
		@Override
		public void run() {
			final String port = SystrayMenu.this.manager.getProperty("org.osgi.service.http.port");
			if (port != null) try {
				if (!SystrayMenu.openBrowser(String.format("http://localhost:%s/search?query=%s", port, super.data))) {
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
				final IDataSink<ICommand>[] sink = SystrayMenu.this.manager.getServices(CRAWLER_SINK_CLASS, CRAWLER_SINK_QUERY);
				if (sink != null)
					sink[0].putData(Command.createCommand(super.data));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private static final String CRAWL = "crawl";
	private static final String CRAWLPR = "crawlpr";
	private static final String SEARCH = "search";
	private static final String RESTART = "restart";
	private static final String QUIT = "quit";
	
	private final ServiceManager manager;
	private final JMenuItem searchItem 	= createMenuItem("Search...", 		null, SEARCH, 	this);
	private final JMenuItem crawlItem 	= createMenuItem("Crawl...", 		null, CRAWL, 	this);
	private final JMenuItem crawlprItem = createMenuItem("Pause Crawling", 	null, CRAWLPR, 	this);
	private final JMenuItem quitItem 	= createMenuItem("Quit", 			null, QUIT, 	this);
	private final JMenuItem restartItem = createMenuItem("Restart", 		null, RESTART, 	this);
	
	public SystrayMenu(ServiceManager manager) {
		this.manager = manager;
		this.add(this.searchItem,
				null,
				this.crawlItem,
				this.crawlprItem,
				null,
				this.restartItem,
				this.quitItem);
		refresh();
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
	
	protected void refresh() {
		try {
			final boolean hasCrawler = this.manager.hasService(CRAWLER_SINK_CLASS, CRAWLER_SINK_QUERY);
			this.crawlItem.setEnabled(hasCrawler);
			this.crawlprItem.setEnabled(hasCrawler);
		} catch (InvalidSyntaxException e) { e.printStackTrace(); }
		this.searchItem.setEnabled(this.manager.hasService(SEARCHER_CLASS));
	}
	
	@Override
	public void setVisible(boolean yes) {
		if (yes) refresh();
		super.setVisible(yes);
	}
}
