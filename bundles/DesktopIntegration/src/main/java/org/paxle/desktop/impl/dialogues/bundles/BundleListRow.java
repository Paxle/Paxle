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
package org.paxle.desktop.impl.dialogues.bundles;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.border.Border;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

import org.paxle.desktop.Utilities;
import org.paxle.desktop.impl.Messages;

public final class BundleListRow implements ActionListener, Comparable<BundleListRow> {
	
	private static final long serialVersionUID = 1L;
	
	private static final String AC_GO = new String();
	private static final String AC_SHOW_RUNTIME_INFO = "0";
	private static final String AC_SHOW_MANIFEST = "1";
	private static final String AC_SHOW_EXPORTED = "2";
	private static final String AC_SHOW_IMPORTED = "3";
	
	private static enum BundleStateActions {
		START,
		STOP,
		RESTART,
		UPDATE,
		UNINSTALL
		
		;
		
		private final String label;
		
		private BundleStateActions() {
			this.label = Messages.getString("bundlePanel.cell.action." + name().toLowerCase());
		}
		
		@Override
		public String toString() {
			return label;
		}
	}
	
	private static final class StateTupel {
		final String label;
		final Color color;
		final Icon icon;
		
		StateTupel(final String label, final Color color, final String iconPath) {
			this.label = label;
			this.color = color;
			this.icon = new ImageIcon("/resources/" + iconPath);
		}
	}
	
	private static final HashMap<Integer,BundleListRow.StateTupel> STATES = new HashMap<Integer,BundleListRow.StateTupel>();
	static {
		STATES.put(Integer.valueOf(Bundle.ACTIVE),
				new StateTupel(Messages.getString("bundlePanel.cell.bundle.active"), 		Color.GREEN, 		"plugin_go.png"));
		STATES.put(Integer.valueOf(Bundle.INSTALLED),
				new StateTupel(Messages.getString("bundlePanel.cell.bundle.installed"), 	Color.BLUE, 		"plugin_disabled.png"));
		STATES.put(Integer.valueOf(Bundle.RESOLVED),
				new StateTupel(Messages.getString("bundlePanel.cell.bundle.resolved"), 		Color.GRAY, 		"plugin_link.png"));
		STATES.put(Integer.valueOf(Bundle.STARTING),
				new StateTupel(Messages.getString("bundlePanel.cell.bundle.starting"), 		Color.YELLOW, 		"plugin_go.png"));
		STATES.put(Integer.valueOf(Bundle.STOPPING),
				new StateTupel(Messages.getString("bundlePanel.cell.bundle.stopping"), 		Color.RED, 			"plugin_delete.png"));
		STATES.put(Integer.valueOf(Bundle.UNINSTALLED),
				new StateTupel(Messages.getString("bundlePanel.cell.bundle.uninstalled"), 	Color.DARK_GRAY, 	"plugin_delete.png"));
	}
	
	static final int DIMX = 7;
	static final int DIMY = 4;
	
	private final JPanel bg = new JPanel();
	
	private final JLabel id = new JLabel();
	private final JLabel icon = new JLabel();
	private final JLabel title = new JLabel();
	private final JLabel symbolicName = new JLabel();
	private final JTextArea description = Utilities.setTextLabelDefaults(new JTextArea());
	private final JLabel version = new JLabel();
	private final JButton btnGo = Utilities.setButtonProps(new JButton(), Messages.getString("bundlePanel.cell.comp.go"), this, AC_GO, KeyEvent.VK_O, null);
	
	private final JComboBox actions = new JComboBox(BundleStateActions.values());
	private final JPanel btnsPanel = new JPanel();
	private final JPanel infoMainPanel = new JPanel(new GridBagLayout());
	private final JPanel[] infoPanels = new JPanel[4];
	private final JToggleButton[] infoBtns = new JToggleButton[] {
			Utilities.setButtonProps(new JToggleButton(), Messages.getString("bundlePanel.cell.show.runtime"), this, AC_SHOW_RUNTIME_INFO, -1, null),
			Utilities.setButtonProps(new JToggleButton(), Messages.getString("bundlePanel.cell.show.manifest"), this, AC_SHOW_MANIFEST, -1, null),
			Utilities.setButtonProps(new JToggleButton(), Messages.getString("bundlePanel.cell.show.exported"), this, AC_SHOW_EXPORTED, -1, null),
			Utilities.setButtonProps(new JToggleButton(), Messages.getString("bundlePanel.cell.show.imported"), this, AC_SHOW_IMPORTED, -1, null)
	};
	private int infoPanelsInitialized = 0;
	
	private final HashSet<Component> comps = new HashSet<Component>();
	
	final Bundle bundle;
	int index;
	
