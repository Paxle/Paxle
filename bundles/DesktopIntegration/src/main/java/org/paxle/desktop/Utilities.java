
package org.paxle.desktop;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EventListener;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

public class Utilities {
	
	public static final Point LOCATION_CENTER = new Point();
	public static final Point LOCATION_BY_PLATFORM = new Point();
	private static final String KE_CLOSE = new String();
	
	public static JToggleButton createToggleButton(final String text, final ActionListener al, final String actionCommand, final Icon icon) {
		return setButtonProps(new JToggleButton(), text, al, actionCommand, icon);
	}
	
	public static JButton createButton(final String text, final ActionListener al, final String actionCommand, final Icon icon) {
		return setButtonProps(new JButton(), text, al, actionCommand, icon);
	}
	
	public static JCheckBox createCheckBox(final String text, final ActionListener al, final String actionCommand, final Icon icon) {
		return setButtonProps(new JCheckBox(), text, al, actionCommand, icon);
	}
	
	private static <E extends AbstractButton> E setButtonProps(
			final E b,
			final String text,
			final ActionListener al,
			final String actionCommand,
			final Icon icon) {
		b.setText(text);
		b.setActionCommand(actionCommand);
		if (al != null)
			b.addActionListener(al);
		if (icon != null)
			b.setIcon(icon);
		return b;
	}
	
	public static File chooseSingleFile(
			final Component parent,
			final String title,
			final boolean load,
			final FileFilter fnef,
			final boolean tryAgain) {
		final JFileChooser fc = new JFileChooser();
		fc.setDialogTitle(title);
		if (fnef != null)
			fc.setFileFilter(fnef);
		fc.setMultiSelectionEnabled(false);
		
		File file;
		boolean ok;
		boolean cont;
		do {
			ok = false;
			final int result = (load) ? fc.showOpenDialog(parent) : fc.showSaveDialog(parent);
			if (result != JFileChooser.APPROVE_OPTION)
				return null;
			file = fc.getSelectedFile();
			
			final String cmsg = "\nSelect another file?";
			final String ctitle;
			final String msg;
			final boolean error;
			
			if (load) {
				if (!file.exists()) {
					msg = "File " + file + " does not exist.";
					ctitle = "File does not exist";
					error = true;
				} else if (file.isDirectory()) {
					msg = "File " + file + " is a directory";
					ctitle = "Invalid file selected";
					error = true;
				} else if (!file.canRead()) {
					msg = "Permissions to read file " + file + " not given";
					ctitle = "Cannot access file";
					error = true;
				} else {
					ok = true;
					break;
				}
			} else {
				if (file.exists()) {
					if (file.isDirectory()) {
						msg = "File " + file + " is a directory.";
						ctitle = "Invalid file selected";
						error = true;
					} else if (!file.canWrite()) {
						msg = "Permissions to write file " + file + " not given";
						ctitle = "Cannot access file";
						error = true;
					} else {
						msg = "File " + file + " already exists.";
						ctitle = "File exists";
						error = false;
					}
				} else {
					ok = true;
					break;
				}
			}
			
			final int answer = contChoose(parent, ctitle, msg + cmsg, msg, tryAgain, error);
			cont = (answer == JOptionPane.YES_OPTION);
			if (!error) {
				if (answer == JOptionPane.CANCEL_OPTION) {
					ok = false;
					break;
				} else {
					ok = !cont;
				}
			}
		} while (tryAgain && cont);
		
		return (ok) ? file : null;
	}
	
	private static int contChoose(
			final Component parent,
			final String title,
			final String message,
			final String noContMsg,
			boolean cont, boolean error) {
		final int optionType = (error) ? JOptionPane.ERROR_MESSAGE : JOptionPane.WARNING_MESSAGE;
		if (cont) {
			return JOptionPane.showConfirmDialog(
					parent, message, title, JOptionPane.YES_NO_CANCEL_OPTION, optionType);
		} else {
			JOptionPane.showMessageDialog(parent, noContMsg, title, optionType);
			return -1;
		}
	}
	
	public static <E extends Window> void setWindowProps(
			final E frame,
			final Container container,
			final JButton defaultButton,
			final Dimension size,
			final Point location) {
		if (frame instanceof RootPaneContainer) {
			final RootPaneContainer rpc = (RootPaneContainer)frame;
			final JRootPane rootPane = rpc.getRootPane();
			if (defaultButton != null)
				rootPane.setDefaultButton(defaultButton);
			rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
					KeyStroke.getKeyStroke('W', InputEvent.CTRL_DOWN_MASK), KE_CLOSE);
			rootPane.getActionMap().put(KE_CLOSE, new WindowCloseAction(frame));
			rpc.setContentPane(container);
		}
		
		if (size == null) {
			frame.pack();
		} else {
			frame.setSize(size);
		}
		
