
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