	public BundleListRow(final Bundle bundle) {
		this.bundle = bundle;
		
		final GridBagConstraints gbcInfo = new GridBagConstraints();
		gbcInfo.gridx = gbcInfo.gridy = 0;
		gbcInfo.anchor = GridBagConstraints.WEST;
		gbcInfo.fill = GridBagConstraints.HORIZONTAL;
		gbcInfo.weightx = 1.0;
		gbcInfo.insets = Utilities.INSETS_DEFAULT;
		for (int i=0; i<infoPanels.length; i++) {
			infoMainPanel.add(infoPanels[i] = new JPanel(new GridBagLayout()), gbcInfo);
			gbcInfo.gridy++;
		}
		
		for (int i=0; i<infoBtns.length; i++) {
			btnsPanel.add(infoBtns[i]);
		}
		
		final LinkedList<Container> l = new LinkedList<Container>(Arrays.asList(
				(Container)bg, id, icon, title, symbolicName, description, version, btnGo, actions, btnsPanel, infoMainPanel
		));
		for (final Container c : infoPanels)
			l.add(c);
		while (!l.isEmpty()) {
			final Container c = l.poll();
			comps.add(c);
			for (final Component cc : c.getComponents())
				if (cc instanceof Container) {
					l.add((Container)cc);
				} else {
					comps.add(cc);
				}
		}
		
		btnsPanel.setBackground(new Color(0,0,0,0));
		description.setBackground(new Color(0,0,0,0));
		description.setLineWrap(true);
		description.setWrapStyleWord(true);
		
		updateBundleStatus();
		updateDisplay(false, false);
	}
	
