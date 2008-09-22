
package org.paxle.desktop.impl.dialogues.stats;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.io.FileSystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.time.Minute;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.osgi.framework.ServiceReference;
import org.paxle.core.IMWComponent;
import org.paxle.desktop.impl.DesktopServices;
import org.paxle.desktop.impl.dialogues.stats.StatisticsPanel.StatsDataSink;

public class Charts {
	
	private static final String TSERIES_INDEX_SIZE = "series.indexSize";
	private static final String TSERIES_MEMORY_USAGE = "series.memoryUsage";
	private static final String TSERIES_DISK_USAGE = "series.diskUsage";
	private static final String TSERIES_PPM = "series.ppm.%s";
	
	private final class ChartsDataSink implements StatsDataSink {
		
		private final TimeSeriesCollection dataset = new TimeSeriesCollection();
		private JFreeChart chart = null;
		
		public void addOrUpdate(final Number... data) {
			final Second second = new Second(new Date());
			for (int i=0; i<data.length; i++)
				dataset.getSeries(i).addOrUpdate(second, data[i]);
		}
		
		public void init(final String title, final String yDesc, final String... names) {
			if (chart != null)
				throw new IllegalStateException("already initialized");
			
			for (int i=0; i<names.length; i++) {
				final TimeSeries ts = new TimeSeries(names[i], Second.class);
				ts.setMaximumItemAge(maxAge);
				dataset.addSeries(ts);
			}
			
			/*
			 * INIT CHART
			 */        
			chart = ChartFactory.createTimeSeriesChart(
					title,
					"Time", 
					yDesc,
					dataset,
					true,
					true,
					false
			);
	        
	        // change axis data format
			((DateAxis) chart.getXYPlot().getDomainAxis()).setDateFormatOverride(new SimpleDateFormat("HH:mm"));
		}
		
		public BufferedImage getChartImage(final int width, final int height) {
			return (chart == null) ? null : chart.createBufferedImage(width, height);
		}
	}
	
	/*
	private JFreeChart createMemoryChart() {
    	// init time series
    	TimeSeries usedmemSeries = new TimeSeries("Used MEM", Minute.class);
    	usedmemSeries.setMaximumItemAge(24*60);
    	this.seriesMap.put(TSERIES_MEMORY_USAGE, usedmemSeries);
    	TimeSeries freeDiskSeries = new TimeSeries("Free Disk", Minute.class);
    	freeDiskSeries.setMaximumItemAge(24*60);
    	this.seriesMap.put(TSERIES_DISK_USAGE, freeDiskSeries);
		
		// init collections and chart
        final TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(usedmemSeries);
        
        // create subplot 1...
        long maxMemory = Runtime.getRuntime().maxMemory();
        NumberAxis memYAxis = new NumberAxis("Memory");
        if (maxMemory != Long.MAX_VALUE) {
        	memYAxis.setRange(0, maxMemory / (1024*1024));
        }
        final XYPlot subplot1 = new XYPlot(dataset, null, memYAxis, new StandardXYItemRenderer());
        subplot1.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);        
        
        // create subplot 2...
        final XYPlot subplot2 = new XYPlot(new TimeSeriesCollection(freeDiskSeries), null, new NumberAxis("Disk"), new StandardXYItemRenderer());
        subplot2.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT);

        // parent plot...
        DateAxis dateaxis = new DateAxis("Time");
        dateaxis.setDateFormatOverride(new SimpleDateFormat("HH:mm"));
        final CombinedDomainXYPlot plot = new CombinedDomainXYPlot(dateaxis);
        plot.setBackgroundPaint(Color.white);
        plot.setGap(10.0);
        
        // add the subplots...
        plot.add(subplot1, 1);
        plot.add(subplot2, 1);
        plot.setOrientation(PlotOrientation.VERTICAL);        
        
        return new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, plot, true);
	}
	*/
	
	private final DesktopServices services;
	
	/**
	 * For logging
	 */
	private final Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * A map containing all {@link TimeSeries} that are filled by the {@link DataCollector}-thread
	 */
	private HashMap<String,TimeSeries> seriesMap = new HashMap<String,TimeSeries>();
	
	private final long maxAge;
	
	public Charts(final DesktopServices services, final long maxAge) {
		this.services = services;
		this.maxAge = maxAge;
	}
	
	public StatsDataSink createDataSink() {
		return new ChartsDataSink();
	}
	
	private void updateMemoryChart() {
		Runtime rt = Runtime.getRuntime();
		long usedMem = (rt.totalMemory() - rt.freeMemory()) / ( 1024 * 1024);
		long freeDisk = 0;
		try {
			freeDisk = FileSystemUtils.freeSpaceKb(new File("/").getCanonicalPath().toString());
			freeDisk = freeDisk / 1024;
		} catch (IOException e) {
			this.logger.error(e);
		}
		
		Minute minute = new Minute(new Date());
		
		TimeSeries usedmemSeries = this.seriesMap.get(TSERIES_MEMORY_USAGE);
		if (usedmemSeries != null) {
			usedmemSeries.addOrUpdate(minute,Long.valueOf(usedMem));
		}
		
		TimeSeries freeDiskSeries = this.seriesMap.get(TSERIES_DISK_USAGE);
		if (freeDiskSeries != null) {
			freeDiskSeries.addOrUpdate(minute, Long.valueOf(freeDisk));
		}
	}
	
	private void updateIndexChart() {
		try {
			Object service = this.services.getServiceManager().getService("org.paxle.se.index.IIndexSearcher");
			Integer docCount = (Integer) service.getClass().getMethod("getDocCount", (Class[])null).invoke(service, (Object[])null);
			
			TimeSeries indexSizeSeries = this.seriesMap.get(TSERIES_INDEX_SIZE); {
				if (indexSizeSeries != null) indexSizeSeries.add(new Minute(new Date()), docCount);	
			}
		} catch (Throwable e) {
			this.logger.error(e);
		}
	}
	
	private void updatePPMChart() {	
		try {
			Minute minute = new Minute(new Date());
			
			ServiceReference[] refs = this.services.getServiceManager().getServiceReferences("org.paxle.core.IMWComponent", "(component.ID=*)");
			if (refs != null) {
				for (ServiceReference ref : refs) {
					String componentID = (String) ref.getProperty("component.ID");
					IMWComponent<?> mw = this.services.getServiceManager().getService(ref, IMWComponent.class);
					if (mw != null) {
						TimeSeries series = this.seriesMap.get(String.format(TSERIES_PPM,componentID)); 
						if (series != null) {
							series.add(minute, mw.getPPM());
						}
					}
				}
			}	
		} catch (Throwable e) {
			this.logger.error(e);
		}	
	}
	
	class DataCollector extends Thread {
		@Override
		public void run() {
			this.setName(this.getClass().getSimpleName());
			
			while(true) {
				try {
					// update charts
					updateMemoryChart();
					updatePPMChart();
					updateIndexChart();
					
					// sleep for a while
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					logger.info(String.format("Interruption detected. Shutdown of %s finished.",this.getClass().getSimpleName()));
					return;
				}
			}
		}
	}

}
