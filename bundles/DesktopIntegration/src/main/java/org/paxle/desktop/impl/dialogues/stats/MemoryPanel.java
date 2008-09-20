/**
 * 
 */
package org.paxle.desktop.impl.dialogues.stats;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;

import org.paxle.desktop.Utilities;
import org.paxle.desktop.impl.Messages;
import org.paxle.desktop.impl.dialogues.stats.StatisticsPanel.Stats;

class MemoryPanel extends Stats implements ActionListener {
	
	private static final long serialVersionUID = 1L;
	
	private static final String AC_GC = new String();
	
	private final JLabel lblMemUsed = new JLabel();
	private final JLabel lblMemFree = new JLabel();
	private final JLabel lblMemTotal = new JLabel();
	private final JLabel lblDiskFree = new JLabel();
	
	public MemoryPanel() {
		init();
	}
	
	private void init() {
		super.setLayout(new GridBagLayout());
		super.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Messages.getString("statisticsPanel.memory"))); //$NON-NLS-1$
		
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = gbc.gridy = 0;
		gbc.insets = Utilities.INSETS_DEFAULT;
		gbc.anchor = GridBagConstraints.EAST;
		
		super.add(new JLabel(Messages.getString("statisticsPanel.memory.used")), gbc); gbc.gridy++; //$NON-NLS-1$
		super.add(new JLabel(Messages.getString("statisticsPanel.memory.free")), gbc); gbc.gridy++; //$NON-NLS-1$
		super.add(new JLabel(Messages.getString("statisticsPanel.memory.total")), gbc); gbc.gridy++; //$NON-NLS-1$
		super.add(new JLabel(Messages.getString("statisticsPanel.memory.diskspace.free")), gbc); gbc.gridy++; //$NON-NLS-1$
		
		gbc.gridx = 1; gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		
		super.add(lblMemUsed, gbc); gbc.gridy++;
		super.add(lblMemFree, gbc); gbc.gridy++;
		super.add(lblMemTotal, gbc); gbc.gridy++;
		super.add(lblDiskFree, gbc); gbc.gridy++;
		
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		super.add(Utilities.setButtonProps(new JButton(), Messages.getString("statisticsPanel.memory.runGC"), this, AC_GC, -1, null), gbc); //$NON-NLS-1$
	}
	
	public void actionPerformed(ActionEvent e) {
		final String ac = e.getActionCommand();
		
		if (ac == AC_GC) {
			System.gc();
		}
	}
	
	@Override
	public boolean update() {
		final Runtime runtime = Runtime.getRuntime();
		final long free = runtime.freeMemory();
		final long max = runtime.maxMemory();
		final long total = runtime.totalMemory();
		
		lblMemUsed.setText(String.format("%.2f MiB", Double.valueOf((total - free) / 1024.0 / 1024.0))); //$NON-NLS-1$
		lblMemFree.setText(String.format("%.2f MiB", Double.valueOf((max - total + free) / 1024.0 / 1024.0))); //$NON-NLS-1$
		lblMemTotal.setText(String.format("%.2f MiB", Double.valueOf(max / 1024.0 / 1024.0))); //$NON-NLS-1$
		lblDiskFree.setText(StatisticsPanel.NA);
		
		return true;
	}
}