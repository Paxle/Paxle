package org.paxle.gui.impl.servlets;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileSystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class ChartServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final String TSERIES_INDEX_SIZE = "series.indexSize";
	private static final String TSERIES_MEMORY_USAGE = "series.memoryUsage";
	private static final String TSERIES_DISK_USAGE = "series.diskUsage";
	private static final String TSERIES_PPM = "series.ppm.%s"; 
	

	/**
	 * An OSGi bundle-context used to access other services registered to the system
	 */
	private final BundleContext context;
	
	/**
	 * For logging
	 */
	private final Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * A {@link Thread} to collect data for the various charts
	 */
	private DataCollector collectorThread;
	
	/**
	 * A map containing all {@link TimeSeries} that are filled by the {@link DataCollector}-thread
	 */
	private HashMap<String,TimeSeries> seriesMap = new HashMap<String,TimeSeries>();

	private JFreeChart memChart;
	private JFreeChart ppmChart;
	private JFreeChart indexChart;	
	
	public ChartServlet(BundleContext context) {
		this.context = context;
	}
	
	@Override
	public void init() throws ServletException {
		super.init();
				
		// create charts
		this.memChart = this.createMemoryChart();
		this.ppmChart = this.createPPMChart();
		this.indexChart = this.createIndexChart();
        
        // start thread
		this.collectorThread = new DataCollector();
		this.collectorThread.start();		
	}
	
	@Override
	public void destroy() {
		// shutdown the collector thread
		if (this.collectorThread != null) {
			// interrupt the thread
			collectorThread.interrupt();
			
			// wait for the thread to shutdown
			try {
				this.collectorThread.join(1000);
			} catch (InterruptedException e) {/* ignore this */}
		}
	
		// clear series map
		this.seriesMap.clear();
		
		super.destroy();
	}
	
	private JFreeChart createPPMChart() {
		/*
		 * INIT TIME-SERIES 
		 */
        final TimeSeriesCollection dataset = new TimeSeriesCollection();
        
        TimeSeries crawlerPPM = new TimeSeries("Crawler PPM", Minute.class);
        dataset.addSeries(crawlerPPM);
        this.seriesMap.put(String.format(TSERIES_PPM,"org.paxle.crawler"), crawlerPPM);

        TimeSeries parserPPM = new TimeSeries("Parser PPM", Minute.class);
        dataset.addSeries(parserPPM);
        this.seriesMap.put(String.format(TSERIES_PPM,"org.paxle.parser"), parserPPM);
        
        TimeSeries indexerPPM = new TimeSeries("Indexer PPM", Minute.class);
        dataset.addSeries(indexerPPM);
        this.seriesMap.put(String.format(TSERIES_PPM,"org.paxle.indexer"), indexerPPM);
        
		/*
		 * INIT CHART
		 */        
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                null,
                "Time", 
                "PPM",
                dataset,
                true,
                true,
                false
            );
        
        // change axis data format
		((DateAxis) chart.getXYPlot().getDomainAxis()).setDateFormatOverride(new SimpleDateFormat("HH:mm"));
		return chart;        
	}
	
	private JFreeChart createIndexChart() {
		// init Time-Series
		TimeSeries indexSizeSeries = new TimeSeries("Index Size", Minute.class);
		this.seriesMap.put(TSERIES_INDEX_SIZE, indexSizeSeries);
		
		// init chart
		JFreeChart chart = ChartFactory.createTimeSeriesChart(
                null,
                "Time", 
                "#Docs",
                new TimeSeriesCollection(indexSizeSeries),
                true,
                true,
                false
            );
		
		// change axis date format
		((DateAxis) chart.getXYPlot().getDomainAxis()).setDateFormatOverride(new SimpleDateFormat("HH:mm"));
		return chart;
	}

	private JFreeChart createMemoryChart() {
    	// init time series
    	TimeSeries usedmemSeries = new TimeSeries("Used MEM", Minute.class);
    	this.seriesMap.put(TSERIES_MEMORY_USAGE, usedmemSeries);
    	TimeSeries freeDiskSeries = new TimeSeries("Free Disk", Minute.class);
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
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String wStrg = request.getParameter("w");
		int width = Integer.valueOf(wStrg == null ? "385": wStrg);
		
		String hStrg = request.getParameter("h");
		int height = Integer.valueOf(hStrg == null ? "200": hStrg);
		
		// set response type
		response.setContentType("image/png");
		
		// render image
		String type = request.getParameter("t");
		if (type.equals("mem")) {			
			ChartUtilities.writeChartAsPNG(response.getOutputStream(), this.memChart, width, height);
		} else if (type.equals("ppm")) {
			ChartUtilities.writeChartAsPNG(response.getOutputStream(), this.ppmChart, width, height);
		} else if (type.equals("index")) {
			ChartUtilities.writeChartAsPNG(response.getOutputStream(), this.indexChart, width, height);
		}
		
		return;
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
	
	private void updatePPMChart() {	
		try {
			Minute minute = new Minute(new Date());
			
			ServiceReference[] refs = this.context.getServiceReferences("org.paxle.core.IMWComponent", "(component.ID=*)");
			if (refs != null) {
				for (ServiceReference ref : refs) {
					String componentID = (String) ref.getProperty("component.ID");
					Object mw = this.context.getService(ref);
					if (mw != null) {
						TimeSeries series = this.seriesMap.get(String.format(TSERIES_PPM,componentID)); 
						if (series != null) {
							Integer ppm = (Integer) mw.getClass().getMethod("getPPM", (Class[])null).invoke(mw, (Object[])null);
							series.add(minute, ppm);
						}
					}
				}
			}	
		} catch (Throwable e) {
			this.logger.error(e);
		}	
	}
	
	private void updateIndexChart() {
		try {
			ServiceReference ref = this.context.getServiceReference("org.paxle.se.index.IIndexSearcher");
			if (ref != null) {
				Object service = this.context.getService(ref);
				Integer docCount = (Integer) service.getClass().getMethod("getDocCount", (Class[])null).invoke(service, (Object[])null);
				
				TimeSeries indexSizeSeries = this.seriesMap.get(TSERIES_INDEX_SIZE); {
					if (indexSizeSeries != null) indexSizeSeries.add(new Minute(new Date()), docCount);	
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
