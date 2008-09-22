
package org.paxle.desktop.impl.dialogues.stats;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.paxle.desktop.DIComponent;
import org.paxle.desktop.impl.DesktopServices;
import org.paxle.desktop.impl.Messages;
import org.paxle.desktop.impl.ServiceManager;

public class StatisticsPanel extends TimerTask implements DIComponent {
	
	private static final long serialVersionUID = 1L;
	
	static final String NA = Messages.getString("statisticsPanel.notAvailable"); //$NON-NLS-1$
	
	// TODO: automatically calculate these values based on the preferred size of the Stats-panels
	private static final Dimension WINDOW_SIZE = new Dimension(700, 800);
	
	// TODO: make the values below configurable via CM
	private static final long UPDATE_DELAY = 2000;	// in milliseconds
	private static final long MAX_AGE = 10 * 60;	// in seconds
	
	static abstract class Stats extends JPanel {
		
		private static final long serialVersionUID = 1L;
		
		protected StatsDataSink sds = null;
		protected final JLabel chart = new JLabel();
		private Image image = null;
		
		public abstract boolean update();
		public abstract boolean isStatsDataSupported();
		public abstract void initChart();
		protected Dimension getChartSize() {
			return new Dimension(300, 200);
		}
		
		private void setStatsDataSink(final StatsDataSink sink) {
			sds = sink;
			initChart();
		}
		
		private boolean update0() {
			final boolean ok = update();
			if (ok && isStatsDataSupported() && sds != null && image != null) {
				chart.setIcon(new ImageIcon(image));
			}
			return ok;
		}
		
		private void generateImage() {
			if (sds == null)
				return;
			final Dimension chartSize = getChartSize();
			image = sds.getChartImage(chartSize.width, chartSize.height);
		}
	}
	
	static interface StatsDataSink {
		public void addOrUpdate(Number... data);
		public void init(String title, String yDesc, String... names);
		public BufferedImage getChartImage(int width, int height);
	}
	
	private final List<? extends Stats> stats;
	private final JPanel panel = new JPanel();
	private final Timer timer = new Timer();
	
	public StatisticsPanel(final DesktopServices services) {
		stats = Arrays.asList(
				new MemoryPanel(),
				new DatabasePanel(services),
				new ActivityPanel(services));
		initChartServlet(services);
		for (final Stats s : stats)
			panel.add(s);
		timer.schedule(new TimerTask() {
			
			private long lastUpdate = 0;
			
			@Override
			public void run() {
				SwingUtilities.invokeLater(StatisticsPanel.this);
				if (System.currentTimeMillis() - lastUpdate > UPDATE_DELAY) {
					lastUpdate = System.currentTimeMillis();
					for (final Stats s : stats)
						s.generateImage();
				}
			}
		}, 0, 1000L);
	}
	
	// the update run method
	@Override
	public void run() {
		for (final Stats s : stats)
			s.update0();
	}
	
	// from org.paxle.gui.impl.Activator
	/**
	 * The external library jfreechart is optional. Therfore we need to wait until the
	 * bundle containing this library is available
	 * 
	 * @param context
	 */
	private void initChartServlet(final DesktopServices services) {
		final ServiceManager manager = services.getServiceManager();
		Bundle[] bundles = manager.getBundles();
		for (Bundle bundle : bundles) {
			// create charts now if the jfree bundle is already installed
			if (bundle.getSymbolicName().equalsIgnoreCase("com.springsource.org.jfree")) {
				addCharts(services);
				break;
			}
		}		
		
		// register a bundle-listener to detect if the jfree-bundle will be removed
		manager.addBundleListener(new BundleListener() {
			public void bundleChanged(BundleEvent event) {
				if (event.getBundle().getSymbolicName().equals("com.springsource.org.jfree")) {
					if (event.getType() == BundleEvent.RESOLVED) {
						// create the charts
						addCharts(services);
					} else if (event.getType() == BundleEvent.STOPPED || event.getType() == BundleEvent.UNINSTALLED) {
						// remove the charts
						removeCharts();
					}
				}				
			}			
		});
	}
	
	private void addCharts(final DesktopServices services) {
		final Charts charts = new Charts(services, MAX_AGE);
		for (final Stats stat : stats)
			if (stat.isStatsDataSupported())
				stat.setStatsDataSink(charts.createDataSink());
	}
	
	private void removeCharts() {
		for (final Stats stat : stats)
			if (stat.isStatsDataSupported())
				stat.setStatsDataSink(null);
	}
	
	public Dimension getWindowSize() {
		return WINDOW_SIZE;
	}
	
	public String getTitle() {
		return Messages.getString("statisticsPanel.title"); //$NON-NLS-1$
	}
	
	public Container getContainer() {
		return panel;
	}
	
	public void close() {
		timer.cancel();
	}
}
