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
package org.paxle.desktop.impl.dialogues.cconsole;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;
import java.util.WeakHashMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import org.paxle.core.IMWComponent;
import org.paxle.core.MWComponentEvent;
import org.paxle.core.filter.CommandFilterEvent;
import org.paxle.core.prefs.Properties;
import org.paxle.core.queue.CommandEvent;
import org.paxle.core.queue.ICommand;
import org.paxle.core.queue.ICommandTracker;
import org.paxle.desktop.Utilities;
import org.paxle.desktop.impl.Messages;
import org.paxle.desktop.impl.ServiceManager;
import org.paxle.desktop.impl.ServiceManager.MWComponents;
import org.paxle.desktop.impl.dialogues.DIServicePanel;

public class CrawlingConsole extends DIServicePanel implements EventHandler, ActionListener {
	
	private static final long serialVersionUID = 1L;
	
	private static final Dimension DIM_CCONSOLE = new Dimension(500, 400);
	
	private static final String PROP_DISPLAYED_MWCOMP = "displayedMWComp"; //$NON-NLS-1$
	private static final String PROP_TABLE_DISPLAY = "tableDisplay"; //$NON-NLS-1$
	private static final String PROP_SHOW_ENQUEUED = "showEnqueued"; //$NON-NLS-1$
	private static final String PROP_SHOW_DESTROYED = "showDestroyed"; //$NON-NLS-1$
	private static final String PROP_COL_WIDTHS = "columnWidths"; //$NON-NLS-1$
	
	private static final String AC_CLEAR = new String();
	private static final String AC_CRAWL = new String();
	private static final String AC_SELECT = new String();
	private static final String AC_ENQUEUED = new String();
	private static final String AC_DESTROYED = new String();
	private static final String AC_SETTINGS = new String();
	
	private static final String LBL_PAUSE_ = Messages.getString("crawlingConsole.pause"); //$NON-NLS-1$
	private static final String LBL_RESUME_ = Messages.getString("crawlingConsole.resume"); //$NON-NLS-1$
	
	static interface TableColumnSpecs {
		public Object[] getColumnHeaders();
		public String name();
		public void insertValues(final Vector<String> row, final Event event, final ICommand cmd, final String lastFilter);
	};
	
	private static enum Events {
		IN_QUEUE, OUT_QUEUE, MWCOMP_STATE
	};
	
	private final WeakHashMap<ICommand,String> lastFilters = new WeakHashMap<ICommand,String>();
	
	private final Log           logger = LogFactory.getLog(CrawlingConsole.class);
	private final JScrollPane   scroll = new JScrollPane();
	private final JButton       clear  = Utilities.setButtonProps(new JButton(), Messages.getString("crawlingConsole.clear"), this, AC_CLEAR, KeyEvent.VK_C, null); //$NON-NLS-1$
	private final JCheckBox     cbEnq  = Utilities.setButtonProps(new JCheckBox(), Messages.getString("crawlingConsole.enqueued"), this, AC_ENQUEUED, -1, null); //$NON-NLS-1$
	private final JCheckBox     cbDstr = Utilities.setButtonProps(new JCheckBox(), Messages.getString("crawlingConsole.rejected"), this, AC_DESTROYED, -1, null); //$NON-NLS-1$
	
	private final JToggleButton cpb    = Utilities.setButtonProps(new JToggleButton(), null, this, AC_CRAWL, KeyEvent.VK_S, null);
	private final JTable        table;
	private final ConsoleTableModel model;
	private final JComboBox     cbox;
	private final ICommandTracker tracker;
	private final JPanel        options = new JPanel();
	
	private MWComponents currentComp;
	private boolean currentEnq, currentDstr;
	
	public CrawlingConsole(final ServiceManager services) {
		super(services, DIM_CCONSOLE);
		
		cbox = new JComboBox(MWComponents.humanReadableNames());
		cbox.addActionListener(this);
		tracker = services.getService(ICommandTracker.class);
		
		final Properties props = services.getServiceProperties();
		final String mwcomp = props.getProperty(PROP_DISPLAYED_MWCOMP, MWComponents.CRAWLER.name());
		final String tableDisplay = props.getProperty(PROP_TABLE_DISPLAY,
				// TableDisplay.WORKING_ON.name()
				Integer.toString(TableColumns.DEFAULT)
		);
		final String showEnqueued = props.getProperty(PROP_SHOW_ENQUEUED, Boolean.TRUE.toString());
		final String showDestroyed = props.getProperty(PROP_SHOW_DESTROYED, Boolean.FALSE.toString());
		
		model = new ConsoleTableModel();
		table = new JTable(model, new ConsoleTableColumnModel());
		table.setAutoCreateColumnsFromModel(true);
		model.setCols((tableDisplay.matches("\\d+"))
				? Integer.parseInt(tableDisplay)
		//		: TableDisplay.valueOf(tableDisplay)
				: TableColumns.DEFAULT
		);
		init(MWComponents.valueOf(mwcomp),
				Boolean.parseBoolean(showEnqueued),
				Boolean.parseBoolean(showDestroyed));
	}
	
