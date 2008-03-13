
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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.paxle.desktop.impl.Utilities;

public class SmallDialog extends JFrame {
	
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
			SwingUtilities.invokeLater(new DoAction(true));
		}
	}
	
	private class DoAction implements Runnable {
		
		private final boolean success;
		
		public DoAction(boolean success) {
			this.success = success;
		}
		
		public void run() {
			if (this.success) {
				SmallDialog.this.af.init(SmallDialog.this.text.getText());
				new Thread(SmallDialog.this.af).start();
			}
			SmallDialog.this.dispose();
		}
	}
	
	private class TextListener extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				SwingUtilities.invokeLater(new DoAction(false));
			}
		}
	}
	
	private final AFinally af;
	private final JLabel label = new JLabel();
	private final JTextField text = new JTextField();
	private final JButton btn = new JButton();
	
	public SmallDialog(AFinally af) {
		super();
		super.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		super.setResizable(false);
		super.setAlwaysOnTop(true);
		super.setUndecorated(true);
		super.addWindowListener(new DWindowListener());
		super.setSize(DIALOG_DIMENSION);
		super.getRootPane().setDefaultButton(this.btn);
		this.af = af;
		
		final JPanel panel = createContentPanel();
		panel.add(this.label, BorderLayout.WEST);
		panel.add(this.text, BorderLayout.CENTER);
		panel.add(this.btn, BorderLayout.EAST);
		super.setContentPane(panel);
		
		this.btn.addActionListener(new BtnListener());
		this.text.addKeyListener(new TextListener());
		
		Utilities.centerOnScreen(this, DIALOG_DIMENSION.width, DIALOG_DIMENSION.height);
	}
	
	public SmallDialog(AFinally af, String labelText, String buttonText) {
		this(af);
		this.label.setText(labelText);
		this.btn.setText(buttonText);
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
	
	public static void main(String[] args) {
		SmallDialog sd = new SmallDialog(new AFinally() {
			@Override
			public void run() {
				System.out.println("AFINALLY IS RUNNING: '" + super.data + "'");
			}
		});
		sd.setLabelText("Enter here:");
		sd.setButtonText("OK");
		sd.setLocation(1200, 1000, 24);
		sd.setVisible(true);
	}
}
