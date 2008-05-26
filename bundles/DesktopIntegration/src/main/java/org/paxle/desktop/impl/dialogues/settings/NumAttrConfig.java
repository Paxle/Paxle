
package org.paxle.desktop.impl.dialogues.settings;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.ParseException;

import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.JSpinner.DefaultEditor;

import org.osgi.service.metatype.AttributeDefinition;
import org.paxle.desktop.impl.event.MultipleChangesListener;

class NumAttrConfig extends AbstractAttrConfig<Number> implements MouseWheelListener {
	
	private JSpinner comp = new JSpinner();
	
	public NumAttrConfig(final AttributeDefinition ad) {
		super(ad);
	}
	
	public void mouseWheelMoved(MouseWheelEvent e) {
		final SpinnerModel m = comp.getModel();
		final Object nv = ((e.getWheelRotation() < 0) ? m.getNextValue() : m.getPreviousValue());
		if (nv != null)
			m.setValue(nv);
	}
	
	@Override
	protected JComponent createOptionComp(Object value, MultipleChangesListener mcl) {
		long lmin = 0L, lmax = 0L;
		double dmin = 0.0, dmax = 0.0;
		comp = new JSpinner();
		comp.addMouseWheelListener(this);
		switch (ad.getType()) {
			case AttributeDefinition.SHORT:
				lmin = Short.MIN_VALUE;
				lmax = Short.MAX_VALUE;
				// fall through
			case AttributeDefinition.INTEGER:
				if (lmin == 0L) lmin = Integer.MIN_VALUE;
				if (lmax == 0L) lmax = Integer.MAX_VALUE;
				// fall through
			case AttributeDefinition.LONG: {
				if (lmin == 0L) lmin = Long.MIN_VALUE;
				if (lmax == 0L) lmax = Long.MAX_VALUE;
				
				if (value instanceof String)
					value = Long.valueOf((String)value);
				
				final long val = ((Number)value).longValue();
				if (ad.validate(Long.toString(val)) != null) {
					final long[] minmax = getMinMax(lmin, lmax, val);
					comp.setModel(new SpinnerNumberModel(
							(Number)value,
							toNumber(minmax[0]), toNumber(minmax[1]),
							Integer.valueOf(1)));
				}
			} break;
			
			case AttributeDefinition.FLOAT:
				dmin = Float.MIN_VALUE;
				dmax = Float.MAX_VALUE;
				// fall-through
			case AttributeDefinition.DOUBLE: {
				if (dmin == 0.0) dmin = Double.MIN_VALUE;
				if (dmax == 0.0) dmax = Double.MAX_VALUE;
				
				if (value instanceof String)
					value = Double.valueOf((String)value);
				
				final double val = ((Number)value).doubleValue();
				if (ad.validate(Double.toString(val)) != null) {
					final double[] minmax = getMinMax(dmin, dmax, val);
					comp.setModel(new SpinnerNumberModel(
							(Number)value,
							toNumber(minmax[0]), toNumber(minmax[1]),
							Double.valueOf(0.1)));
				}
			} break;
		}
		
		mcl.addComp2Monitor(comp);
		return comp;
	}
	
	@Override
	public Number getValue() {
		switch (ad.getType()) {
			case AttributeDefinition.SHORT: return Short.valueOf(getSpinnerValue(true));
			case AttributeDefinition.INTEGER: return Integer.valueOf(getSpinnerValue(true));
			case AttributeDefinition.LONG: return Long.valueOf(getSpinnerValue(true));
			
			case AttributeDefinition.FLOAT: return Float.valueOf(getSpinnerValue(false));
			case AttributeDefinition.DOUBLE: return Double.valueOf(getSpinnerValue(false));
			
			default:
				throw new IllegalStateException("attr-type: " + ad.getType() + " invalid for " + NumAttrConfig.class.getSimpleName());
		}
	}
	
	@Override
	public void setValue(Number value) {
		comp.setValue(value);
	}
	
