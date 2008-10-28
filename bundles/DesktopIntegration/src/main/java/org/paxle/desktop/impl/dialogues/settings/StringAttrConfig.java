/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 * 
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.desktop.impl.dialogues.settings;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.osgi.service.metatype.AttributeDefinition;
import org.paxle.core.metadata.Attribute;
import org.paxle.desktop.impl.Messages;
import org.paxle.desktop.impl.event.MultipleChangesListener;

class StringAttrConfig extends AbstractAttrConfig<String> {
	
	private static final int MIN_CHARS = 15;
	
	private JTextComponent comp;
	private final boolean isMultiline;
	private final boolean isPassword;
	
	public StringAttrConfig(final AttributeDefinition ad, final Attribute attr) {
		super(ad);
		isMultiline = attr != null && attr.multiline();
		isPassword = attr != null && attr.password() || ad.getID().toLowerCase().contains("password"); //$NON-NLS-1$
	}
	
	@Override
	protected JTextArea createDescription(String desc) {
		final JTextArea a = super.createDescription(desc);
		if (isPassword) try {
			final Document doc = a.getDocument();
			doc.insertString(doc.getLength(), Messages.getString("settingsPanel.attrString.pwdSavedUnencrypted"), null); //$NON-NLS-1$
		} catch (BadLocationException e) { e.printStackTrace(); }
		return a;
	}
	
	@Override
	protected JComponent createOptionComp(String value, final MultipleChangesListener mcl) {
		comp = (isPassword) ? new JPasswordField() : (isMultiline) ? new JTextArea() : new JTextField();
		mcl.addComp2Monitor(comp);
		
		final Insets insets = comp.getInsets();
		final FontMetrics metrics = comp.getFontMetrics(comp.getFont());
		
		final Dimension minSize = new Dimension(
				MIN_CHARS * metrics.charWidth('m') + insets.left + insets.right,
				metrics.getHeight() * ((isMultiline) ? 4 : 1) + insets.bottom + insets.top);
		
		final JComponent r;
		if (comp instanceof JTextArea) {
			final JScrollPane scroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scroll.setViewportView(comp);
			final Insets sinsets = scroll.getInsets();
			minSize.width += sinsets.left + sinsets.right;
			minSize.height += sinsets.top + sinsets.bottom;
			scroll.setMinimumSize(minSize);
			scroll.setPreferredSize(minSize);
			r = scroll;
		} else {
			comp.setMinimumSize(minSize);
			comp.setPreferredSize(minSize);
			r = comp;
		}
		return r;
	}
	
	@Override
	public String getValue() {
		return comp.getText();
	}
	
	@Override
	protected String fromObject(Object value) {
		return value.toString();
	}
	
	@Override
	public void setValue(String value) {
		comp.setText(value);
	}
}
