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

import javax.swing.JComboBox;
import javax.swing.JComponent;

import org.osgi.service.metatype.AttributeDefinition;
import org.paxle.desktop.impl.event.MultipleChangesListener;

class ComboAttrConfig extends AbstractMultiAttrConfig {
	
	private JComboBox comp;
	
	public ComboAttrConfig(final AttributeDefinition ad) {
		super(ad);
	}
	
	@Override
	protected JComponent createOptionComp(Object value, MultipleChangesListener mcl) {
		comp = new JComboBox(ad.getOptionLabels());
		comp.setEditable(false);
		mcl.addComp2Monitor(comp);
		return comp;
	}
	
	@Override
	public Object getValue() {
		return comp.getSelectedItem();
	}
	
	@Override
	public void setValue(Object value) {
		comp.setSelectedIndex(((int[])value)[0]);
	}
}
