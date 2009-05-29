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
package org.paxle.tools.logging.impl.desktop;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LF5Appender;
import org.apache.log4j.lf5.LogLevel;
import org.apache.log4j.lf5.viewer.LogBrokerMonitor;
import org.apache.log4j.lf5.viewer.LogTable;
import org.apache.log4j.lf5.viewer.configure.ConfigurationManager;
import org.paxle.desktop.DIComponent;

/**
 * @scr.component
 * @scr.service interface="org.paxle.desktop.DIComponent"
 */
public class LogFactor5DIComponent implements DIComponent {

	private static final Dimension DEFAULT_DIM = new Dimension(700, 500);
	private static final String TITLE = "LogFactor5";	
	
	private Frame f;
	private LogMonitor monitor;
	private LF5Appender appender;
	
	public void close() {	
		Logger rootLogger = Logger.getRootLogger();
		rootLogger.removeAppender(this.appender);
		this.appender = null;
	}

	public Container getContainer() {
		try {
//			log4j.appender.lf5=org.apache.log4j.lf5.LF5Appender
//			log4j.appender.lf5.MaxNumberOfRecords=1000
			
		    this.monitor = new LogMonitor(LogLevel.getLog4JLevels());
	        this.monitor.setFontSize(10);
	        this.monitor.setMaxNumberOfLogRecords(500);
	        
			// getting the Log4j root logger
			Logger rootLogger = Logger.getRootLogger();
			this.appender = new LF5Appender(monitor);
			rootLogger.addAppender(this.appender);
	        
	        return monitor.getBaseFrame().getContentPane();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public String getTitle() {
		return TITLE;
	}

	public Dimension getWindowSize() {
		return DEFAULT_DIM;
	}

	public void setFrame(Frame frame) {
		this.f = frame;
		f.setTitle(TITLE);
		f.setPreferredSize(DEFAULT_DIM);
	    f.setIconImage(Toolkit.getDefaultToolkit().createImage(LogBrokerMonitor.class.getResource("/org/apache/log4j/lf5/viewer/images/lf5_small_icon.gif")));
	    
	    if (frame instanceof JFrame) {
	    	// getting the menu-bar
	    	JMenuBar menu = this.monitor.getBaseFrame().getRootPane().getJMenuBar();
	    	
	    	// manipulate it (removing system-exit / close)
	    	JMenu fileMenu = menu.getMenu(0);
	    	fileMenu.remove(5);
	    	fileMenu.remove(4);
	    	
	    	// adding it to the DIComponent frame
	    	((JFrame)frame).getRootPane().setJMenuBar(menu);
	    }
	}
	
	private static class LogMonitor extends LogBrokerMonitor {
		@SuppressWarnings("unchecked")
		public LogMonitor(List logLevels) {
			super(logLevels);
			this._configurationManager = new MyConfigManager(this,this._table);
		}
	}
	
	private static class MyConfigManager extends ConfigurationManager {
		public MyConfigManager(LogBrokerMonitor monitor, LogTable table) {
			super(monitor, table);
		}
		
		@Override
		protected String getFilename() {
		    String home = System.getProperty("paxle.data");
		    String sep = System.getProperty("file.separator");
		    return home + sep + "lf5" + sep + "lf5_configuration.xml";
		}
	}
}
