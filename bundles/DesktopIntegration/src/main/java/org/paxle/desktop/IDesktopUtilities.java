
package org.paxle.desktop;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.EventListener;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.JTextComponent;

public interface IDesktopUtilities {
	
	public static final Insets INSETS_DEFAULT = new Insets(5, 5, 5, 5);
	public static final Insets INSETS_ZERO = new Insets(0, 0, 0, 0);
	
	public static final Object LOCATION_CENTER = new Object();
	public static final Object LOCATION_BY_PLATFORM = new Object();
	public static final Dimension SIZE_PACK = new Dimension();
	
	public <E extends AbstractButton> E setButtonProps(
			final E b,
			final String text,
			final ActionListener al,
			final String actionCommand,
			final int mnemonic,
			final Icon icon);
	
	public File chooseSingleFile(
			final Component parent,
			final String title,
			final boolean load,
			final FileFilter fnef,
			final boolean tryAgain);
	
	public <E extends Window> void setWindowProps(
			final E frame,
			final Container container,
			final JButton defaultButton,
			final JButton cancelButton,
			final Dimension size,
			final Object location,
			final boolean isMainWindow);
	
	public void addListeners(final Object frame, final EventListener... listeners);
	
	public <E extends Frame> E setFrameProps(
			final E frame,
			final Container container,
			final String title,
			final Dimension size,
			final boolean resizable,
			final Object location,
			final JButton defaultButton,
			final boolean isMainWindow,
			final EventListener... listeners);
	
	public <E extends Dialog> E setDialogProps(
			final E dialog,
			final Container container,
			final String title,
			final Dimension size,
			final boolean resizable,
			final Object location,
			final JButton defaultButton,
			final JButton cancelButton,
			final boolean isMainWindow,
			final EventListener... listeners);
	
	public <E extends Component> E addGridbag(final JComponent comp, final E obj, final int x, final int y);
	public JPanel addGridbagSpacer(final JComponent comp, final int x, final int y);
	
	public <E extends Component> E addGridbag(
			final JComponent comp,
			final E obj,
			final int x, final int y,
			final int width, final int height,
			final int align,
			final int fill,
			final double weightx, final double weighty);
	
	public <E extends Component> E addGridbag(
			final JComponent comp,
			final E obj,
			final int x, final int y,
			final int width, final int height,
			final int align,
			final int fill,
			final double weightx, final double weighty,
			final Insets insets);
	
	public void addGridbagLine(final JComponent comp, final JComponent left, final JComponent right, int y);
	
	public void showURLErrorMessage(final String message, final String url);
	public <E extends JTextComponent> E setTextLabelDefaults(final E textComponent);
	
	public void centerOnScreen(final Component component);
	public void centerOn(final Component component, final Component parent);
	public void centerOn(final Component component, final Rectangle r);
	public void centerOn(final Component component, final Dimension d, final Point off);
	public void centerOn(final Component component, final Dimension cdim, final Dimension d, final Point off);
	
	public void showExceptionBox(final Frame parent, final String detail, final Throwable ex);
	public void showExceptionBox(final String detail, final Throwable ex);
	public void showExceptionBox(final Throwable ex);
}
