/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2010 the original author or authors.
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

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

import org.osgi.service.metatype.AttributeDefinition;
import org.paxle.desktop.impl.event.MultipleChangesListener;

class ListAttrConfig extends AbstractMultiAttrConfig {
	
	private final class Entry {
		
		private final int idx;
		
		public Entry(final int idx) {
			this.idx = idx;
		}
		
		public String getValue() {
			return values[idx];
		}
		
		@Override
		public String toString() {
			return labels[idx];
		}
	}
	
	private JList comp;
	
	public ListAttrConfig(final AttributeDefinition ad) {
		super(ad);
	}
	
	@Override
	protected JComponent createOptionComp(Object value, MultipleChangesListener mcl) {
		final Entry[] entries = new Entry[labels.length];
		for (int i=0; i<entries.length; i++)
			entries[i] = new Entry(i);
		comp = new JList(entries);
		comp.setBorder(BorderFactory.createEtchedBorder());
		comp.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		mcl.addComp2Monitor(comp);
		return comp;
	}
	
	@Override
	public Object getValue() {
		final Object[] temp = comp.getSelectedValues();
		
		// convert values into proper format
		// XXX: we have a problem here if the required target-value-type is not a String
		// XXX: we should be able to use the indices directly now, but since all the sub-configs are being recreated on update,
		//      we have no other choice currently
		String[] val = new String[(temp == null) ? 0 : temp.length];
		for (int i=0; i<val.length; i++)
			val[i] = ((Entry)temp[i]).getValue();
		return val;
	}
	
	@Override
	public void setValue(Object newValue) {
		comp.setSelectedIndices((int[])newValue);
	}
}