	public int compareTo(BundleListRow o) {
		final long delta = bundle.getBundleId() - o.bundle.getBundleId();
		return (delta < 0L) ? -1 : (delta > 0L) ? 1 : 0;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BundleListRow)
			return bundle.getBundleId() == ((BundleListRow)obj).bundle.getBundleId();
		return super.equals(obj);
	}
	
	public void updateDisplay(final boolean isSelected, final boolean hasFocus) {
		btnsPanel.setVisible(isSelected || hasFocus);
	//	infoMainPanel.setVisible(isSelected || hasFocus);
	}
	
	public void updateBundleStatus() {
		final Dictionary<?,?> headers = bundle.getHeaders();
		final BundleListRow.StateTupel st = STATES.get(Integer.valueOf(bundle.getState()));
		final Object name = headers.get(Constants.BUNDLE_NAME);
		final Object desc = headers.get(Constants.BUNDLE_DESCRIPTION);
		final Object vers = headers.get(Constants.BUNDLE_VERSION);
		
		id.setText(Long.toString(bundle.getBundleId()));
		icon.setIcon(st.icon);
		title.setText((name == null) ? "" : name.toString());
		title.setToolTipText(st.label);
		id.setBackground(st.color);
		symbolicName.setText(bundle.getSymbolicName());
		description.setText(normalizeDescription(desc));
		version.setText((vers == null) ? "" : vers.toString());
		
		infoPanelsInitialized = 0;
		for (int i=0; i<infoPanels.length; i++) {
			infoPanels[i].removeAll();
			setInfoPanelVisible(i, infoBtns[i].isSelected());
		}
	}
	
	private static String normalizeDescription(final Object desc) {
		if (desc == null)
			return "";
		return desc.toString().replaceAll("\\s+", " ");
	}
	
	public void removeRow(final JComponent comp, final ML ml) {
		comp.remove(id);
		comp.remove(icon);
		comp.remove(title);
		comp.remove(description);
		comp.remove(btnsPanel);
		comp.remove(symbolicName);
		comp.remove(version);
		comp.remove(actions);
		comp.remove(btnGo);
		comp.remove(infoMainPanel);
		comp.remove(bg);
		
		ml.removeFromComps(comps);
	}
	
	public void invalidate() {
		updateBundleStatus();
		id.invalidate();
		icon.invalidate();
		title.invalidate();
		description.invalidate();
		btnsPanel.invalidate();
		symbolicName.invalidate();
		version.invalidate();
		btnGo.invalidate();
		infoMainPanel.invalidate();
	}
	
	public void setRow(final JComponent comp, final int y, final ML ml) {
		
		ml.addToComps(comps, this);
		
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0; gbc.gridy = y;
		gbc.insets = Utilities.INSETS_DEFAULT;
		gbc.insets.bottom = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		
		gbc.gridheight = 3;
		
		comp.add(id, gbc); gbc.gridx++;
		comp.add(icon, gbc); gbc.gridx++;
		
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		comp.add(title, gbc); gbc.gridy++;
		
		gbc.gridwidth = 3;
		comp.add(description, gbc); gbc.gridy++;
		comp.add(btnsPanel, gbc); gbc.gridy = y; gbc.gridx++;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridwidth = 1;
		
		gbc.anchor = GridBagConstraints.NORTHEAST;
		comp.add(symbolicName, gbc); gbc.gridx++;
		
		gbc.anchor = GridBagConstraints.NORTHEAST;
		comp.add(version, gbc); gbc.gridx++;
		gbc.gridheight = 3;
		gbc.anchor = GridBagConstraints.WEST;
		comp.add(actions, gbc); gbc.gridx++;
		comp.add(btnGo, gbc);
		
		final GridBagConstraints gbcInfo = new GridBagConstraints();
		gbcInfo.gridx = 1;
		gbcInfo.gridy = y + 3;
		gbcInfo.gridwidth = 6;
		gbcInfo.anchor = GridBagConstraints.WEST;
		gbcInfo.fill = GridBagConstraints.HORIZONTAL;
		gbcInfo.weightx = 1.0;
		gbcInfo.insets = Utilities.INSETS_ZERO;
		comp.add(infoMainPanel, gbcInfo);
		
		final GridBagConstraints gbcbg = new GridBagConstraints();
		gbcbg.gridx = 0;
		gbcbg.gridy = y;
		gbcbg.fill = GridBagConstraints.BOTH;
		gbcbg.gridwidth = DIMX;
		gbcbg.gridheight = DIMY;
		gbcbg.weightx = 1.0;
		gbcbg.weighty = 1.0;
		gbcbg.insets = Utilities.INSETS_ZERO;
		bg.setBackground(((y & DIMY) == 0) ? new Color(238,238,238) : new Color(218,218,218));		// TODO: generate the values
		comp.add(bg, gbcbg);
	}
	
	public void actionPerformed(ActionEvent e) {
		final String ac = e.getActionCommand();
		if (ac == AC_GO) {
			try {
				switch ((BundleListRow.BundleStateActions)actions.getSelectedItem()) {
					case START: 	bundle.start(); break;
					case STOP: 		bundle.stop(); break;
					case RESTART: 	bundle.stop(); bundle.start(); break;
					case UPDATE: 	bundle.update(); break;
					case UNINSTALL: bundle.uninstall(); break;
				}
			} catch (BundleException ex) { ex.printStackTrace(); }
		} else {
			final boolean toggled = ((JToggleButton)e.getSource()).isSelected();
			final int num = Integer.parseInt(ac);
			setInfoPanelVisible(num, toggled);
		}
	}
	
	private void setInfoPanelVisible(final int num, final boolean yes) {
		if (yes && (infoPanelsInitialized & (1 << num)) == 0)
			initInfo(num);
		infoPanels[num].setVisible(yes);
	}
	
	private void initInfo(final int num) {
		final JPanel panel = infoPanels[num];
		final String[][] data;
		final Object[] header;
		final String title;
		switch (num) {
			case 0: {
				title = Messages.getString("bundlePanel.cell.rt.title");
				final DateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
				data = new String[][] {
						{ Messages.getString("bundlePanel.cell.rt.id"),			Long.toString(bundle.getBundleId()) },
						{ Messages.getString("bundlePanel.cell.rt.location"), 	bundle.getLocation() },
						{ Messages.getString("bundlePanel.cell.rt.state"),		STATES.get(Integer.valueOf(bundle.getState())).label },
						{ Messages.getString("bundlePanel.cell.rt.lastmod"),	format.format(new Date(bundle.getLastModified())) }
				};
				header = new Object[] { Messages.getString("bundlePanel.cell.rt.name"), Messages.getString("bundlePanel.cell.rt.value") };
			} break;
			case 1: {
				title = Messages.getString("bundlePanel.cell.manifest.title");
				final Dictionary<?,?> headers = bundle.getHeaders();
				data = new String[headers.size()][2];
				final Enumeration<?> keyEnum = headers.keys();
				int idx = 0;
				while (keyEnum.hasMoreElements()) {
					final Object key = keyEnum.nextElement();
					data[idx][0] = toString(key, 0);
					data[idx][1] = toString(headers.get(key),
							(key.equals(Constants.BUNDLE_DESCRIPTION) || key.equals(Constants.BUNDLE_COPYRIGHT)) ? 0 : 1);
					idx++;
				}
				header = new Object[] { Messages.getString("bundlePanel.cell.manifest.name"), Messages.getString("bundlePanel.cell.manifest.value") };
			} break;
			case 2: {
				title = Messages.getString("bundlePanel.cell.exported.title");
				data = createTableData(bundle.getRegisteredServices());
				header = new Object[] {
							Messages.getString("bundlePanel.cell.exported.id"),
							Messages.getString("bundlePanel.cell.exported.classes"),
							Messages.getString("bundlePanel.cell.imported.properties") };
			} break;
			case 3: {
				title = Messages.getString("bundlePanel.cell.imported.title");
				data = createTableData(bundle.getServicesInUse());
				header = new Object[] {
							Messages.getString("bundlePanel.cell.imported.id"),
							Messages.getString("bundlePanel.cell.imported.classes"),
							Messages.getString("bundlePanel.cell.imported.properties") };
			} break;
			
			default:
				throw new RuntimeException("illegal info panel index: " + num);
		}
		
		panel.setLayout(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), title));
		
		final GridBagConstraints gbc = new GridBagConstraints();
		// gbc.weightx = 1.0;
		gbc.insets = Utilities.INSETS_DEFAULT;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridy = 0;
		final Border titleColBorder = BorderFactory.createMatteBorder(0, 0, 2, 0, Color.BLACK);
		for (gbc.gridx=0; gbc.gridx<header.length; gbc.gridx++) {
			gbc.weightx = (gbc.gridx == 0) ? 0.0 : 1.0;
			final JLabel titleCol = new JLabel(String.valueOf(header[gbc.gridx]));
			titleCol.setBorder(titleColBorder);
			panel.add(titleCol, gbc);
		}
		
		gbc.anchor = GridBagConstraints.NORTHWEST;
		for (gbc.gridy=1; gbc.gridy<=data.length; gbc.gridy++)
			for (gbc.gridx=0; gbc.gridx<data[gbc.gridy-1].length; gbc.gridx++) {
				JTextArea comp = Utilities.setTextLabelDefaults(new JTextArea(data[gbc.gridy-1][gbc.gridx]));
				if (gbc.gridx > 0) {
					comp.setWrapStyleWord(true);
					comp.setLineWrap(true);
					gbc.weightx = 1.0;
				} else {
					comp.setWrapStyleWord(false);
					comp.setLineWrap(false);
					gbc.weightx = 0.0;
				}
				panel.add(comp, gbc);
			}
		
		infoPanelsInitialized |= 1 << num;
	}
	
	private static String toString(final Object o, final int split) {
		String r;
		final Class<?> c = o.getClass();
		if (c.isArray()) {
			final ArrayList<String> rr = new ArrayList<String>();
			for (final Object oo : (Object[])o)
				rr.add(toString(oo, split - 1));
			r = rr.toString();
			if (split > 0)
				r = split(r, ',', "[", "]");
		} else {
			r = String.valueOf(o);
			if (split > 0)
				r = split(r, ',', "\"", "\"");
		}
		return r;
	}
	
	private static String split(final String s, final char where, final String excludeStart, final String excludeEnd) {
		final StringBuilder sb = new StringBuilder(s.length());
		String exStart = excludeStart, exEnd = excludeEnd, exBoth = "";
		for (int i=0; i<excludeStart.length(); i++) {
			final char c = excludeStart.charAt(i);
			final int j = excludeEnd.indexOf(c);
			if (j != -1) {
				exBoth += excludeStart.charAt(i);
				exStart = exStart.substring(0, i) + ((i + 1 < exStart.length()) ? exStart.substring(i + 1) : "");
				exEnd = exEnd.substring(0, j) + ((j + 1 < exEnd.length()) ? exEnd.substring(j + 1) : "");
			}
		}
		int level = 0;
		for (int i=0; i<s.length(); i++) {
			char c = s.charAt(i);
			if (exStart.indexOf(c) != -1) {
				level++;
			} else if (exEnd.indexOf(c) != -1) {
				level--;
			} else if (exBoth.indexOf(c) != -1) {
				level = (level == 0) ? 1 : 0;
			} else if (c == where && level == 0) {
				c = '\n';
			}
			sb.append(c);
		}
		return sb.toString();
	}
	
	private static String[][] createTableData(final ServiceReference[] refs) {
		if (refs == null)
			return new String[0][3];
		final String[][] data = new String[refs.length][3];
		for (int i=0; i<refs.length; i++) {
			data[i][0] = String.valueOf(refs[i].getProperty(Constants.SERVICE_ID));
			data[i][1] = toString(refs[i].getProperty(Constants.OBJECTCLASS), 1);
			final ArrayList<String> otherKeys = new ArrayList<String>();
			for (final String key : refs[i].getPropertyKeys())
				if (!Constants.SERVICE_ID.equals(key) && !Constants.OBJECTCLASS.equals(key))
					otherKeys.add(key + " = " + refs[i].getProperty(key));
			data[i][2] = otherKeys.toString();
		}
		return data;
	}
}
