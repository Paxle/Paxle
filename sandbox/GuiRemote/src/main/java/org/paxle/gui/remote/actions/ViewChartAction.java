package org.paxle.gui.remote.actions;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.paxle.gui.remote.Peer;
import org.paxle.gui.remote.PeerService;
import org.paxle.gui.remote.PeerStatus;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionSupport;

public class ViewChartAction extends ActionSupport {
	private static final long serialVersionUID = 1L;
	
	private PeerService service;
	private JFreeChart chart;
	
	private String varName;
	
	public ViewChartAction(PeerService service) {
		this.service = service;
	}

	public String execute() throws Exception {	
		final TimeSeriesCollection dataset = new TimeSeriesCollection();
		
		List<Peer> peers = this.service.getPeers();
		for (Peer peer : peers) {
			TimeSeries ppm = new TimeSeries(peer.getName(), Second.class);
			dataset.addSeries(ppm);
			
			List<PeerStatus> statusValues = peer.getPeerStatus(this.varName);
			if (statusValues != null) {
				for (PeerStatus statusValue : statusValues) {					
					ppm.addOrUpdate(new Second(new Date(statusValue.getTimeStamp())), statusValue.getValue());
				}
			}
		}
		
        chart = ChartFactory.createTimeSeriesChart(
                null,
                "Time", 
                "Value",
                dataset,
                true,
                false,
                false
            );
        
        // change axis data format
		((DateAxis) chart.getXYPlot().getDomainAxis()).setDateFormatOverride(new SimpleDateFormat("HH:mm"));
		chart.setBackgroundPaint(Color.WHITE);
		
		return Action.SUCCESS;
	}

	public JFreeChart getChart() {
		return chart;
	}

	public String getVarName() {
		return varName;
	}

	public void setVarName(String varName) {
		this.varName = varName;
	}
}
