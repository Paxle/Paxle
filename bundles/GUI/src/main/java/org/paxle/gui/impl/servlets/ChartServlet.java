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

	private final BundleContext context;
	private final Log logger = LogFactory.getLog(this.getClass());
	
	private final TimeSeries indexSizeSeries = new TimeSeries("Index Size", Minute.class);
	private final TimeSeries usedmemSeries = new TimeSeries("Used MEM", Minute.class);
	private final TimeSeries freeDiskSeries = new TimeSeries("Free Disk", Minute.class);
	
	private final HashMap<String,TimeSeries> ppmSeries = new HashMap<String,TimeSeries>();
	{
		ppmSeries.put("org.paxle.crawler",new TimeSeries("Crawler PPM", Minute.class));
		ppmSeries.put("org.paxle.parser",new TimeSeries("Parser PPM", Minute.class));
		ppmSeries.put("org.paxle.indexer",new TimeSeries("Indexer PPM", Minute.class));
	}
	
	private final JFreeChart memChart;
	private final JFreeChart ppmChart;
	private final JFreeChart indexChart;
	
	
	public ChartServlet(BundleContext context) {
		this.context = context;
		
		// create charts
		this.memChart = this.createMemoryChart();
		this.ppmChart = this.createPPMChart();
		this.indexChart = this.createIndexChart();
        
        // start thread
        new DataCollector().start();
	}
	
	private JFreeChart createPPMChart() {
        final TimeSeriesCollection dataset = new TimeSeriesCollection();
        for (TimeSeries series : this.ppmSeries.values()) {
        	dataset.addSeries(series);
        }
        
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                null,
                "Time", 
                "PPM",
                dataset,
                true,
                true,
                false
            );
        
		((DateAxis) chart.getXYPlot().getDomainAxis()).setDateFormatOverride(new SimpleDateFormat("HH:mm"));
		return chart;        
	}
	
	private JFreeChart createIndexChart() {
		JFreeChart chart = ChartFactory.createTimeSeriesChart(
                null,
                "Time", 
                "#Docs",
                new TimeSeriesCollection(indexSizeSeries),
                true,
                true,
                false
            );
		((DateAxis) chart.getXYPlot().getDomainAxis()).setDateFormatOverride(new SimpleDateFormat("HH:mm"));
		return chart;
	}

	private JFreeChart createMemoryChart() {
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
			e.printStackTrace();
		}
		
		Minute minute = new Minute(new Date());
		this.usedmemSeries.addOrUpdate(minute,Long.valueOf(usedMem));	
		this.freeDiskSeries.addOrUpdate(minute, Long.valueOf(freeDisk));		
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
						TimeSeries series = this.ppmSeries.get(componentID);
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
				this.indexSizeSeries.add(new Minute(new Date()), docCount);
			}
		} catch (Throwable e) {
			this.logger.error(e);
		}
	}
	
	class DataCollector extends Thread {
		@Override
		public void run() {
			while(true) {
				try {
					// update charts
					updateMemoryChart();
					updatePPMChart();
					updateIndexChart();
					
					// sleep for a while
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