	@Override
	protected Number fromObject(Object value) {
		if (value instanceof Number)
			return (Number)value;
		
		final String str = value.toString();
		switch (ad.getType()) {
			case AttributeDefinition.SHORT: return Short.valueOf(str);
			case AttributeDefinition.INTEGER: return Integer.valueOf(str);
			case AttributeDefinition.LONG: return Long.valueOf(str);
			case AttributeDefinition.FLOAT: return Float.valueOf(str);
			case AttributeDefinition.DOUBLE: return Double.valueOf(str);
			default:
				throw new IllegalStateException("attr-type: " + ad.getType() + " invalid for " + NumAttrConfig.class.getSimpleName());
		}
	}
	
	private String getSpinnerValue(final boolean dec) {
		try { comp.commitEdit(); } catch (ParseException e) {
			final JComponent editor = comp.getEditor();
			if (editor instanceof DefaultEditor)
				((DefaultEditor)editor).getTextField().setText(comp.getValue().toString());
		}
		final String val = comp.getValue().toString();
		final int idx = (dec) ? val.indexOf('.') : -1;
		return (idx == -1) ? val : val.substring(0, idx);
	}
	
	private Comparable<? extends Number> toNumber(final long n) {
		switch (ad.getType()) {
			case AttributeDefinition.SHORT: return Short.valueOf((short)n);
			case AttributeDefinition.INTEGER: return Integer.valueOf((int)n);
			case AttributeDefinition.LONG: return Long.valueOf(n);
			
			default:
				throw new IllegalArgumentException("long is incompatible to attr-def-type: " + ad.getType());
		}
	}
	
	private Comparable<? extends Number> toNumber(final double n) {
		switch (ad.getType()) {
			case AttributeDefinition.FLOAT: return Float.valueOf((float)n);
			case AttributeDefinition.DOUBLE: return Double.valueOf(n);
			
			default:
				throw new IllegalArgumentException("double is incompatible to attr-def-type: " + ad.getType());
		}
	}
	
	private double[] getMinMax(final double min, final double max, final double mid) {
		final boolean omitMin = (ad.validate(Double.toString(min)).length() == 0);
		final boolean omitMax = (ad.validate(Double.toString(max)).length() == 0);
		if (omitMin && omitMax)
			return new double[] { min, max };
		
		double minlo = min;
		double minhi = (omitMin) ? min : mid;
		double maxlo = (omitMax) ? max : mid;
		double maxhi = max;
		
		for (int i=0; i<SettingsPanel.MAX_DET_MINMAX_ITERATIONS; i++) {
			if (!omitMin) {
				final double nextMin = minhi - (minhi - minlo) / 2;
				if (ad.validate(Double.toString(nextMin)).length() == 0) {
					minhi = nextMin;
				} else {
					minlo = nextMin;
				}
			}
			
			if (!omitMax) {
				final double nextMax = maxhi - (maxhi - maxlo) / 2;
				if (ad.validate(Double.toString(nextMax)).length() == 0) {
					maxlo = nextMax;
				} else {
					maxhi = nextMax;
				}
			}
		}
		
		return new double[] { minhi, maxlo };
	}
	
	private long[] getMinMax(final long min, final long max, final long mid) {
		final boolean omitMin = (ad.validate(Long.toString(min)).length() == 0);
		final boolean omitMax = (ad.validate(Long.toString(max)).length() == 0);
		if (omitMin && omitMax)
			return new long[] { min, max };
		
		long minlo = min;
		long minhi = (omitMin) ? min : mid;
		long maxlo = (omitMax) ? max : mid;
		long maxhi = max;
		
		for (int i=0; i<SettingsPanel.MAX_DET_MINMAX_ITERATIONS; i++) {
			if (!omitMin) {
				final long nextMin = minhi - (minhi - minlo) / 2;
				if (ad.validate(Long.toString(nextMin)).length() == 0) {
					minhi = nextMin;
				} else {
					minlo = nextMin;
				}
			}
			
			if (!omitMax) {
				final long nextMax = maxhi - (maxhi - maxlo) / 2;
				if (ad.validate(Long.toString(nextMax)).length() == 0) {
					maxlo = nextMax;
				} else {
					maxhi = nextMax;
				}
			}
		}
		
		return new long[] { minhi, maxlo };
	}
}
