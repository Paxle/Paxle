/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
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

import java.awt.GridLayout;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.osgi.service.metatype.AttributeDefinition;
import org.paxle.desktop.impl.Messages;
import org.paxle.desktop.impl.event.MultipleChangesListener;

class BooleanAttrConfig extends AbstractAttrConfig<Boolean> {
	
	private JRadioButton rbTrue = new JRadioButton();
	private JRadioButton rbFalse = new JRadioButton();
	
	public BooleanAttrConfig(final AttributeDefinition ad) {
		super(ad);
	}
	
	@Override
	protected JComponent createOptionComp(Boolean value, MultipleChangesListener mcl) {
		final JPanel op = new JPanel(new GridLayout(1, 3, 5, 5));
		rbTrue = new JRadioButton(Messages.getString("settingsPanel.attrBoolean.true")); //$NON-NLS-1$
		rbFalse = new JRadioButton(Messages.getString("settingsPanel.attrBoolean.false")); //$NON-NLS-1$
		final ButtonGroup bg = new ButtonGroup();
		bg.add(rbTrue);
		bg.add(rbFalse);
		op.add(rbTrue);
		op.add(rbFalse);
		mcl.addComp2Monitor(rbTrue);
		return op;
	}
	
	@Override
	public Boolean getValue() {
		return Boolean.valueOf(rbTrue.isSelected());
	}
	
	@Override
	protected Boolean fromObject(Object value) {
		return (value instanceof Boolean) ? (Boolean)value : Boolean.valueOf(value.toString());
	}
	
	@Override
	public void setValue(Boolean value) {
		((value.booleanValue()) ? rbTrue : rbFalse).setSelected(true);
	}
}
