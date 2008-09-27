
package org.paxle.desktop.impl.dialogues.bundles;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.text.JTextComponent;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;

import org.paxle.desktop.DIComponent;
import org.paxle.desktop.Utilities;
import org.paxle.desktop.impl.DesktopServices;
import org.paxle.desktop.impl.Messages;
import org.paxle.desktop.impl.dialogues.DIServicePanel;
import org.paxle.desktop.impl.dialogues.ScrollablePanel;

public class BundlePanel extends DIServicePanel implements DIComponent, DocumentListener {
	
	private static final long serialVersionUID = 1L;
	
	private final JTextComponent searchField = new JTextField();
	private final BundleListModel bundleListModel = new BundleListModel(this);
	private final GridBagLayout layout = new GridBagLayout();
	private final JPanel bundlePanel = new ScrollablePanel(layout, ScrollablePanel.ALWAYS_TRACK, ScrollablePanel.NO_TRACK);
	private final JScrollPane scroll = new JScrollPane();
	private final Runnable updater = new Runnable() {
		public void run() {
			bundleListModel.setRestriction(searchField.getText());
			bundlePanel.revalidate();
		}
	};
	
	boolean multipleChanged = false;
	
	public BundlePanel(final DesktopServices services) {
		super(services);
		super.services.getServiceManager().addBundleListener(bundleListModel);
		init();
		for (final Bundle bundle : services.getServiceManager().getBundles())
			bundleListModel.bundleChanged(bundle, BundleEvent.RESOLVED);
	}
	
	@Override
	public void close() {
		super.services.getServiceManager().removeBundleListener(bundleListModel);
		super.close();
	}
	
	public void modelDataChanged(final int type, final int idx, final List<BundleListRow> data) {
		switch (type) {
			case ListDataEvent.CONTENTS_CHANGED:
				for (final BundleListRow cc : data)
					cc.invalidate();
				break;
			case ListDataEvent.INTERVAL_ADDED:
				int i = idx;
				for (final BundleListRow cc : data)
					cc.setRow(bundlePanel, i++ * 4);
				break;
			case ListDataEvent.INTERVAL_REMOVED:
				for (final BundleListRow cc : data)
					cc.removeRow(bundlePanel);
				break;
		}
	}
	
	public void changedUpdate(DocumentEvent e) {
		SwingUtilities.invokeLater(updater);
	}
	
	public void insertUpdate(DocumentEvent e) {
		SwingUtilities.invokeLater(updater);
	}
	
	public void removeUpdate(DocumentEvent e) {
		SwingUtilities.invokeLater(updater);
	}
	
	private void init() {
		super.setLayout(new BorderLayout(0, 0));
		
		final JPanel searchPanel = new JPanel(new GridBagLayout());
		Utilities.addGridbag(searchPanel, new JLabel(Messages.getString("bundlePanel.search")), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.0, 0.0);
		Utilities.addGridbag(searchPanel, searchField, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1.0, 0.0);
		searchField.getDocument().addDocumentListener(this);
		super.add(searchPanel, BorderLayout.NORTH);
		
		scroll.setViewportView(bundlePanel);
		super.add(scroll, BorderLayout.CENTER);
	}
	
	@Override
	public String getTitle() {
		return Messages.getString("bundlePanel.title"); //$NON-NLS-1$
	}
	
}