	private int[] readColumnWidths(final TableColumnSpecs specs) {
		final Properties props = services.getServiceProperties();
		final String widthsProp = props.getProperty(PROP_COL_WIDTHS + specs.name(), null);
		if (widthsProp == null || widthsProp.length() == 0)
			return null;
		final String[] propSplit = widthsProp.split("[,]");
		final int[] widths = new int[propSplit.length];
		for (int i=0; i<propSplit.length; i++)
			widths[i] = Integer.parseInt(propSplit[i]);
		return widths;
	}
	
	private void saveColumnWidths(final TableColumnSpecs specs, final int[] widths) {
		if (widths == null || widths.length == 0)
			return;
		final StringBuilder sb = new StringBuilder();
		for (int i=0; i<widths.length;) {
			sb.append(Integer.toString(widths[i]));
			if (++i < widths.length)
				sb.append(',');
		}
		final Properties props = services.getServiceProperties();
		props.put(PROP_COL_WIDTHS + specs.name(), sb.toString());
	}
	
	@Override
	public void close() {
		final Properties props = super.services.getServiceProperties();
		props.put(PROP_DISPLAYED_MWCOMP, MWComponents.valueOfHumanReadable((String)cbox.getSelectedItem()).name());
		props.put(PROP_TABLE_DISPLAY, model.type.name());
		props.put(PROP_SHOW_ENQUEUED, Boolean.toString(cbEnq.isSelected()));
		props.put(PROP_SHOW_DESTROYED, Boolean.toString(cbDstr.isSelected()));
		saveColumnWidths(model.type, getCurrentWidths());
		super.close();
	}
	
	private void updateListeners(final MWComponents comp, final boolean enqueued, final boolean destroyed) {
		final String id = comp.getID();
		
		final boolean compUpdate = currentComp != comp;
		final boolean enqUpdate = compUpdate || (currentEnq ^ enqueued);
		final boolean dstrUpdate = compUpdate || (currentDstr ^ destroyed);
		
		if (compUpdate) {
			super.unregisterService(Events.MWCOMP_STATE);
			registerEventListener(Events.MWCOMP_STATE,
					MWComponentEvent.PROP_COMPONENT_ID, id,
					MWComponentEvent.TOPIC_ALL);
			currentComp = comp;
		}
		if (enqUpdate) {
			if (currentEnq)
				super.unregisterService(Events.IN_QUEUE);
			if (enqueued)
				registerEventListener(Events.IN_QUEUE,
						CommandEvent.PROP_COMPONENT_ID, id + ".in", //$NON-NLS-1$
						CommandEvent.TOPIC_DEQUEUED);
			currentEnq = enqueued;
		}
		if (dstrUpdate) {
			if (currentDstr)
				super.unregisterService(Events.OUT_QUEUE);
			if (destroyed)
				registerEventListener(Events.OUT_QUEUE,
						CommandEvent.PROP_COMPONENT_ID, id + ".out", //$NON-NLS-1$
						CommandFilterEvent.TOPIC_POST_FILTER, CommandEvent.TOPIC_DESTROYED);
			currentDstr = destroyed;
		}
	}

	@SuppressWarnings("unchecked")
	private void registerEventListener(final Events key, final String filterKey, final String filterVal, final String... topics) {
		final Hashtable<String,Object> props = new Hashtable<String,Object>();
		if (topics.length > 0)
			props.put(EventConstants.EVENT_TOPIC, topics);
		props.put(EventConstants.EVENT_FILTER, String.format("(%s=%s)", filterKey, filterVal)); //$NON-NLS-1$
		super.registerService(key, this, props, EventHandler.class);
	}
	
	@Override
	public String getTitle() {
		return Messages.getString("crawlingConsole.title"); //$NON-NLS-1$
	}
	
