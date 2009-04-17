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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.lang.reflect.Method;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import org.paxle.desktop.Utilities;
import org.paxle.desktop.impl.DesktopServices;
import org.paxle.desktop.impl.Messages;
import org.paxle.desktop.impl.ServiceManager;
import org.paxle.desktop.impl.dialogues.stats.StatisticsPanel.Stats;

class DatabasePanel extends Stats {
	
	private static final long serialVersionUID = 1L;
	
	private static final String IINDEXER_SEARCHER = "org.paxle.se.index.IIndexSearcher"; //$NON-NLS-1$
	private static final String ICOMMAND_DB = "org.paxle.data.db.ICommandDB";
	
	private final JLabel lblDocsKnown = new JLabel();
	private final JLabel lblUrisKnown = new JLabel();
	private final JLabel lblEnqueued = new JLabel();
	
	private final ServiceManager services;
	
	public DatabasePanel(final ServiceManager services) {
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
		
		Utilities.addGridbagLine(this, new JLabel(Messages.getString("statisticsPanel.database.docsKnown")), lblDocsKnown, ++gbc.gridy);
		Utilities.addGridbagLine(this, new JLabel(Messages.getString("statisticsPanel.database.urisKnown")), lblUrisKnown, ++gbc.gridy);
		Utilities.addGridbagLine(this, new JLabel(Messages.getString("statisticsPanel.database.enqueued")), lblEnqueued, ++gbc.gridy);
	}
	
	@Override
	public boolean isStatsDataSupported() {
		return services.getService(IINDEXER_SEARCHER) != null;
	}
	
	@Override
	public void initChart() {
		if (sds != null) {
			final String yDesc = Messages.getString("statisticsPanel.database.chart.yDesc");
			sds.addPlot(yDesc, Long.MIN_VALUE, Long.MAX_VALUE, null,
					Messages.getString("statisticsPanel.database.chart.numDocs"),
					Messages.getString("statisticsPanel.database.chart.numUris"));
			sds.addPlot(yDesc, Long.MIN_VALUE, Long.MAX_VALUE, null,
					Messages.getString("statisticsPanel.database.chart.numEnqueued"));
			sds.finish(Messages.getString("statisticsPanel.database.chart.title"), null);
		}
	}
	
	@Override
	public boolean update() {
		Object docCount = null, size = null, enqueued = null;
		
		final Object indexSearcher = services.getService(IINDEXER_SEARCHER);
		if (indexSearcher != null) try {
			final Method getDocCount = indexSearcher.getClass().getMethod("getDocCount"); //$NON-NLS-1$
			docCount = getDocCount.invoke(indexSearcher);
		} catch (Exception e) { e.printStackTrace(); }
		
		final Object cmddb = services.getService(ICOMMAND_DB);
		if (cmddb != null) try {
			final Class<?> clazz = cmddb.getClass();
			final Method mSize = clazz.getMethod("size");
			final Method mEnqueued = clazz.getMethod("enqueuedSize");
			size = mSize.invoke(cmddb);
			enqueued = mEnqueued.invoke(cmddb);
		} catch (Exception e) { e.printStackTrace(); }
		
		if (docCount != null || size != null) {
			lblDocsKnown.setText(String.format("%,d", docCount)); //$NON-NLS-1$
			lblUrisKnown.setText(String.format("%,d", size));
			if (sds != null)
				sds.addOrUpdate(0, new Number[] { (Number)docCount, (Number)size });
		}
		if (enqueued != null) {
			lblEnqueued.setText(String.format("%,d", enqueued));
			if (sds != null)
				sds.addOrUpdate(1, new Number[] { (Number)enqueued });
		}
		
		return (docCount != null || size != null || enqueued != null);
	}
}
