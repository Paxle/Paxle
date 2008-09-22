
package org.paxle.desktop.impl.dialogues.stats;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.lang.reflect.Method;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import org.paxle.desktop.Utilities;
import org.paxle.desktop.impl.DesktopServices;
import org.paxle.desktop.impl.Messages;
import org.paxle.desktop.impl.dialogues.stats.StatisticsPanel.Stats;

class DatabasePanel extends Stats {
	
	private static final long serialVersionUID = 1L;
	
	private static final String IINDEXER_SEARCHER = "org.paxle.se.index.IIndexSearcher"; //$NON-NLS-1$
	
	private final JLabel lblDocsKnown = new JLabel();
	
	private final DesktopServices services;
	
	public DatabasePanel(final DesktopServices services) {
		this.services = services;
		init();
	}
	
	private void init() {
		super.setLayout(new GridBagLayout());
		super.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Messages.getString("statisticsPanel.database"))); //$NON-NLS-1$
		
		final GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.gridx = 0; gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.insets = Utilities.INSETS_DEFAULT;
		gbc.anchor = GridBagConstraints.CENTER;
		super.add(chart, gbc);
		
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.EAST;
		super.add(new JLabel(Messages.getString("statisticsPanel.database.docsKnown")), gbc); //$NON-NLS-1$
		
		gbc.gridx++;
		gbc.anchor = GridBagConstraints.WEST;
		super.add(lblDocsKnown, gbc);
	}
	
	@Override
	public boolean isStatsDataSupported() {
		return services.getServiceManager().getService(IINDEXER_SEARCHER) != null;
	}
	
	@Override
	public void initChart() {
		if (sds != null) {
			sds.init(
					Messages.getString("statisticsPanel.database.chart.title"),
					Messages.getString("statisticsPanel.database.chart.yDesc"),
					Messages.getString("statisticsPanel.database.chart.numDocs"));
		}
	}
	
	@Override
	public boolean update() {
		final Object indexSearcher = services.getServiceManager().getService(IINDEXER_SEARCHER);
		if (indexSearcher == null)
			return false;
		try {
			final Method getDocCount = indexSearcher.getClass().getMethod("getDocCount"); //$NON-NLS-1$
			final Object docCount = getDocCount.invoke(indexSearcher);
			lblDocsKnown.setText(String.format("%,d", docCount)); //$NON-NLS-1$
			if (sds != null)
				sds.addOrUpdate((Number)docCount);
			return true;
		} catch (Exception e) { e.printStackTrace(); }
		return false;
	}
}