	private void updateCpb(final boolean paused, final boolean getState, final boolean setState) {
		if (getState && setState)
			throw new IllegalArgumentException("cannot set and get state at the same time"); //$NON-NLS-1$
		final String item = (String)cbox.getSelectedItem();
		final IMWComponent<?> mwComp = services.getMWComponent(MWComponents.valueOfHumanReadable(item));
		final boolean state = (getState && mwComp != null) ? mwComp.isPaused() : paused;
		
		final class PausedUpdateRunnable implements Runnable {
			public void run() {
				cpb.setVisible(mwComp != null);
				cpb.setSelected(state);
				cpb.setText(String.format((state) ? LBL_RESUME_ : LBL_PAUSE_, item));
			}
		};
		
		SwingUtilities.invokeLater(new PausedUpdateRunnable());
		
		if (setState && mwComp != null) {
			if (paused) {
				mwComp.pause();
			} else {
				mwComp.resume();
			}
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		final String ac = e.getActionCommand();
		if (ac == AC_CLEAR) {
			clear();
		} else if (ac == AC_CRAWL) {
			updateCpb(cpb.isSelected(), false, true);
		} else if (ac == AC_SELECT) {
			updateCpb(false, true, false);
		} else if (ac == AC_SETTINGS) {
			final JToggleButton btn = (JToggleButton)e.getSource();
			final boolean sel = btn.isSelected();
			btn.setText(((sel) ? '\u2191' : '\u2193') + btn.getText().substring(1));
			final boolean notInitialized = sel && options.getComponentCount() == 0;
			if (notInitialized)
				initOptions();
			final Runnable sr = new Runnable() {
				public void run() {
					int height = options.getHeight();
					frame.setSize(frame.getWidth(), frame.getHeight() + ((sel) ? height : -height));
				}
			};
			if (!notInitialized)
				sr.run();
			options.setVisible(sel);
			if (notInitialized)
				SwingUtilities.invokeLater(sr);
				
		}
		if (ac == AC_SELECT || ac == AC_ENQUEUED || ac == AC_DESTROYED) {
			final String compItem = (String)cbox.getSelectedItem();
			final boolean enq = cbEnq.isSelected();
			final boolean dstr = cbDstr.isSelected();
			if (logger.isDebugEnabled())
				logger.debug("received ActionEvent, item: '" + compItem + "', enq: " + enq + ", dstr: " + dstr);
			updateListeners(MWComponents.valueOfHumanReadable(compItem), enq, dstr);
			/*
			model.setType((cbDstr.isSelected()) ? TableDisplay.REJECTED : (cbEnq.isSelected()) ? TableDisplay.WORKING_ON
					: new TableColumns());
					*/
		}
	}
	
	private void clear() {
		model.clear();
		clear.setEnabled(false);
	}
	
	private void init(
			final MWComponents comp,
			final boolean showEnqueued,
			final boolean showDestroyed) {
		scroll.setViewportView(table);
		updateCpb(false, true, false);
		
		cbEnq.setSelected(showEnqueued);
		cbDstr.setSelected(showDestroyed);
		
		cbox.setActionCommand(AC_SELECT);
		cbox.setSelectedIndex(comp.ordinal());
		
		final JPanel bbLeft = new JPanel(new FlowLayout(FlowLayout.LEFT));
		bbLeft.add(cpb);
		bbLeft.add(cbox);
		bbLeft.add(cbEnq);
		bbLeft.add(cbDstr);
		
		final JPanel bbRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		clear.setEnabled(false);
		bbRight.add(Utilities.setButtonProps(new JToggleButton(), "\u2193 " + Messages.getString("crawlingConsole.settings"), this, AC_SETTINGS, KeyEvent.VK_S, null));
		bbRight.add(clear);
		
		
		final JPanel b = new JPanel(new BorderLayout());
		b.add(bbLeft, BorderLayout.WEST);
		b.add(bbRight, BorderLayout.EAST);
		options.setVisible(false);
		b.add(options, BorderLayout.SOUTH);
		
		super.setLayout(new BorderLayout());
		super.add(scroll, BorderLayout.CENTER);
		super.add(b, BorderLayout.SOUTH);
	}
	
	private final int[] getCurrentWidths() {
		final TableColumnModel tcm = table.getTableHeader().getColumnModel();
		final int[] widths = new int[tcm.getColumnCount()];
		for (int i=0; i<widths.length; i++)
			widths[i] = tcm.getColumn(i).getWidth();
		return widths;
	}
	
	private final class ConsoleTableModel extends DefaultTableModel {
		
		private static final long serialVersionUID = 1L;
		
		private final TableColumns type = new TableColumns(0);
		private int maxSize = 100;
		
		public ConsoleTableModel() {
		}
		
		public void setCols(final int cols) {
			if (cols == type.showCmd)
				return;
			
			if (type.showCmd != 0) {
				// save old widths
				final int[] oldWidths = getCurrentWidths();
				saveColumnWidths(type, oldWidths);
				logger.debug("Saving old widths: " + Arrays.toString(oldWidths));
			}
			
			// set identifiers
			type.showCmd = cols;
			super.setColumnIdentifiers(type.getColumnHeaders());
			
			// set new widths
			final int[] newWidths = readColumnWidths(type);
			logger.debug("New widhts: " + Arrays.toString(newWidths));
			final TableColumnModel headerCols = table.getTableHeader().getColumnModel();
			if (newWidths != null && headerCols.getColumnCount() > 0)
				for (int i=0; i<newWidths.length; i++)
					headerCols.getColumn(i).setPreferredWidth(newWidths[i]);
		}
		
		/*
		public void setType(final TableColumnSpecs type) {
			if (this.type != type) {
				if (type != null) {
					if (this.type != null) {
						// save old widths
						final int[] oldWidths = getCurrentWidths();
						saveColumnWidths(type, oldWidths);
						logger.debug("Saving old widths: " + Arrays.toString(oldWidths));
					}
					
					// set identifiers
					super.setColumnIdentifiers(type.getColumnHeaders());
					// set new widths
					final int[] newWidths = readColumnWidths(type);
					logger.debug("New widhts: " + Arrays.toString(newWidths));
					final TableColumnModel headerCols = table.getTableHeader().getColumnModel();
					if (newWidths != null && headerCols.getColumnCount() > 0) {
						for (int i=0; i<newWidths.length; i++)
							headerCols.getColumn(i).setPreferredWidth(newWidths[i]);
					}
				}
				this.type = type;
			}
		}*/
		
		public void setMaxSize(int maxSize) {
			final int current = super.getRowCount();
			if (maxSize >= current) {
				super.setRowCount(maxSize);
			} else {
				for (int i=0; i<current - maxSize; i++)
					dataVector.remove(0);
				super.fireTableRowsDeleted(0, current - maxSize);
			}
			this.maxSize = maxSize;
		}
		
		public void clear() {
			super.dataVector.clear();
			super.fireTableDataChanged();
		}
		
		public void addEventLine(final Event event, final ICommand cmd) {
			if (type == null)
				return;
			
			final Vector<String> row = new Vector<String>();
			type.insertValues(row, event, cmd, lastFilters.get(cmd));
			
			try {
				SwingUtilities.invokeAndWait(new Scroller(scroll, table, this).init(row, clear, maxSize));
			} catch (Exception e) { e.printStackTrace(); }
		}
	};
	
	private void initOptions() {
		options.setLayout(new GridBagLayout());
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = Utilities.INSETS_ZERO;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridx = gbc.gridy = 0;
		final int show = model.type.showCmd;
		for (final Columns col : Columns.values()) {
			final int mask = 1 << col.ordinal();
			final JCheckBox cbox = new JCheckBox();
			cbox.setSelected((show & mask) != 0);
			
			// TODO: make static by setting an action-command which represents the respective cbox
			final class CboxAC implements ActionListener {
				public void actionPerformed(ActionEvent e) {
					if (cbox.isSelected()) {
						model.setCols(model.type.showCmd | mask);
					} else {
						model.setCols(model.type.showCmd & ~mask);
					}
				}
			};
			
			options.add(Utilities.setButtonProps(cbox, col.getL10n(), new CboxAC(), null, -1, null), gbc);
			
			if (gbc.gridy < 5) {
				gbc.gridy++;
			} else {
				gbc.gridx++;
				gbc.gridy = 0;
			}
		}
	}
	
	private void handleCommandEvent(final Event event) {
		final String topic = event.getTopic();
		final Long id = (Long)event.getProperty(CommandEvent.PROP_COMMAND_ID);
		final ICommand cmd = tracker.getCommandByID(id);
		
		if (topic.equals(CommandFilterEvent.TOPIC_POST_FILTER)) {
			lastFilters.put(cmd, (String)event.getProperty(CommandFilterEvent.PROP_FILTER_NAME));
		} else {
			model.addEventLine(event, cmd);
		}
	}
	
	private void handleMWComponentEvent(final Event event) {
		updateCpb(event.getTopic() == MWComponentEvent.TOPIC_PAUSED, false, false);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.service.event.EventHandler#handleEvent(Event)
	 */
	public void handleEvent(Event event) {
		final String topic = event.getTopic();
		if (logger.isDebugEnabled())
			logger.debug("Received event '" + event +
					"', topic: '" + topic +
					"', keys: " + Arrays.toString(event.getPropertyNames()) +
					", componentID: '" + event.getProperty("componentID"));
		if (topic.startsWith(CommandEvent.TOPIC_)) {
			handleCommandEvent(event);
		} else if (topic.startsWith(MWComponentEvent.TOPIC_)) {
			handleMWComponentEvent(event);
		}
	}
};
