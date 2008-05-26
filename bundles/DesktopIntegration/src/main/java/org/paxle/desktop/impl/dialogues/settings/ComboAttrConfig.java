
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
