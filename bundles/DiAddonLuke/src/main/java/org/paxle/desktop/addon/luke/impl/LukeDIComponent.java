
package org.paxle.desktop.addon.luke.impl;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;

import org.getopt.luke.Luke;
import org.paxle.desktop.DIComponent;

public class LukeDIComponent implements DIComponent {
	
	private static final Dimension DEFAULT_DIM = new Dimension(700, 500);
	private static final String TITLE = "Luke - Lucene Index Toolbox, v 0.8.1 (2008-02-13)";
	
	private final Luke luke = new Luke();
	
	public void close() {
		luke.actionClose();
	}
	
	public Container getContainer() {
		return luke;
	}
	
	public String getTitle() {
		return "Luke Toolbox";
	}
	
	public Dimension getWindowSize() {
		return DEFAULT_DIM;
	}
	
	public void setFrame(Frame f) {
		f.setTitle(TITLE);
		f.setPreferredSize(DEFAULT_DIM);
	    f.setIconImage(Toolkit.getDefaultToolkit().createImage(Luke.class.getResource("/img/luke.gif")));
	}
}
