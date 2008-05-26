
package org.paxle.desktop.impl.event;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MultipleChangesListener implements ChangeListener, ActionListener, ListSelectionListener, OptionChangeListener, DocumentListener {
	
	private static final long serialVersionUID = 1L;
	
	private static final Class<?>[] CLASSES;
	private static final String[] GET_VALUES;
	private static final Class<?>[] ADD_LISTENERS;
	
	static {
		/* This method to intialize the arrays is not the most optimized one, but it
		 * makes it easy to maintain the data as well as the data-structure and does
		 * not consume additional permanent memory like a Collection or storing Entry-
		 * objects would */
		
		final class Entry {
			final Class<?> clazz;
			final String getValue;
			final Class<?> listenerClass;
			
			Entry(final Class<?> clazz, final String getValue, final Class<?> listenerClass) {
				this.clazz = clazz;
				this.getValue = getValue;
				this.listenerClass = listenerClass;
			}
		}
		
		final Entry[] entries = new Entry[] {
				new Entry(MultipleChangesListener.class, 	"isChanged", 			OptionChangeListener.class),
				new Entry(JSpinner.class, 					"getValue", 			ChangeListener.class),
				new Entry(AbstractButton.class, 			"isSelected", 			ChangeListener.class),
				new Entry(JComboBox.class, 					"getSelectedItem", 		ActionListener.class),
				new Entry(JList.class, 						"getSelectedValues", 	ListSelectionListener.class)
		};
		
		CLASSES = new Class[entries.length];
		GET_VALUES = new String[entries.length];
		ADD_LISTENERS = new Class[entries.length];
		for (int i=0; i<entries.length; i++) {
			final Entry e = entries[i];
			CLASSES[i] = e.clazz;
			GET_VALUES[i] = e.getValue;
			ADD_LISTENERS[i] = e.listenerClass;
		}
	}
	
	private static class CompEntry {
		
		private static int NUM_ENTRIES = 0;
		
		Object initialValue;
		final int num;
		
		CompEntry(final Object initialValue) {
			this.initialValue = initialValue;
			this.num = NUM_ENTRIES++;
		}
	}
	
	private final Log logger = LogFactory.getLog(MultipleChangesListener.class);
	
	private final HashMap<Object,CompEntry> initialValues = new HashMap<Object,CompEntry>();
	private final EventListenerList eventListeners = new EventListenerList();
	private final BitSet changedSet = new BitSet();
	
	private final JButton save = new JButton();
	private final JButton reset = new JButton();
	private boolean changed = false;
	
	public MultipleChangesListener() {
		this(null, null, null);
	}
	
	public MultipleChangesListener(
			final ActionListener actionListener,
			final String saveCommand,
			final String resetCommand) {
		if (actionListener != null) {
			save.addActionListener(actionListener);
			reset.addActionListener(actionListener);
		}
		if (saveCommand != null)
			save.setActionCommand(saveCommand);
		if (resetCommand != null)
			reset.setActionCommand(resetCommand);
		init();
	}
	
	private void init() {
		save.setEnabled(false);
		reset.setEnabled(false);
	}
	
	public JPanel layoutDefault(final boolean buttonsRight) {
		final JPanel buttonPanel = new JPanel(new FlowLayout((buttonsRight) ? FlowLayout.RIGHT : FlowLayout.LEFT, 5, 5));
		buttonPanel.add(reset);
		buttonPanel.add(save);
		
		final JPanel panel = new JPanel(new BorderLayout());
		panel.add(buttonPanel, BorderLayout.SOUTH);
		return panel;
	}
	
	public JButton getSaveButton() {
		return save;
	}
	
	public JButton getResetButton() {
		return reset;
	}
	
	public boolean isChanged() {
		return changed;
	}
	
	public void addOptionChangeListener(final OptionChangeListener l) {
		if (l == this)
			throw new IllegalArgumentException("argument == this, will result in endless loop");
		eventListeners.add(OptionChangeListener.class, l);
	}
	
	public void removeOptionChangeListener(final OptionChangeListener l) {
		if (l == this)
			throw new IllegalArgumentException("l == this");
		eventListeners.remove(OptionChangeListener.class, l);
	}
	
	public void addComp2Monitor(final Object comp) {
		if (comp instanceof JTextComponent) {
			final JTextComponent tc = (JTextComponent)comp;
			final Document doc = tc.getDocument();
			initialValues.put(doc, new CompEntry(tc.getText()));
			doc.addDocumentListener(this);
		} else {
			final Class<?> clazz = comp.getClass();
			final int eidx = getEntryIndex(clazz);
			if (eidx < 0)
				throw new RuntimeException("component '" + clazz.getName() + "' not supported for monitoring");
			try {
				// save the initial value of the component for comparison purposes on state change
				initialValues.put(comp, new CompEntry(clazz.getMethod(GET_VALUES[eidx]).invoke(comp)));
				
				// register this as event listener for the component
				final Method method = clazz.getMethod("add" + ADD_LISTENERS[eidx].getSimpleName(), ADD_LISTENERS[eidx]);
				method.invoke(comp, this);
				logger.debug("added " + ADD_LISTENERS[eidx].getSimpleName() + " for " + comp);
			} catch (Exception e) { e.printStackTrace(); }
		}
	}
	
	private static class ChangedCompEntry implements Map.Entry<Object,Object> {
		
		private final Map.Entry<Object,CompEntry> e;
		
		public ChangedCompEntry(final Map.Entry<Object,CompEntry> e) {
			this.e = e;
		}
		
		public Object getKey() {
			return e.getKey();
		}
		
		public Object getValue() {
			return e.getValue().initialValue;
		}
		
		public Object setValue(Object value) {
			final Object r = e.getValue().initialValue;
			e.getValue().initialValue = value;
			return r;
		}
	}
	
	public Iterator<Map.Entry<Object,Object>> iterator(final boolean changedComps) {
		return new Iterator<Map.Entry<Object,Object>>() {
			
			final Iterator<Map.Entry<Object,CompEntry>> it = initialValues.entrySet().iterator();
			
			Map.Entry<Object,Object> cur = null;
			Map.Entry<Object,Object> next = next0();
			
			private Map.Entry<Object,Object> next0() {
				while (it.hasNext()) {
					final Map.Entry<Object,CompEntry> e = it.next();
					if (!(changedSet.get(e.getValue().num) ^ changedComps))
						return new ChangedCompEntry(e);
				}
				return null;
			}
			
			public boolean hasNext() {
				return next != null;
			}
			
			public Map.Entry<Object,Object> next() {
				cur = next;
				next = next0();
				return cur;
			}
			
			public void remove() {
				removeComp2Monitor(cur);
			}
		};
	}
	
	public void removeComp2Monitor(final Object comp) {
		final CompEntry entry = initialValues.remove(comp);
		if (entry != null)
			changedSet.clear(entry.num);
		update("removed comp");
	}
	
	public void clearDefaults() {
		for (final CompEntry e : initialValues.values())
			e.initialValue = null;
		changedSet.clear();
		update("cleared values");
	}
	
	public void resetDefaults() {
		for (final Object comp : initialValues.keySet())
			setState(comp, -1L, true);
		update("resetted values");
	}
	
	public void setState(final Object comp, final long when) {
		setState(comp, when, false);
	}
	
	private int getEntryIndex(final Class<?> clazz) {
		for (int i=0; i<CLASSES.length; i++)
			if (CLASSES[i].isAssignableFrom(clazz))
				return i;
		return -1;
	}
	
	private void setState(final Object comp, final long when, final boolean init) {
		if (comp instanceof Document) {
			final Document doc = (Document)comp;
			try {
				setState(doc, doc.getText(0, doc.getLength()), when, init);
			} catch (BadLocationException e) { e.printStackTrace(); }
		} else {
			final Class<?> clazz = comp.getClass();
			final int eidx = getEntryIndex(clazz);
			if (eidx < 0)
				throw new RuntimeException("component '" + comp.getClass().getName() + "' not supported for monitoring");
			try {
				setState(comp, clazz.getMethod(GET_VALUES[eidx]).invoke(comp), when, init);
			} catch (Exception e) { e.printStackTrace(); }
		}
	}
	
	private void setState(final Object comp, final Object newValue, final long when, final boolean init) {
		final CompEntry entry = initialValues.get(comp);
		final boolean entryChanged;
		if (entry.initialValue == null || init) {
			entry.initialValue = newValue;
			entryChanged = false;
		} else {
			final Object origValue = entry.initialValue;
			entryChanged = !((origValue.getClass().isArray())
					? Arrays.equals((Object[])origValue, (Object[])newValue)
					: origValue.equals(newValue));
		}
		
		if (logger.isDebugEnabled()) {
			final StringWriter sw = new StringWriter();
			new Exception("Stack trace").printStackTrace(new PrintWriter(sw));
			logger.debug(sw.getBuffer().toString());
			logger.debug("init: " + init + ", changedEntry: " + entryChanged + ", org: " +
					entry.initialValue.getClass() + " (" + entry.initialValue + "), new: " +
					newValue.getClass() + " (" + newValue + ")");
		}
		
		changedSet.set(entry.num, entryChanged);
		if (!init && when > 0L)
			fireOptionChanged(new OptionChangedEvent(comp, newValue, when, entryChanged));
		update(comp);
	}
	
	private void fireOptionChanged(final OptionChangedEvent e) {
		for (final OptionChangeListener l : eventListeners.getListeners(OptionChangeListener.class))
			l.optionChanged(e);
	}
	
	private void fireOptionStateChanged(final Object reason) {
		final OptionStateChangedEvent e = new OptionStateChangedEvent(this, reason, System.currentTimeMillis(), changed);
		for (final OptionChangeListener l : eventListeners.getListeners(OptionChangeListener.class))
			l.optionStateChanged(e);
	}
	
	private void update(final Object why) {
		final boolean before = changed;
		changed = changedSet.cardinality() != 0;
		if (before != changed)
			fireOptionStateChanged(why);
		save.setEnabled(changed);
		reset.setEnabled(changed);
	}
	
	/* ========================================================================== *
	 * Listener methods
	 * ========================================================================== */
	
	public void changedUpdate(DocumentEvent e) {
		// ignore
	}
	
	public void insertUpdate(DocumentEvent e) {
		setState(e.getDocument(), System.currentTimeMillis());
	}
	
	public void removeUpdate(DocumentEvent e) {
		setState(e.getDocument(), System.currentTimeMillis());
	}
	
	public void optionStateChanged(OptionStateChangedEvent e) {
		setState(e.getSource(), e.getWhen());
	}
	
	public void optionChanged(OptionChangedEvent e) {
		// ignore
	}
	
	public void stateChanged(ChangeEvent e) {
		setState(e.getSource(), System.currentTimeMillis());
	}
	
	public void actionPerformed(ActionEvent e) {
		setState(e.getSource(), e.getWhen());
	}
	
	public void valueChanged(ListSelectionEvent e) {
		setState(e.getSource(), System.currentTimeMillis());
	}
}
