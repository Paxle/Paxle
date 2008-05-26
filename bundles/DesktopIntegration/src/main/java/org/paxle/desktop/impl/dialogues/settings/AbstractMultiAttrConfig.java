
package org.paxle.desktop.impl.dialogues.settings;

import java.util.HashMap;

import org.osgi.service.metatype.AttributeDefinition;

abstract class AbstractMultiAttrConfig extends AbstractAttrConfig<Object> {
	
	protected final String[] labels;
	protected final String[] values;
	protected final HashMap<String,Integer> valMap = new HashMap<String,Integer>();
	
	protected AbstractMultiAttrConfig(final AttributeDefinition ad) {
		super(ad);
		labels = ad.getOptionLabels();
		values = ad.getOptionValues();
		for (int i=0; i<values.length; i++)
			valMap.put(values[i].intern(), Integer.valueOf(i));
	}
	
	@Override
	protected Object fromObject(Object value) {
		return valMap.get(value);
	}
	
	@Override
	protected int[] normalize(Object currentValue) {
		final Object value = currentValue; // super.normalize(currentValue);
		
		final int[] selectedIndices;
		if (value == null) {
			selectedIndices = new int[1];
		} else if (value.getClass().isArray()) {
			final Object[] curVals = (Object[])value;
			// get selected indices
			selectedIndices = new int[curVals.length];
			int i = 0;
			for (final Object val : curVals) {
				final Integer idx = valMap.get(((val instanceof String) ? (String)val : val.toString()).intern());
				if (idx != null)
					selectedIndices[i++] = idx.intValue();
			}
		} else {
			selectedIndices = new int[1];
			final Integer idx = valMap.get(((value instanceof String) ? (String)value : value.toString()).intern());
			if (idx != null)
				selectedIndices[0] = idx.intValue();
		}
		return selectedIndices;
	}
}
