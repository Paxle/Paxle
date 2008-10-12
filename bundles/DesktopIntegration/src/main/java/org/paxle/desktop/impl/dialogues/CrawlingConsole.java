
package org.paxle.desktop.impl.dialogues;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import java.util.WeakHashMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
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
import javax.swing.table.TableColumnModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import org.paxle.core.IMWComponent;
import org.paxle.core.MWComponentEvent;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.doc.IParserDocument;
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
	
	private static interface TableColumnSpecs {
		public Object[] getColumnHeaders();
		public String name();
		public void insertValues(final Vector<String> row, final Event event, final ICommand cmd, final String lastFilter);
	}
	
	private static enum TableDisplay implements TableColumnSpecs {
		WORKING_ON(
				Messages.getString("crawlingConsole.tbl1.comp"), //$NON-NLS-1$
				Messages.getString("crawlingConsole.tbl1.depth"), //$NON-NLS-1$
				Messages.getString("crawlingConsole.tbl1.result"), //$NON-NLS-2$
				Messages.getString("crawlingConsole.tbl1.uri")), //$NON-NLS-3$
		REJECTED(
				Messages.getString("crawlingConsole.tbl2.comp"), //$NON-NLS-1$
				Messages.getString("crawlingConsole.tbl2.depth"), //$NON-NLS-1$
				Messages.getString("crawlingConsole.tbl2.result"), //$NON-NLS-2$
				Messages.getString("crawlingConsole.tbl2.filter"), //$NON-NLS-3$
				Messages.getString("crawlingConsole.tbl2.reason"), //$NON-NLS-4$
				Messages.getString("crawlingConsole.tbl2.uri")); //$NON-NLS-5$
		
		private final Object[] columnHeaders;
		
		private TableDisplay(final Object... columnHeaders) {
			this.columnHeaders = columnHeaders;
		}
		
		public Object[] getColumnHeaders() {
			return columnHeaders;
		}
		
		public void insertValues(Vector<String> row, Event event, ICommand cmd, final String lastFilter) {
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
			row.add((cmd == null) ? "-" : Integer.toString(cmd.getDepth()));
			row.add((cmd == null) ? Messages.getString("crawlingConsole.unknown") : cmd.getResult().name()); //$NON-NLS-1$
			switch (this) {
				case WORKING_ON:
					break;
				case REJECTED:
					row.add((lastFilter == null) ? Messages.getString("crawlingConsole.unknown") : lastFilter); //$NON-NLS-1$
					row.add((cmd == null) ? Messages.getString("crawlingConsole.unknown") : cmd.getResultText()); //$NON-NLS-1$
					break;
			}
			try {
				row.add(URLDecoder.decode(uri, Charset.defaultCharset().name()));
			} catch (UnsupportedEncodingException e) { /* cannot happen as we use the default charset here */ }
		}
	}
	
	private static enum Events {
		IN_QUEUE, OUT_QUEUE, MWCOMP_STATE
	}
	
	private final WeakHashMap<ICommand,String> lastFilters = new WeakHashMap<ICommand,String>();
	
	private final Log           logger = LogFactory.getLog(CrawlingConsole.class);
	private final JScrollPane   scroll = new JScrollPane();
	private final JButton       clear  = Utilities.setButtonProps(new JButton(), Messages.getString("crawlingConsole.clear"), this, AC_CLEAR, -1, null); //$NON-NLS-1$
	private final JCheckBox     cbEnq  = Utilities.setButtonProps(new JCheckBox(), Messages.getString("crawlingConsole.enqueued"), this, AC_ENQUEUED, -1, null); //$NON-NLS-1$
	private final JCheckBox     cbDstr = Utilities.setButtonProps(new JCheckBox(), Messages.getString("crawlingConsole.rejected"), this, AC_DESTROYED, -1, null); //$NON-NLS-1$
	
	private final JToggleButton cpb    = Utilities.setButtonProps(new JToggleButton(), null, this, AC_CRAWL, -1, null);
	private final JTable        table;
	private final ConsoleTableModel model;
	private final JComboBox     cbox;
	private final ICommandTracker tracker;
	private final JPanel        options = new JPanel();
	
	private MWComponents currentComp;
	private boolean currentEnq, currentDstr;
	
	public CrawlingConsole(final DesktopServices services) {
		super(services, DIM_CCONSOLE);
		
		cbox = new JComboBox(DesktopServices.MWComponents.humanReadableNames());
		cbox.addActionListener(this);
		tracker = services.getServiceManager().getService(ICommandTracker.class);
		
		final Properties props = services.getServiceManager().getServiceProperties();
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
		init(DesktopServices.MWComponents.valueOf(mwcomp),
				Boolean.parseBoolean(showEnqueued),
				Boolean.parseBoolean(showDestroyed));
	}
	
	private int[] readColumnWidths(final TableColumnSpecs specs) {
		final Properties props = services.getServiceManager().getServiceProperties();
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
		final Properties props = services.getServiceManager().getServiceProperties();
		props.put(PROP_COL_WIDTHS + specs.name(), sb.toString());
	}
	
	@Override
	public void close() {
		final Properties props = super.services.getServiceManager().getServiceProperties();
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
		} else if (ac == AC_SETTINGS) {
			final boolean sel = ((JToggleButton)e.getSource()).isSelected();
			if (sel && options.getComponentCount() == 0)
				initOptions();
			options.setVisible(sel);
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
			final DesktopServices.MWComponents comp,
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
		bbRight.add(Utilities.setButtonProps(new JToggleButton(), Messages.getString("crawlingConsole.settings"), this, AC_SETTINGS, KeyEvent.VK_S, null));
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
			options.add(Utilities.setButtonProps(cbox, col.getL10n(), new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (cbox.isSelected()) {
						model.setCols(model.type.showCmd | mask);
					} else {
						model.setCols(model.type.showCmd & ~mask);
					}
				}
			}, null, -1, null), gbc);
			if (gbc.gridy < 5) {
				gbc.gridy++;
			} else {
				gbc.gridx++;
				gbc.gridy = 0;
			}
		}
	}
	
	private static enum Columns {
		// ICommand specific
		COMPONENT, DEPTH, LOCATION, RESULT, RESULTTEXT, LAST_FILTER,
		// ICrawlerDocument specific
		C_CHARSET, C_DATE, C_LANGS, C_LASTMOD, C_MD5, C_MIMETYPE, C_SIZE, C_STATUS, C_STATUSTEXT,
		// IParserDocument specific
		P_AUTHOR, P_CHARSET, P_HEADLINES, P_IMAGES, P_KEYWORDS, P_LANGUAGES, P_LASTMOD, P_LINKS,
		P_MIMETYPE, P_STATUS, P_STATUSTEXT, P_SUBDOCS, P_SUMMARY, P_TITLE;
		
		public String getL10n() {
			return Messages.getString("crawlingConsole.col." + name().toLowerCase());
		}
	}
	
	private static final char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	
	private static final class TableColumns implements TableColumnSpecs {
		
		private static final int DEFAULT = (
				(1 << Columns.COMPONENT.ordinal()) |
				(1 << Columns.LOCATION.ordinal()) |
				(1 << Columns.DEPTH.ordinal()));
		
		private final String unknown = Messages.getString("crawlingConsole.unknown");
		
		private int showCmd;
		
		public TableColumns() {
			this(DEFAULT);
		}
		
		public TableColumns(final int showCmd) {
			this.showCmd = showCmd;
		}
		
		public Object[] getColumnHeaders() {
			final Object[] r = new Object[Integer.bitCount(showCmd)];
			int i = 0;
			for (final Columns col : Columns.values())
				if ((showCmd & (1 << col.ordinal())) != 0)
					r[i++] = col.getL10n();
			return r;
		}
		
		public String name() {
			return Integer.toString(showCmd);
		}
		
		private String fromCollection(final Object[] o) {
			return (o == null) ? "null" : fromCollection(Arrays.asList(o));
		}
		
		private String fromCollection(final Map<?,?> m) {
			// TODO
			return (m == null) ? "null" : m.toString();
		}
		
		private String fromCollection(final Collection<?> c) {
			// TODO
			return (c == null) ? "null" : c.toString();
		}
		
		public void insertValues(final Vector<String> row, final Event event, final ICommand cmd, final String lastFilter) {
			
			for (final Columns col : Columns.values()) try {
				if ((showCmd & (1 << col.ordinal())) == 0)
					continue;
				
				switch (col) {
					/* ICommand specific */
					case COMPONENT: {
						String compId = (String)event.getProperty(CommandEvent.PROP_COMPONENT_ID);
						if (compId.endsWith(".in")) { //$NON-NLS-1$
							compId = compId.substring(0, compId.length() - ".in".length()); //$NON-NLS-1$
						} else if (compId.endsWith(".out")) { //$NON-NLS-1$
							compId = compId.substring(0, compId.length() - ".out".length()); //$NON-NLS-1$
						}
						final MWComponents mwc = MWComponents.valueOfID(compId);
						row.add((mwc == null) ? compId : mwc.toString());
					} break;
					
					case DEPTH: {
						row.add((cmd == null) ? "-" : Integer.toString(cmd.getDepth()));
					} break;
					
					case LOCATION: {
						final String uri = (String)event.getProperty(CommandEvent.PROP_COMMAND_LOCATION);
						row.add(URLDecoder.decode(uri, Charset.defaultCharset().name()));
					} break;
					
					case RESULT: {
						row.add((cmd == null) ? unknown : cmd.getResult().toString());
					} break;
					
					case RESULTTEXT: {
						row.add((cmd == null) ? unknown : cmd.getResultText());
					} break;
					
					case LAST_FILTER: {
						row.add((lastFilter == null) ? unknown : lastFilter);
					} break;
					
					/* ICrawlerDocument specific */
					case C_CHARSET: {
						final ICrawlerDocument cdoc = (cmd == null) ? null : cmd.getCrawlerDocument();
						row.add((cdoc == null) ? unknown : cdoc.getCharset());
					} break;
					
					case C_DATE: {
						final ICrawlerDocument cdoc = (cmd == null) ? null : cmd.getCrawlerDocument();
						row.add((cdoc == null) ? unknown : DateFormat.getDateTimeInstance().format(cdoc.getCrawlerDate()));
					} break;
					
					case C_LANGS: {
						final ICrawlerDocument cdoc = (cmd == null) ? null : cmd.getCrawlerDocument();
						row.add((cdoc == null) ? unknown : fromCollection(cdoc.getLanguages()));
					} break;
					
					case C_LASTMOD: {
						final ICrawlerDocument cdoc = (cmd == null) ? null : cmd.getCrawlerDocument();
						row.add((cdoc == null) ? unknown : DateFormat.getDateTimeInstance().format(cdoc.getLastModDate()));
					} break;
					
					case C_MD5: {
						final ICrawlerDocument cdoc = (cmd == null) ? null : cmd.getCrawlerDocument();
						if (cdoc == null) {
							row.add(unknown);
						} else {
							final byte[] md5 = cdoc.getMD5Sum();
							if (md5 == null) {
								row.add(unknown);
							} else {
								final StringBuilder sb = new StringBuilder(md5.length * 2);
								for (int i=0; i<md5.length; i++)
									sb.append(HEX[(md5[i] & 0xF0) >>> 8]).append(HEX[md5[i] & 0x0F]);
								row.add(sb.toString());
							}
						}
					} break;
					
					case C_MIMETYPE: {
						final ICrawlerDocument cdoc = (cmd == null) ? null : cmd.getCrawlerDocument();
						row.add((cdoc == null) ? unknown : cdoc.getMimeType());
					} break;
					
					case C_SIZE: {
						final ICrawlerDocument cdoc = (cmd == null) ? null : cmd.getCrawlerDocument();
						row.add((cdoc == null) ? unknown : Long.toString(cdoc.getSize()));
					} break;
					
					case C_STATUS: {
						final ICrawlerDocument cdoc = (cmd == null) ? null : cmd.getCrawlerDocument();
						row.add((cdoc == null) ? unknown : cdoc.getStatus().toString());
					} break;
					
					case C_STATUSTEXT: {
						final ICrawlerDocument cdoc = (cmd == null) ? null : cmd.getCrawlerDocument();
						row.add((cdoc == null) ? unknown : cdoc.getStatusText());
					} break;
					
					/* IParserDocument specific */
					case P_AUTHOR: {
						final IParserDocument pdoc = (cmd == null) ? null : cmd.getParserDocument();
						row.add((pdoc == null) ? unknown : pdoc.getAuthor());
					} break;
					
					case P_CHARSET: {
						final IParserDocument pdoc = (cmd == null) ? null : cmd.getParserDocument();
						row.add((pdoc == null) ? unknown : pdoc.getCharset().name());
					} break;
					
					case P_HEADLINES: {
						final IParserDocument pdoc = (cmd == null) ? null : cmd.getParserDocument();
						row.add((pdoc == null) ? unknown : fromCollection(pdoc.getHeadlines()));
					} break;
					
					case P_IMAGES: {
						final IParserDocument pdoc = (cmd == null) ? null : cmd.getParserDocument();
						row.add((pdoc == null) ? unknown : fromCollection(pdoc.getImages()));
					} break;
					
					case P_KEYWORDS: {
						final IParserDocument pdoc = (cmd == null) ? null : cmd.getParserDocument();
						row.add((pdoc == null) ? unknown : fromCollection(pdoc.getKeywords()));
					} break;
					
					case P_LANGUAGES: {
						final IParserDocument pdoc = (cmd == null) ? null : cmd.getParserDocument();
						row.add((pdoc == null) ? unknown : fromCollection(pdoc.getLanguages()));
					} break;
					
					case P_LASTMOD: {
						final IParserDocument pdoc = (cmd == null) ? null : cmd.getParserDocument();
						row.add((pdoc == null) ? unknown : DateFormat.getDateTimeInstance().format(pdoc.getLastChanged()));
					} break;
					
					case P_LINKS: {
						final IParserDocument pdoc = (cmd == null) ? null : cmd.getParserDocument();
						row.add((pdoc == null) ? unknown : fromCollection(pdoc.getLinks()));
					} break;
					
					case P_MIMETYPE: {
						final IParserDocument pdoc = (cmd == null) ? null : cmd.getParserDocument();
						row.add((pdoc == null) ? unknown : pdoc.getMimeType());
					} break;
					
					case P_STATUS: {
						final IParserDocument pdoc = (cmd == null) ? null : cmd.getParserDocument();
						row.add((pdoc == null) ? unknown : pdoc.getStatus().toString());
					} break;
					
					case P_STATUSTEXT: {
						final IParserDocument pdoc = (cmd == null) ? null : cmd.getParserDocument();
						row.add((pdoc == null) ? unknown : pdoc.getStatusText());
					} break;
					
					case P_SUBDOCS: {
						final IParserDocument pdoc = (cmd == null) ? null : cmd.getParserDocument();
						if (pdoc == null) {
							row.add(unknown);
						} else {
							// TODO
						}
					} break;
					
					case P_SUMMARY: {
						final IParserDocument pdoc = (cmd == null) ? null : cmd.getParserDocument();
						row.add((pdoc == null) ? unknown : pdoc.getSummary());
					} break;
					
					case P_TITLE: {
						final IParserDocument pdoc = (cmd == null) ? null : cmd.getParserDocument();
						row.add((pdoc == null) ? unknown : pdoc.getTitle());
					} break;
				}
			} catch (UnsupportedEncodingException e) { /* ignore, we are using the default charset here */
			} catch (Throwable e) { e.printStackTrace(); }
			
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
