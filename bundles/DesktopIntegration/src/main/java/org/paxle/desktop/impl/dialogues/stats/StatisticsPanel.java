/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2010 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.desktop.impl.dialogues.stats;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
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
import org.paxle.desktop.impl.Messages;
import org.paxle.desktop.impl.ServiceManager;
import org.paxle.desktop.impl.Utilities;
import org.paxle.desktop.impl.dialogues.DIServicePanel;

public class StatisticsPanel extends DIServicePanel implements DIComponent, Runnable {
	
	private static final long serialVersionUID = 1L;
	
	static final String NA = Messages.getString("statisticsPanel.notAvailable"); //$NON-NLS-1$
	
	private static final Dimension WINDOW_SIZE = new Dimension(700, 800);
	
	// TODO: make the values below configurable via CM
	private static final long UPDATE_DELAY = 2000;	// in milliseconds
	private static final long MAX_AGE = 5 * 60;	// in seconds
	
	static abstract class Stats extends JPanel {
		
		private static final long serialVersionUID = 1L;
		
		protected StatsDataSink sds = null;
		protected final JLabel chart = new JLabel();
		private Image image = null;
		
		public abstract boolean update();
		public abstract boolean isStatsDataSupported();
		public abstract void initChart();
		protected Dimension getChartSize() {
			return (sds == null) ? null : new Dimension(300, 70 + 130 * sds.numPlots());
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
		public static enum Locations {
			TOP_OR_LEFT, BOTTOM_OR_LEFT, TOP_OR_RIGHT, BOTTOM_OR_RIGHT
		}
		public static enum Orientation {
			HORIZONTAL, VERTICAL;
		}
		public void addOrUpdate(int idx, Number... data);
		public void addOrUpdate(Number... data);
		public void init(String title, String yDesc, String... names);
		public Image getChartImage(int width, int height);
		public void addPlot(final String yDesc, final long min, final long max, final Locations loc, final String... names);
		public void finish(final String title, final Orientation orientation);
		public int numPlots();
	}
	
	private final List<? extends Stats> stats;
	private final Timer timer = new Timer("Desktop_StatsUpdater", true);
	
	public StatisticsPanel(final ServiceManager services) {
		super(services, WINDOW_SIZE);
		stats = Arrays.asList(
				new MemoryPanel(),
				new DatabasePanel(services),
				new ActivityPanel(services));
		initChartServlet(services);
		((FlowLayout)super.getLayout()).setHgap(0);
		((FlowLayout)super.getLayout()).setVgap(0);
		for (final Stats s : stats)
			super.add(s);
		timer.schedule(new TimerTask() {
			
			private long lastUpdate = 0;
			
			@Override
			public void run() {
				try {
					SwingUtilities.invokeAndWait(StatisticsPanel.this);
					if (System.currentTimeMillis() - lastUpdate > UPDATE_DELAY) {
						lastUpdate = System.currentTimeMillis();
						for (final Stats s : stats)
							s.generateImage();
					}
				} catch (Throwable e) { Utilities.instance.showExceptionBox(frame, "Error during run of Statistics-update-timer", e); }
			}
		}, 0, 1000L);
	}
	
	// the update run method
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
	private void initChartServlet(final ServiceManager services) {
		Bundle[] bundles = services.getBundles();
		for (Bundle bundle : bundles) {
			// create charts now if the jfree bundle is already installed
			if (bundle.getSymbolicName().equalsIgnoreCase("com.springsource.org.jfree")) {
				addCharts();
				break;
			}
		}		
		
		// register a bundle-listener to detect if the jfree-bundle will be removed
		services.addBundleListener(new BundleListener() {
			public void bundleChanged(BundleEvent event) {
				if (event.getBundle().getSymbolicName().equals("com.springsource.org.jfree")) {
					if (event.getType() == BundleEvent.RESOLVED) {
						// create the charts
						addCharts();
					} else if (event.getType() == BundleEvent.STOPPED || event.getType() == BundleEvent.UNINSTALLED) {
						// remove the charts
						removeCharts();
					}
				}				
			}			
		});
	}
	
	private void addCharts() {
		final Charts charts = new Charts(MAX_AGE);
		for (final Stats stat : stats)
			if (stat.isStatsDataSupported())
				stat.setStatsDataSink(charts.createDataSink());
	}
	
	private void removeCharts() {
		for (final Stats stat : stats)
			if (stat.isStatsDataSupported())
				stat.setStatsDataSink(null);
	}
	
	@Override
	public String getTitle() {
		return Messages.getString("statisticsPanel.title"); //$NON-NLS-1$
	}
	
	@Override
	public void close() {
		timer.cancel();
		super.close();
	}
}