		if (location == null || location == LOCATION_BY_PLATFORM) {
			frame.setLocationByPlatform(true);
		} else if (location == LOCATION_CENTER) {
			centerOnScreen(frame);
		} else {
			frame.setLocation(location);
		}
	}
	
	public static void addListeners(final Object frame, final EventListener... listeners) {
		if (listeners != null && listeners.length > 0) {
			for (final Method m : frame.getClass().getMethods()) {
				final Class<?>[] pt = m.getParameterTypes();
				if (pt.length != 1)
					continue;
				final String name = m.getName();
				if (!name.startsWith("add") || !name.endsWith("Listener"))
					continue;
				for (final EventListener l : listeners) {
					if (l != null && pt[0].isAssignableFrom(l.getClass())) try {
						m.invoke(frame, l);
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public static void setFrameProps(
			final Frame frame,
			final Container container,
			final String title,
			final Dimension size,
			final boolean resizable,
			final Point location,
			final JButton defaultButton,
			final EventListener... listeners) {
		setWindowProps(frame, container, defaultButton, size, location);
		frame.setTitle(title);
		if (frame instanceof JFrame)
			((JFrame)frame).setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setResizable(resizable);
		addListeners(frame, listeners);
	}
	
	public static void setDialogProps(
			final Dialog dialog,
			final Container container,
			final String title,
			final Dimension size,
			final boolean resizable,
			final Point location,
			final JButton defaultButton,
			final EventListener... listeners) {
		setWindowProps(dialog, container, defaultButton, size, location);
		dialog.setTitle(title);
		if (dialog instanceof JDialog)
			((JDialog)dialog).setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.setResizable(resizable);
		addListeners(dialog, listeners);
	}
	
	public static JFrame wrapIntoFrame(
			final Container container,
			final String title,
			final Dimension size,
			final boolean resizable,
			final Point location,
			final JButton defaultButton,
			final EventListener... listeners) {
		final JFrame frame = new JFrame();
		setFrameProps(frame, container, title, size, resizable, location, defaultButton, listeners);
		return frame;
	}
	
	public static class WindowCloseAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;
		
		private final Window window;
		
		public WindowCloseAction(final Window window) {
			this.window = window;
		}
		
		public void actionPerformed(ActionEvent e) {
			window.dispose();
		}
	}
	
	public static final Insets DEFAULT_INSETS = new Insets(5, 5, 5, 5);
	
	public static void showURLErrorMessage(final String message, final String url) {
		final JButton close = new JButton("Close");
		final JPanel panel = new JPanel(new GridBagLayout());
		final JFrame frame = wrapIntoFrame(panel, "Error", null, false, LOCATION_CENTER, close);
		close.setAction(new WindowCloseAction(frame));
		
		final GridBagConstraints gbc = new GridBagConstraints();
		final UIDefaults def = UIManager.getLookAndFeelDefaults();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 2;
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.weighty = 1.0;
		gbc.insets = DEFAULT_INSETS;
		panel.add(new JLabel(def.getIcon("OptionPane.errorIcon")), gbc);
		
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.gridy = 2;
		gbc.weighty = 0.0;
		panel.add(close, gbc);
		
		gbc.gridx = 1;
		gbc.gridwidth = 1;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		final JTextArea textField = new JTextArea(message);
		setTextLabelDefaults(textField);
		panel.add(textField, gbc);
		
		final JTextField urlField = new JTextField(url);
		setTextLabelDefaults(urlField);
		urlField.setAutoscrolls(true);
		urlField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		urlField.select(0, url.length());
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weighty = 0.0;
		panel.add(urlField, gbc);
		
		frame.setVisible(true);
	}
	
	public static void setTextLabelDefaults(final JTextComponent textComponent) {
		final UIDefaults def = UIManager.getLookAndFeelDefaults();
		textComponent.setFont(def.getFont("OptionPane.font"));
		textComponent.setBackground(def.getColor("OptionPane.background"));
		textComponent.setForeground(def.getColor("OptionPane.foreground"));
		textComponent.setBorder(null);
		textComponent.setEditable(false);
		textComponent.setFocusable(false);
	}
	
	public static void centerOnScreen(final Component component) {
		centerOn(component, Toolkit.getDefaultToolkit().getScreenSize());
	}
	
	public static void centerOn(final Component component, final Component parent) {
		if (parent == null) {
			centerOnScreen(component);
		} else {
			centerOn(component, parent.getSize());
		}
	}
	
	public static void centerOn(final Component component, final Dimension d) {
		centerOn(component, component.getSize(), d);
	}
	
	public static void centerOn(final Component component, final Dimension cdim, final Dimension d) {
		component.setLocation(Math.max(0, (d.width - cdim.width) / 2), Math.max(0, (d.height - cdim.height) / 2));
	}
	
	/* 
	 * Code below (modified):
	 * ~~~~~~~~~~
	 * Author: Franz Brauße
	 * from YacyAdmin - relicensed
	 * License: CPL v1.0
	 */
	private static final String SHOW_TEXT 			= "Show stacktrace";
	private static final String HIDE_TEXT 			= "Hide stacktrace";
	private static final String LBL_TITLE 			= "An error occured";
	private static final String ERR_MSG_ 			= "Error message:";
	private static final String LBL_STACKTRACE_ 	= "Stacktrace:";
	private static final String LBL_OK 				= "OK";
	private static final String LBL_COPY2CLIPBRD 	= "Copy to clipboard";
	
	private static final Dimension hiddenDim = new Dimension(500, 84);
	private static final Dimension shownDim = new Dimension(500, 300);
	
	private static final String AC_OK = new String();
	private static final String AC_HIDE = new String();
	private static final String AC_CPY = new String();
	
	public static void showExceptionBox(final Frame parent, final String detail, final Throwable ex) {
		final JPanel displayPanel = new JPanel(new GridBagLayout());
		
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.anchor = GridBagConstraints.NORTHEAST;
		displayPanel.add(new JLabel(ERR_MSG_), gbc);
		
		gbc.gridx = 1;
		gbc.weightx = 1D;
		gbc.anchor = GridBagConstraints.WEST;
		displayPanel.add(new JLabel(detail), gbc);
		
		final JLabel lStackTrace = new JLabel(LBL_STACKTRACE_);
		lStackTrace.setVisible(false);
		gbc.weightx = 0D;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.NORTHEAST;
		displayPanel.add(lStackTrace, gbc);
		
		final JTextArea textStacktrace = new JTextArea();
		textStacktrace.setEditable(false);
		textStacktrace.setBorder(null);
		ex.printStackTrace(new PrintWriter(new Writer() {
			
			final Document doc = textStacktrace.getDocument();
			
			@Override public void close() throws IOException {  }
			@Override public void flush() throws IOException {  }
			
			@Override
			public void write(int c) throws IOException {
				write(String.valueOf(c));
			}
			
			@Override
			public void write(char[] cbuf, int off, int len) throws IOException {
				write(String.valueOf(cbuf, off, len));
			}
			
			@Override
			public void write(String str, int off, int len) throws IOException {
				try { doc.insertString(doc.getLength(), str.substring(off, off + len), null); } catch (BadLocationException e) { e.printStackTrace(); }
			}
		}));
		
		final JScrollPane exceptionLog = new JScrollPane();
		exceptionLog.setViewportView(textStacktrace);
		exceptionLog.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		exceptionLog.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		exceptionLog.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		exceptionLog.setVisible(false);
		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1D;
		gbc.weighty = 1D;
		displayPanel.add(exceptionLog, gbc);
		
		final JDialog eb = new JDialog(parent, true);
		final ActionListener l = new ActionListener() {
			
			private boolean hidden = true;
			
			public void actionPerformed(ActionEvent e) {
				final String ac = e.getActionCommand();
				if (ac == AC_CPY) {
					final Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
					cb.setContents(new StringSelection(textStacktrace.getText()), null);
				} else if (ac == AC_HIDE) {
					hidden = !hidden;
					((JButton)e.getSource()).setText((hidden) ? SHOW_TEXT : HIDE_TEXT);
					exceptionLog.setVisible(!hidden);
					lStackTrace.setVisible(!hidden);
					eb.setSize((hidden) ? hiddenDim : shownDim);
					exceptionLog.setViewportView((hidden) ? null : textStacktrace);
					textStacktrace.updateUI();
					lStackTrace.updateUI();
				} else if (ac == AC_OK) {
					eb.dispose();
				}
			}
		};
		
		final JPanel submitPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
		submitPanel.add(createButton(LBL_COPY2CLIPBRD, l, AC_CPY, null));
		submitPanel.add(createButton(SHOW_TEXT, l, AC_HIDE, null));
		final JButton btnOk;
		submitPanel.add(btnOk = createButton(LBL_OK, l, AC_OK, null));
		
		final JPanel content = new JPanel();
		content.setLayout(new BorderLayout());
		content.add(displayPanel, BorderLayout.CENTER);
		content.add(submitPanel, BorderLayout.SOUTH);
		
		setDialogProps(eb, content, LBL_TITLE, hiddenDim, true, null, btnOk);
		centerOn(eb, parent);
		eb.setVisible(true);
	}
	
	public static void showExceptionBox(final String detail, final Throwable ex) {
		showExceptionBox(null, detail, ex);
	}
	
	public static void showExceptionBox(final Throwable ex) {
		showExceptionBox(null, ex.getMessage(), ex);
	}
}
