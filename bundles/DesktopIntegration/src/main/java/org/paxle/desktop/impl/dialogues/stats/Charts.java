/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import org.paxle.desktop.impl.Messages;
import org.paxle.desktop.impl.dialogues.stats.StatisticsPanel.StatsDataSink;

public class Charts {
	
	private final class ChartsDataSink implements StatsDataSink {
		
		private final List<TimeSeriesCollection> datasets = new ArrayList<TimeSeriesCollection>();
		private JFreeChart chart = null;
		private ArrayList<XYPlot> plots = new ArrayList<XYPlot>();
		
		public void addOrUpdate(final int idx, final Number... data) {
			final Second second = new Second(new Date());
			final TimeSeriesCollection dataset = datasets.get(idx);
			for (int i=0; i<data.length; i++)
				dataset.getSeries(i).addOrUpdate(second, data[i]);
		}
		
		public void addOrUpdate(final Number... data) {
			addOrUpdate(0, data);
		}
		
		public void init(final String title, final String yDesc, final String... names) {
			addPlot(yDesc, Long.MIN_VALUE, Long.MAX_VALUE, null, names);
			finish(title, null);
		}
		
		public int numPlots() {
			return datasets.size();
		}
		
		public void addPlot(
				final String yDesc,
				final long min, final long max,
				final Locations loc, final String... names) {
			if (chart != null)
				throw new IllegalStateException("already initialized");
			
			final TimeSeriesCollection dataset = new TimeSeriesCollection();
			datasets.add(dataset);
			
			for (int i=0; i<names.length; i++) {
				final TimeSeries ts = new TimeSeries(names[i], Second.class);
				ts.setMaximumItemAge(maxAge);
				dataset.addSeries(ts);
			}
			
			final NumberAxis memYAxis = new NumberAxis(yDesc);
			if (min != Long.MIN_VALUE && max != Long.MAX_VALUE) {
				memYAxis.setRange(min, max);
			} else {
				memYAxis.setAutoRange(true);
				memYAxis.setAutoRangeIncludesZero(false);
			}
			
			final XYPlot subplot = new XYPlot(dataset, null, memYAxis, new StandardXYItemRenderer());
			if (loc != null) {
				final AxisLocation al;
				switch (loc) {
					case BOTTOM_OR_LEFT: al = AxisLocation.BOTTOM_OR_LEFT; break;
					case TOP_OR_LEFT: al = AxisLocation.TOP_OR_LEFT; break;
					case BOTTOM_OR_RIGHT: al = AxisLocation.BOTTOM_OR_RIGHT; break;
					case TOP_OR_RIGHT: al = AxisLocation.TOP_OR_RIGHT; break;
					default:
						throw new RuntimeException("unhandled case of location: " + loc);
				}
				subplot.setRangeAxisLocation(al);
			}
			plots.add(subplot);
		}
		
		public void finish(final String title, final Orientation orientation) {
			if (plots.size() == 0) {
				return;
			} else if (plots.size() == 1) {
				chart = ChartFactory.createTimeSeriesChart(
						title,
						Messages.getString("statisticsPanel.chart.time"), 
						plots.get(0).getRangeAxis().getLabel(),
						plots.get(0).getDataset(),
						true,
						true,
						false
				);
		        
				// change axis data format
				((DateAxis) chart.getXYPlot().getDomainAxis()).setDateFormatOverride(new SimpleDateFormat("HH:mm"));
			} else {
		        // parent plot...
				final DateAxis dateAxis = new DateAxis(Messages.getString("statisticsPanel.chart.time"));
				dateAxis.setDateFormatOverride(new SimpleDateFormat("HH:mm"));
		        dateAxis.setLowerMargin(0.02);  // reduce the default margins 
		        dateAxis.setUpperMargin(0.02);
				final CombinedDomainXYPlot plot = new CombinedDomainXYPlot(dateAxis);
		        plot.setBackgroundPaint(Color.white);
		        plot.setGap(10.0);
		        
		        // add the subplots...
		        for (final XYPlot subplot : plots)
		        	plot.add(subplot);
				
		        if (orientation != null)
		        	switch (orientation) {
		        		case HORIZONTAL: plot.setOrientation(PlotOrientation.HORIZONTAL); break;
		        		case VERTICAL: plot.setOrientation(PlotOrientation.VERTICAL); break;
						default:
							throw new RuntimeException("unhandled case of orientation: " + orientation);
		        	}
		        
				chart = new JFreeChart(title, null, plot, true);
			}
			plots = null;
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
	
	private final long maxAge;
	
	public Charts(final long maxAge) {
		this.maxAge = maxAge;
	}
	
	public StatsDataSink createDataSink() {
		return new ChartsDataSink();
	}
}
