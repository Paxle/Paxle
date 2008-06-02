
package org.paxle.desktop.impl.dialogues.settings;

import java.awt.GridBagConstraints;
import java.util.Hashtable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.osgi.service.metatype.AttributeDefinition;
import org.paxle.desktop.Utilities;
import org.paxle.desktop.impl.event.MultipleChangesListener;

abstract class AbstractAttrConfig<E> {
	
	protected final AttributeDefinition ad;
	
	protected AbstractAttrConfig(final AttributeDefinition ad) {
		this.ad = ad;
	}
	
	public static AbstractAttrConfig<?> createAttrConfig(final AttributeDefinition ad) {
		final String[] labels = ad.getOptionLabels();
		if (labels != null && labels.length > 0) {
			if (ad.getCardinality() == 0) {
				return new ComboAttrConfig(ad);
			} else {
				return new ListAttrConfig(ad);
			}
		} else switch (ad.getType()) {
			case AttributeDefinition.BOOLEAN: return new BooleanAttrConfig(ad);
			case AttributeDefinition.BYTE: break;
			case AttributeDefinition.CHARACTER: break;
			case AttributeDefinition.SHORT: // fall through
			case AttributeDefinition.INTEGER: // fall through
			case AttributeDefinition.LONG: // fall through
			case AttributeDefinition.FLOAT: // fall through
			case AttributeDefinition.DOUBLE: return new NumAttrConfig(ad);
			case AttributeDefinition.STRING: return new StringAttrConfig(ad);
			default:
				break;
		}
		throw new RuntimeException("attr-type " + ad.getType() + " not supported");
	}
	
	public void addConfigLine(
			final MultipleChangesListener optComp,
			final JPanel panel,
			final Object currentValue,
			final GridBagConstraints gbcLabel,
			final GridBagConstraints gbcOptSingle,
			final GridBagConstraints gbcOptMulti,
			final GridBagConstraints gbcDesc) {
		panel.add(createLabel(ad.getName()), gbcLabel);
		panel.add(createDescription(ad.getDescription()), gbcDesc);
		panel.add(createOptionComp(normalize(currentValue), optComp),
				(this instanceof AbstractMultiAttrConfig) ? gbcOptMulti : gbcOptSingle);
		setValue(currentValue, false);
	}
	
	protected E normalize(final Object currentValue) {
		E value;
		if (currentValue == null) {
			final String[] defs = ad.getDefaultValue();
			if (defs == null || defs.length == 0) {
				value = null;
			} else {
				value = fromObject(defs[0]);
			}
		} else if (currentValue.getClass().isArray()) {
			final Object[] cval = (Object[])currentValue;
			value = (cval.length == 0) ? null : fromObject(cval[0]);
		} else {
			value = fromObject(currentValue);
		}
		return value;
	}
	
	protected abstract E fromObject(Object value);
	
	protected JLabel createLabel(final String name) {
		return new JLabel(name);
	}
	
	protected JTextArea createDescription(final String desc) {
		final JTextArea df = new JTextArea(desc);
		Utilities.setTextLabelDefaults(df);
		df.setLineWrap(true);
		df.setWrapStyleWord(true);
		return df;
	}
	
	protected abstract JComponent createOptionComp(final Object value, final MultipleChangesListener mcl);
	
	public void setValue(final Object value, final boolean changed) {
		setValue(normalize(value));
	}
	
	public abstract E getValue();
	public abstract void setValue(E value);
	
	public void save(final Hashtable<String,Object> props) {
		props.put(ad.getID(), getValue());
	}
	
	public void reset(final Hashtable<String,Object> props) {
		setValue(ad.getDefaultValue(), true);
		save(props);
	}
}
