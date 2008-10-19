/*
This file is part of the Paxle project.
Visit http://www.paxle.net for more information.
Copyright 2007-2008 the original author or authors.

Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
or in the file LICENSE.txt in the root directory of the Paxle distribution.

Unless required by applicable law or agreed to in writing, this software is distributed
on an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*/

package org.paxle.desktop.impl.dialogues;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.paxle.desktop.Utilities;

public class SmallDialog extends JDialog implements Runnable {
	
	private static final long serialVersionUID = 1L;
	
	private static final Dimension DIALOG_DIMENSION = new Dimension(500, 26);
	
	private class DWindowListener extends WindowAdapter {
		@Override
		public void windowActivated(WindowEvent e) {
			SmallDialog.this.text.requestFocusInWindow();
		}
		
		@Override
		public void windowGainedFocus(WindowEvent e) {
			SmallDialog.this.text.requestFocusInWindow();
		}
		
		@Override
		public void windowOpened(WindowEvent e) {
			SmallDialog.this.text.requestFocusInWindow();
		}
	}
	
	private class BtnListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			aborted = false;
			SwingUtilities.invokeLater(SmallDialog.this);
		}
	}
	
	private class TextListener extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				aborted = true;
				SwingUtilities.invokeLater(SmallDialog.this);
			}
		}
	}
	
	private final AFinally af;
	private final JLabel label = new JLabel();
	private final JTextField text = new JTextField();
	private final JButton btn = new JButton();
	private boolean aborted = false;
	
	public SmallDialog(final AFinally af) {
		super.setAlwaysOnTop(true);
		super.setUndecorated(true);
		this.af = af;
		
		final JPanel panel = createContentPanel();
		panel.add(this.label, BorderLayout.WEST);
		panel.add(this.text, BorderLayout.CENTER);
		panel.add(this.btn, BorderLayout.EAST);
		super.setContentPane(panel);
		Utilities.setDialogProps(this, panel, null, DIALOG_DIMENSION, false, Utilities.LOCATION_CENTER, btn, null, false, new DWindowListener());
		
		this.btn.addActionListener(new BtnListener());
		this.text.addKeyListener(new TextListener());
	}
	
	public SmallDialog(AFinally af, String labelText, String buttonText) {
		this(af);
		this.label.setText(labelText);
		this.btn.setText(buttonText);
	}
	
	public void run() {
		if (!aborted && af != null) {
			SmallDialog.this.af.init(SmallDialog.this.text.getText());
			new Thread(SmallDialog.this.af).start();
		}
		SmallDialog.this.dispose();
	}
	
	public void setLabelText(String text) {
		this.label.setText(text);
	}
	
	public void setLabelIcon(ImageIcon icon) {
		this.label.setIcon(icon);
	}
	
	public void setDefaultText(String text) {
		this.text.setText(text);
	}
	
	public void setButtonText(String text) {
		this.btn.setText(text);
	}
	
	private static final int genOff = 10;
	
	public void setLocation(int iconx, int icony, int iconSize) {
		final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int x;
		int y;
		int xoff = iconSize + genOff;
		int yoff = iconSize + genOff;
		
		if (iconx > screen.width / 2) {
			xoff *= -1;
			x = iconx - DIALOG_DIMENSION.width;
		} else {
			x = 0;
		}
		
		if (icony > screen.height / 2) {
			yoff = -genOff;
			y = icony - DIALOG_DIMENSION.height;
		} else {
			y = 0;
		}
		super.setLocation(x + xoff, y + yoff);
	}
	
	private static JPanel createContentPanel() {
		final JPanel panel = new JPanel(new BorderLayout(5, 5));
		
		final Color borderCol;
		/* determine a color for the border of the frame visible on the panel's background */
		final Color bgnd = UIManager.getLookAndFeel().getDefaults().getColor("Panel.background");
		final Color bgndd = bgnd.darker().darker();
		// if the background already is too dark
		if (bgnd.equals(Color.BLACK) || bgndd.equals(Color.BLACK)) {
			borderCol = bgnd.brighter().brighter();
		} else {
			borderCol = bgndd;
		}
		panel.setBorder(BorderFactory.createLineBorder(borderCol, 2));
		
		return panel;
	}
	
	public static String showDialog(final String labelText, final String buttonText) {
		final SmallDialog sd = new SmallDialog(null, labelText, buttonText);
		sd.setModal(true);
		sd.setVisible(true);
		sd.dispose();
		return (sd.aborted) ? null : sd.text.getText();
	}
	
	public static void main(String[] args) {
		/*
		SmallDialog sd = new SmallDialog(new AFinally() {
			@Override
			public void run() {
				System.out.println("AFINALLY IS RUNNING: '" + super.data + "'");
			}
		});
		sd.setLabelText("Enter here:");
		sd.setButtonText("OK");
		sd.setLocation(1200, 1000, 24);
		sd.setModal(true);
		sd.setVisible(true);
		*/
		// System.out.println("finished");
		
		System.out.println(showDialog("bla", "blubb"));
	}
}
