
package org.paxle.desktop.impl.dialogues;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.paxle.core.queue.CommandEvent;
import org.paxle.core.queue.ICommand;
import org.paxle.core.queue.ICommandTracker;
import org.paxle.desktop.impl.Utilities;

public class CrawlingConsole extends JPanel implements EventHandler, ActionListener {
	
	private static final long serialVersionUID = 1L;
	
	private static final String AC_CLEAR = new String();
	private static final String AC_SAVE = new String();
	
	private final JScrollPane scroll = new JScrollPane();
	private final JTextPane text = new JTextPane();
	private final JButton clear = new JButton("Clear");
	private final JButton save = new JButton("Save text");
	private final ICommandTracker tracker;
	private final Object sync = new Object();
	
	public CrawlingConsole(final ICommandTracker tracker) {
		this.tracker = tracker;
		init();
	}
	
	public void actionPerformed(ActionEvent e) {
		final String ac = e.getActionCommand();
		if (ac == AC_CLEAR) {
			updateText(false, "");
			clear.setEnabled(false);
			save.setEnabled(false);
		} else if (ac == AC_SAVE) {
			new Thread() {
				@Override
				public void run() {
					final File file = Utilities.chooseFile(CrawlingConsole.this, "Save As", false, null, true);
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
			}.start();
		}
	}
	
	public void updateText(final boolean append, final String val) {
		synchronized (sync) {
			if (append) try {
				final Document doc = text.getDocument();
				text.getEditorKit().read(new StringReader(val), doc, doc.getLength());
			} catch (BadLocationException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} else {
				text.setText(val);
			}
		}
		text.invalidate();
		scroll.getViewport().setViewPosition(new Point(scroll.getViewport().getViewPosition().x, text.getHeight()));
		// scroll.revalidate();
		clear.setEnabled(true);
		save.setEnabled(true);
		// updateUI();
	}
	
	private void init() {
		text.setEditable(false);
		text.setAutoscrolls(true);
		scroll.setViewportView(text);
		
		final JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		clear.setActionCommand(AC_CLEAR);
		clear.addActionListener(this);
		bottom.add(clear);
		save.setActionCommand(AC_SAVE);
		save.addActionListener(this);
		bottom.add(save);
		
		super.setLayout(new BorderLayout(5, 5));
		super.add(scroll, BorderLayout.CENTER);
		super.add(bottom, BorderLayout.SOUTH);
	}
	
	private final Log logger = LogFactory.getLog(CrawlingConsole.class);
	
	public void handleEvent(Event event) {
		final Long id = (Long)event.getProperty(CommandEvent.PROP_COMMAND_ID);
		final ICommand cmd = tracker.getCommandByID(id);
		logger.debug("received event for command: " + cmd + " from component (ID): " + event.getProperty(CommandEvent.PROP_COMPONENT_ID));
		if (cmd != null)
			updateText(true, cmd.getLocation().toString() + "\n");
	}
}
