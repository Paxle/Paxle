
package org.paxle.desktop.impl.dialogues.stats;

import java.awt.Container;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.paxle.desktop.DIComponent;
import org.paxle.desktop.impl.DesktopServices;
import org.paxle.desktop.impl.Messages;

public class StatisticsPanel extends TimerTask implements DIComponent {
	
	private static final long serialVersionUID = 1L;
	
	static final String NA = Messages.getString("statisticsPanel.notAvailable"); //$NON-NLS-1$
	
	private static final Dimension WINDOW_SIZE = new Dimension(500, 400);
	
	static abstract class Stats extends JPanel {
		private static final long serialVersionUID = 1L;
		public abstract boolean update();
	}
	
	private final List<? extends Stats> stats;
	private final JPanel panel = new JPanel();
	private final Timer timer = new Timer();
	
	public StatisticsPanel(final DesktopServices services) {
		stats = Arrays.asList(
				new MemoryPanel(),
				new DatabasePanel(services),
				new ActivityPanel(services));
		for (final Stats s : stats)
			panel.add(s);
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				SwingUtilities.invokeLater(StatisticsPanel.this);
			}
		}, 0, 1000L);
	}
	
	public Dimension getWindowSize() {
		return WINDOW_SIZE;
	}
	
	// the update run method
	@Override
	public void run() {
		for (final Stats s : stats)
			s.update();
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
