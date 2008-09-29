
package org.paxle.desktop.impl.dialogues;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;
import java.util.WeakHashMap;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

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
import org.paxle.desktop.impl.DesktopServices;
import org.paxle.desktop.impl.Messages;
import org.paxle.desktop.impl.DesktopServices.MWComponents;

public class CrawlingConsole extends DIServicePanel implements EventHandler, ActionListener {
	
	private static final long serialVersionUID = 1L;
	
	private static final Dimension DIM_CCONSOLE = new Dimension(500, 400);
	
	private static final String PROP_DISPLAYED_MWCOMP = "displayedMWComp"; //$NON-NLS-1$
	private static final String PROP_URLS_ENCODED = "urlsEncoded"; //$NON-NLS-1$
	private static final String PROP_TABLE_DISPLAY = "tableDisplay"; //$NON-NLS-1$
	private static final String PROP_SHOW_ENQUEUED = "showEnqueued"; //$NON-NLS-1$
	private static final String PROP_SHOW_DESTROYED = "showDestroyed"; //$NON-NLS-1$
	
	private static final String AC_CLEAR = new String();
	private static final String AC_CRAWL = new String();
	private static final String AC_SELECT = new String();
	private static final String AC_ENQUEUED = new String();
	private static final String AC_DESTROYED = new String();
	
	private static final String LBL_PAUSE_ = Messages.getString("crawlingConsole.pause"); //$NON-NLS-1$
	private static final String LBL_RESUME_ = Messages.getString("crawlingConsole.resume"); //$NON-NLS-1$
	
