
package org.paxle.desktop.impl.dialogues;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
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
import javax.swing.JTextPane;
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
import org.paxle.desktop.impl.DesktopServices.MWComponents;

public class CrawlingConsole extends DIServicePanel implements EventHandler, ActionListener {
	
	private static final long serialVersionUID = 1L;
	
	private class SaveActionRunnable implements Runnable {
		public void run() {
			final File file = Utilities.chooseSingleFile(CrawlingConsole.this, "Save As", false, null, true);
			if (file == null)
				return;
			FileWriter fw = null;
			try {
				fw = new FileWriter(file);
				synchronized (sync) {
					text.write(fw);
				}
			} catch (IOException e) {
				logger.error("I/O-exception storing text contents", e);
				Utilities.showExceptionBox("I/O-exception storing text contents", e);
			} finally { if (fw != null) try { fw.close(); } catch (IOException e) { /* ignore */ } }
		}
	}
	
	private static final Dimension DIM_CCONSOLE = new Dimension(500, 400);
	
	private static final String PROP_DISPLAYED_MWCOMP = "displayedMWComp";
	private static final String PROP_URLS_ENCODED = "urlsEncoded";
	private static final String PROP_TABLE_DISPLAY = "tableDisplay";
	private static final String PROP_SHOW_ENQUEUED = "showEnqueued";
	private static final String PROP_SHOW_DESTROYED = "showDestroyed";
	
	private static final String AC_CLEAR = new String();
	private static final String AC_SAVE = new String();
	private static final String AC_CRAWL = new String();
	private static final String AC_SELECT = new String();
	private static final String AC_ENQUEUED = new String();
	private static final String AC_DESTROYED = new String();
	
	private static final String LBL_PAUSE_ = "Pause %s";
	private static final String LBL_RESUME_ = "Resume %s";
	
	private static enum TableDisplay {
		WORKING_ON("Comp", "Result", "URI"),
		REJECTED("Comp", "Result", "Filter", "Reason", "URI");
		
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
	private final JTextPane     text   = new JTextPane();
	private final JButton       clear  = Utilities.createButton("Clear", this, AC_CLEAR, null);
	private final JButton       save   = Utilities.createButton("Save ...", this, AC_SAVE, null);
	private final JRadioButton  enc    = new JRadioButton("URL-encoded");
	private final JRadioButton  normal = new JRadioButton("Original URLs");
	private final JCheckBox     cbEnq  = Utilities.createCheckBox("Enqueued", this, AC_ENQUEUED, null);
	private final JCheckBox     cbDstr = Utilities.createCheckBox("Rejected", this, AC_DESTROYED, null);
	private final Object        sync   = new Object();
	// private final IntRingBuffer buf    = new IntRingBuffer(100);
	private final JToggleButton cpb    = Utilities.createToggleButton(null, this, AC_CRAWL, null);
	private final JTable        table;
	private final ConsoleTableModel model;
	private final JComboBox     cbox;
	private final ICommandTracker tracker;
	
	private MWComponents currentComp;
	private boolean currentEnq, currentDstr;
	
	public CrawlingConsole(final DesktopServices services) {
		super(services);
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
		System.out.println("closed CrawlingConsole: " + new Exception().getStackTrace()[1].toString());
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
		}
		if (enqUpdate) {
			if (currentEnq)
				super.unregisterService(Events.IN_QUEUE);
			if (enqueued)
				registerEventListener(Events.IN_QUEUE,
						CommandEvent.PROP_COMPONENT_ID, id + ".in",
						CommandEvent.TOPIC_DEQUEUED);
			currentEnq = enqueued;
		}
		if (dstrUpdate) {
			if (currentDstr)
				super.unregisterService(Events.OUT_QUEUE);
			if (destroyed)
				registerEventListener(Events.OUT_QUEUE,
						CommandEvent.PROP_COMPONENT_ID, id + ".out",
						CommandFilterEvent.TOPIC_POST_FILTER, CommandEvent.TOPIC_DESTROYED);
			currentDstr = destroyed;
		}
	}

	@SuppressWarnings("unchecked")
	private void registerEventListener(final Events key, final String filterKey, final String filterVal, final String... topics) {
		final Hashtable<String,Object> props = new Hashtable<String,Object>();
		if (topics.length > 0)
			props.put(EventConstants.EVENT_TOPIC, topics);
		props.put(EventConstants.EVENT_FILTER, String.format("(%s=%s)", filterKey, filterVal));
		super.registerService(key, this, props, EventHandler.class);
	}
	
	public Container getContainer() {
		return this;
	}
	
	@Override
	public String getTitle() {
		return "Crawling Console";
	}
	
	@Override
	public Dimension getWindowSize() {
		return DIM_CCONSOLE;
	}
	
	private void updateCpb(final boolean paused, final boolean getState, final boolean setState) {
		if (getState && setState)
			throw new IllegalArgumentException("cannot set and get state at the same time");
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
		} else if (ac == AC_SAVE) {
			new Thread(new SaveActionRunnable()).start();
		} else if (ac == AC_CRAWL) {
			updateCpb(cpb.isSelected(), false, true);
		} else if (ac == AC_SELECT) {
			updateCpb(false, true, false);
		}
		if (ac == AC_SELECT || ac == AC_ENQUEUED || ac == AC_DESTROYED) {
			updateListeners(
					MWComponents.valueOfHumanReadable((String)cbox.getSelectedItem()),
					cbEnq.isSelected(), cbDstr.isSelected());
			
			model.setType((cbDstr.isSelected()) ? TableDisplay.REJECTED : (cbEnq.isSelected()) ? TableDisplay.WORKING_ON : null);
		}
	}
	
	private void clear() {
		model.clear();
		clear.setEnabled(false);
		save.setEnabled(false);
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
		bRight.add(save);
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
			if (compId.endsWith(".in")) {
				compId = compId.substring(0, compId.length() - ".in".length());
			} else if (compId.endsWith(".out")) {
				compId = compId.substring(0, compId.length() - ".out".length());
			}
			final MWComponents mwc = MWComponents.valueOfID(compId);
			System.out.println("mwc: " + ((mwc == null) ? compId : mwc.toString()));
			row.add((mwc == null) ? compId : mwc.toString());
			row.add((cmd == null) ? "unknown" : cmd.getResult().name());
			switch (type) {
				case WORKING_ON:
					break;
				case REJECTED:
					final String lastFilter = lastFilters.get(cmd);
					row.add((lastFilter == null) ? "unknown" : lastFilter);
					row.add((cmd == null) ? "unknown" : cmd.getResultText());
					break;
			}
			try {
				System.out.println("uri: " + ((enc.isSelected()) ? URLDecoder.decode(uri, Charset.defaultCharset().name()) : uri));
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
					if (!clear.isEnabled()) {
						clear.setEnabled(true);
						save.setEnabled(true);
					}
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
	
	public void handleEvent(Event event) {
		final String topic = event.getTopic();
		if (topic.startsWith(CommandEvent.TOPIC_)) {
			handleCommandEvent(event);
		} else if (topic.startsWith(MWComponentEvent.TOPIC_)) {
			handleMWComponentEvent(event);
		}
	}
}
