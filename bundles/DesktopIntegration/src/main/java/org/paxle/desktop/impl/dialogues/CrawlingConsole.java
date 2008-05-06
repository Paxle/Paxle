
package org.paxle.desktop.impl.dialogues;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Hashtable;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import org.paxle.core.queue.CommandEvent;
import org.paxle.core.queue.ICommand;
import org.paxle.core.queue.ICommandTracker;
import org.paxle.desktop.impl.DesktopServices;
import org.paxle.desktop.impl.Utilities;

public class CrawlingConsole extends DIServicePanel implements EventHandler, ActionListener {
	
	private static final long serialVersionUID = 1L;
	
	private static final String AC_CLEAR = new String();
	private static final String AC_SAVE = new String();
	
	private static class IntRingBuffer {
		
		private final int[] buf;
		private final int size;
		private int head;			// points to next free index
		private int tail;			// points to last full index
		private int num = 0;
		
		public IntRingBuffer(final int size) {
			this.size = size;
			buf = new int[size];
			tail = size - 1;
		}
		
		public IntRingBuffer copyToNew(final int size) {
			final IntRingBuffer rb = new IntRingBuffer(size);
			System.arraycopy(buf, 0, rb.buf, 0, Math.min(this.size, size));
			rb.head = head;
			rb.tail = tail;
			rb.num = num;
			return rb;
		}
		
		public int push(final int x) {
			final int r = (head == tail) ? pop() : -1;
			buf[head++] = x;
			if (head == size)
				head = 0;
			num++;
			return r;
		}
		
		public int pop() {
			final int r = buf[tail++];
			if (tail == size)
				tail = 0;
			num--;
			return r;
		}
		
		public int top() {
			return buf[tail];
		}
		
		public void clear() {
			head = tail + 1;
			if (head == size)
				head = 0;
			num = 0;
		}
		
		public boolean isEmpty() {
			return num == 0;
		}
	}
	
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
	
	private final Log           logger = LogFactory.getLog(CrawlingConsole.class);
	private final JScrollPane   scroll = new JScrollPane();
	private final JTextPane     text   = new JTextPane();
	private final JButton       clear  = Utilities.createButton("Clear", this, AC_CLEAR, null);
	private final JButton       save   = Utilities.createButton("Save ...", this, AC_SAVE, null);
	private final JRadioButton  enc    = new JRadioButton("URL-encoded");
	private final JRadioButton  normal = new JRadioButton("Original URLs");
	private final Object        sync   = new Object();
	private final IntRingBuffer buf    = new IntRingBuffer(100);
	private final ICommandTracker tracker;
	
	// TODO: save enc/normal
	
	@SuppressWarnings("unchecked")
	public CrawlingConsole(final DesktopServices services) {
		super(services);
		this.tracker = services.getServiceManager().getService(ICommandTracker.class);
		init();
		final Hashtable<String,Object> properties = new Hashtable<String,Object>();
		properties.put(EventConstants.EVENT_TOPIC, new String[] { CommandEvent.TOPIC_DEQUEUED });
		properties.put(EventConstants.EVENT_FILTER, String.format("(%s=org.paxle.crawler.in)", CommandEvent.PROP_COMPONENT_ID));
		super.registerService(this, this, properties, EventHandler.class);
	}
	
	public void actionPerformed(ActionEvent e) {
		final String ac = e.getActionCommand();
		if (ac == AC_CLEAR) {
			updateText(false, "");
			clear.setEnabled(false);
			save.setEnabled(false);
		} else if (ac == AC_SAVE) {
			new Thread(new SaveActionRunnable()).start();
		}
	}
	
	public void updateText(final boolean appendLine, final String val) {
		int j = 0;
		int i = -1;
		int top = 0;
		synchronized (sync) {
			if (!appendLine)
				buf.clear();
			while ((i = val.indexOf('\n', j)) != -1) {
				top += buf.push(i - j + 1);
				j = i + 1;
			}
			
			try {
				final Document doc = text.getDocument();
				
				if (appendLine) {
					top += buf.push(val.length() - j + 1);
					text.getEditorKit().read(new StringReader(val + "\n"), doc, doc.getLength());
				} else {
					text.setText(val);
				}
			} catch (BadLocationException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (!clear.isEnabled()) {
			clear.setEnabled(true);
			save.setEnabled(true);
		}
	}
	
	private void init() {
		text.setEditable(false);
		scroll.getViewport().setView(text);
		
		enc.setSelected(true);
		final ButtonGroup bg = new ButtonGroup();
		bg.add(enc);
		bg.add(normal);
		final JPanel bottomRight = new JPanel(new FlowLayout(FlowLayout.LEFT));
		bottomRight.add(normal);
		bottomRight.add(enc);
		
		final JPanel bottomLeft = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		bottomLeft.add(save);
		bottomLeft.add(clear);
		
		final JPanel bottom = new JPanel(new BorderLayout());
		bottom.add(bottomRight, BorderLayout.WEST);
		bottom.add(bottomLeft, BorderLayout.EAST);
		
		super.setLayout(new BorderLayout());
		super.add(scroll, BorderLayout.CENTER);
		super.add(bottom, BorderLayout.SOUTH);
	}
	
	public void handleEvent(Event event) {
		final Long id = (Long)event.getProperty(CommandEvent.PROP_COMMAND_ID);
		final ICommand cmd = tracker.getCommandByID(id);
		logger.debug("received event for command: " + cmd + " from component (ID): " + event.getProperty(CommandEvent.PROP_COMPONENT_ID));
		if (cmd != null) try {
			final String uri = (enc.isSelected())
					? URLDecoder.decode(cmd.getLocation().toString(), Charset.defaultCharset().name())
					: cmd.getLocation().toASCIIString();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					updateText(true, uri);
				}
			});
		} catch (UnsupportedEncodingException e) { /* cannot happen as we use the default charset here */ }
	}
}