	private static enum TableDisplay {
		WORKING_ON(Messages.getString("crawlingConsole.tbl1.comp"), Messages.getString("crawlingConsole.tbl1.result"), Messages.getString("crawlingConsole.tbl1.uri")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		REJECTED(Messages.getString("crawlingConsole.tbl2.comp"), Messages.getString("crawlingConsole.tbl2.result"), Messages.getString("crawlingConsole.tbl2.filter"), Messages.getString("crawlingConsole.tbl2.reason"), Messages.getString("crawlingConsole.tbl2.uri")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		
		final Object[] columnHeaders;
		
		private TableDisplay(final Object... columnHeaders) {
			this.columnHeaders = columnHeaders;
		}
	}
	
	private static enum Events {
		IN_QUEUE, OUT_QUEUE, MWCOMP_STATE
	}
	
	private final WeakHashMap<ICommand,String> lastFilters = new WeakHashMap<ICommand,String>();
	
	private final Log           logger = LogFactory.getLog(CrawlingConsole.class);
	private final JScrollPane   scroll = new JScrollPane();
	private final JButton       clear  = Utilities.setButtonProps(new JButton(), Messages.getString("crawlingConsole.clear"), this, AC_CLEAR, -1, null); //$NON-NLS-1$
	private final JRadioButton  enc    = new JRadioButton(Messages.getString("crawlingConsole.urlEncoded")); //$NON-NLS-1$
	private final JRadioButton  normal = new JRadioButton(Messages.getString("crawlingConsole.urlOriginal")); //$NON-NLS-1$
	private final JCheckBox     cbEnq  = Utilities.setButtonProps(new JCheckBox(), Messages.getString("crawlingConsole.enqueued"), this, AC_ENQUEUED, -1, null); //$NON-NLS-1$
	private final JCheckBox     cbDstr = Utilities.setButtonProps(new JCheckBox(), Messages.getString("crawlingConsole.rejected"), this, AC_DESTROYED, -1, null); //$NON-NLS-1$
	
	private final JToggleButton cpb    = Utilities.setButtonProps(new JToggleButton(), null, this, AC_CRAWL, -1, null);
	private final JTable        table;
	private final ConsoleTableModel model;
	private final JComboBox     cbox;
	private final ICommandTracker tracker;
	
	private MWComponents currentComp;
	private boolean currentEnq, currentDstr;
	
	public CrawlingConsole(final DesktopServices services) {
		super(services, DIM_CCONSOLE);
		cbox = new JComboBox(DesktopServices.MWComponents.humanReadableNames());
		cbox.addActionListener(this);
		tracker = services.getServiceManager().getService(ICommandTracker.class);
		
		final Properties props = services.getServiceManager().getServiceProperties();
		final String mwcomp = props.getProperty(PROP_DISPLAYED_MWCOMP, MWComponents.CRAWLER.name());
		final String urlsEncoded = props.getProperty(PROP_URLS_ENCODED, Boolean.TRUE.toString());
		final String tableDisplay = props.getProperty(PROP_TABLE_DISPLAY, TableDisplay.WORKING_ON.name());
		final String showEnqueued = props.getProperty(PROP_SHOW_ENQUEUED, Boolean.TRUE.toString());
		final String showDestroyed = props.getProperty(PROP_SHOW_DESTROYED, Boolean.FALSE.toString());
		model = new ConsoleTableModel(TableDisplay.valueOf(tableDisplay));
		table = new JTable(model, new ConsoleTableColumnModel());
		table.setAutoCreateColumnsFromModel(true);
		init(DesktopServices.MWComponents.valueOf(mwcomp),
				Boolean.parseBoolean(urlsEncoded),
				Boolean.parseBoolean(showEnqueued),
				Boolean.parseBoolean(showDestroyed));
	}
	
	@Override
	public void close() {
		final Properties props = super.services.getServiceManager().getServiceProperties();
		props.put(PROP_DISPLAYED_MWCOMP, MWComponents.valueOfHumanReadable((String)cbox.getSelectedItem()).name());
		props.put(PROP_URLS_ENCODED, Boolean.toString(enc.isSelected()));
		props.put(PROP_TABLE_DISPLAY, model.type.name());
		props.put(PROP_SHOW_ENQUEUED, Boolean.toString(cbEnq.isSelected()));
		props.put(PROP_SHOW_DESTROYED, Boolean.toString(cbDstr.isSelected()));
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
		final IMWComponent<?> mwComp = services.getMWComponent(DesktopServices.MWComponents.valueOfHumanReadable(item));
		final boolean state = (getState && mwComp != null) ? mwComp.isPaused() : paused;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				cpb.setVisible(mwComp != null);
				cpb.setSelected(state);
				cpb.setText(String.format((state) ? LBL_RESUME_ : LBL_PAUSE_, item));
			}
		});
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
		}
		if (ac == AC_SELECT || ac == AC_ENQUEUED || ac == AC_DESTROYED) {
			final String compItem = (String)cbox.getSelectedItem();
			final boolean enq = cbEnq.isSelected();
			final boolean dstr = cbDstr.isSelected();
			if (logger.isDebugEnabled())
				logger.debug("received ActionEvent, item: '" + compItem + "', enq: " + enq + ", dstr: " + dstr);
			updateListeners(MWComponents.valueOfHumanReadable(compItem), enq, dstr);
			
			model.setType((cbDstr.isSelected()) ? TableDisplay.REJECTED : (cbEnq.isSelected()) ? TableDisplay.WORKING_ON : null);
		}
	}
	
	private void clear() {
		model.clear();
		clear.setEnabled(false);
	}
	
	private void init(
			final DesktopServices.MWComponents comp,
			final boolean showUrlsEncoded,
			final boolean showEnqueued,
			final boolean showDestroyed) {
		scroll.setViewportView(table);
		updateCpb(false, true, false);
		
		final ButtonGroup bg = new ButtonGroup();
		bg.add(enc);
		bg.add(normal);
		((showUrlsEncoded) ? enc : normal).setSelected(true);
		final JPanel bLeft = new JPanel(new FlowLayout(FlowLayout.LEFT));
		bLeft.add(normal);
		bLeft.add(enc);
		
		cbEnq.setSelected(showEnqueued);
		cbDstr.setSelected(showDestroyed);
		
		cbox.setActionCommand(AC_SELECT);
		cbox.setSelectedIndex(comp.ordinal());
		final JPanel bRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		clear.setEnabled(false);
		bRight.add(clear);
		
		final JPanel bbLeft = new JPanel(new FlowLayout(FlowLayout.LEFT));
		bbLeft.add(cbox);
		bbLeft.add(cbEnq);
		bbLeft.add(cbDstr);
		
		final JPanel bbRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		bbRight.add(cpb);
		
		final JPanel b = new JPanel(new BorderLayout());
		b.add(bLeft, BorderLayout.WEST);
		b.add(bRight, BorderLayout.EAST);
		final JPanel bb = new JPanel(new BorderLayout());
		bb.add(bbLeft, BorderLayout.WEST);
		bb.add(bbRight, BorderLayout.EAST);
		b.add(bb, BorderLayout.SOUTH);
		
		super.setLayout(new BorderLayout());
		super.add(scroll, BorderLayout.CENTER);
		super.add(b, BorderLayout.SOUTH);
	}
	
	// sets a DefaultTableCellRenderer for every new Column, otherwise JTable would throw NPEs
	// while repainting, haven't figured out yet, why...
	private static final class ConsoleTableColumnModel extends DefaultTableColumnModel {
		
		private static final long serialVersionUID = 1L;
		
		private static final DefaultTableCellRenderer TCR = new DefaultTableCellRenderer();
		
		public ConsoleTableColumnModel() {
		}
		
		@Override
		public TableColumn getColumn(int columnIndex) {
			return prepareColumn(super.getColumn(columnIndex));
		}
		
		private static TableColumn prepareColumn(final TableColumn tc) {
			if (tc.getCellRenderer() == null)
				tc.setCellRenderer(TCR);
			return tc;
		}
		
		@Override
		protected void fireColumnAdded(TableColumnModelEvent e) {
			for (int i=e.getFromIndex(); i<=e.getToIndex(); i++)
				prepareColumn(tableColumns.get(i));
			super.fireColumnAdded(e);
		}
	}
	
	private final class ConsoleTableModel extends DefaultTableModel {
		
		private static final long serialVersionUID = 1L;
		
		private TableDisplay type;
		private int maxSize = 100;
		
		public ConsoleTableModel(final TableDisplay type) {
			setType(type);
		}
		
		public void setType(final TableDisplay type) {
			if (this.type != type) {
				if (type != null)
					super.setColumnIdentifiers(type.columnHeaders);
				this.type = type;
			}
		}
		
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
			final String uri = (String)event.getProperty(CommandEvent.PROP_COMMAND_LOCATION);
			String compId = (String)event.getProperty(CommandEvent.PROP_COMPONENT_ID);
			if (compId.endsWith(".in")) { //$NON-NLS-1$
				compId = compId.substring(0, compId.length() - ".in".length()); //$NON-NLS-1$
			} else if (compId.endsWith(".out")) { //$NON-NLS-1$
				compId = compId.substring(0, compId.length() - ".out".length()); //$NON-NLS-1$
			}
			final MWComponents mwc = MWComponents.valueOfID(compId);
			// System.out.println("mwc: " + ((mwc == null) ? compId : mwc.toString())); //$NON-NLS-1$
			row.add((mwc == null) ? compId : mwc.toString());
			row.add((cmd == null) ? Messages.getString("crawlingConsole.unknown") : cmd.getResult().name()); //$NON-NLS-1$
			switch (type) {
				case WORKING_ON:
					break;
				case REJECTED:
					final String lastFilter = lastFilters.get(cmd);
					row.add((lastFilter == null) ? Messages.getString("crawlingConsole.unknown") : lastFilter); //$NON-NLS-1$
					row.add((cmd == null) ? Messages.getString("crawlingConsole.unknown") : cmd.getResultText()); //$NON-NLS-1$
					break;
			}
			try {
				logger.debug("uri: " + ((enc.isSelected()) ? URLDecoder.decode(uri, Charset.defaultCharset().name()) : uri)); //$NON-NLS-1$
				row.add((enc.isSelected()) ? URLDecoder.decode(uri, Charset.defaultCharset().name()) : uri);
			} catch (UnsupportedEncodingException e) { /* cannot happen as we use the default charset here */ }
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (getRowCount() == maxSize)
						removeRow(0);
					addRow(row);
					final JViewport vp = scroll.getViewport();
					final Rectangle r = vp.getVisibleRect();
					r.setLocation(0, table.getHeight());
					vp.scrollRectToVisible(r);
					if (!clear.isEnabled())
						clear.setEnabled(true);
				}
			});
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
}
