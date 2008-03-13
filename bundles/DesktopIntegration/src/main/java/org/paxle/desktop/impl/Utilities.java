
package org.paxle.desktop.impl;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

public class Utilities {
	
	public static void showURLErrorMessage(final String message, final String url) {
		final JFrame frame = new JFrame("Error");
		final JPanel panel = new JPanel(new GridBagLayout());
		final GridBagConstraints gbc = new GridBagConstraints();
		final UIDefaults def = UIManager.getLookAndFeelDefaults();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 2;
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.weighty = 1.0;
		gbc.insets = new Insets(5, 5, 5, 5);
		panel.add(new JLabel(def.getIcon("OptionPane.errorIcon")), gbc);
		final JButton close = new JButton("Close");
		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
				frame.dispose();
			} });
		frame.getRootPane().setDefaultButton(close);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
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
		textField.setFont(def.getFont("OptionPane.font"));
		textField.setBackground(def.getColor("OptionPane.background"));
		textField.setForeground(def.getColor("OptionPane.foreground"));
		textField.setBorder(null);
		textField.setEditable(false);
		textField.setFocusable(false);
		panel.add(textField, gbc);
		final JTextField urlField = new JTextField(url);
		urlField.setFont(def.getFont("OptionPane.font"));
		urlField.setBackground(def.getColor("OptionPane.background"));
		urlField.setForeground(def.getColor("OptionPane.foreground"));
		urlField.setBorder(null);
		urlField.setAutoscrolls(true);
		urlField.setEditable(false);
		urlField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		urlField.select(0, url.length());
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weighty = 0.0;
		panel.add(urlField, gbc);
		
		frame.setContentPane(panel);
		frame.pack();
		centerOnScreen(frame);
		frame.setVisible(true);
	}
	
	public static void centerOnScreen(final Component component) {
		centerOnScreen(component, component.getWidth(), component.getHeight());
	}
	
	public static void centerOnScreen(final Component component, final int compWidth, final int compHeight) {
		final Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		component.setLocation(Math.max(0, (d.width - compWidth) / 2), Math.max(0, (d.height - compHeight) / 2));
	}
}
