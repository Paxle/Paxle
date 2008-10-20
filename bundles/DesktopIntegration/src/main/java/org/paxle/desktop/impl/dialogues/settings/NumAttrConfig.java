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
				
				final long val = (value instanceof String) ? Long.parseLong((String)value) : ((Number)value).longValue();
				if (ad.validate(Long.toString(val)) != null) {
					final long[] minmax = getMinMax(lmin, lmax, val);
					comp.setModel(new SpinnerNumberModel(
							(Number)value,
							toNumber(minmax[0]), toNumber(minmax[1]),
							Integer.valueOf(1)));
				}
			} break;
			
			case AttributeDefinition.FLOAT: {
				final float val = (value instanceof String) ? Float.parseFloat((String)value) : ((Number)value).floatValue();
				if (ad.validate(String.valueOf(val)) != null) {
					final float[] minmax = getMinMax(-Float.MAX_VALUE, Float.MAX_VALUE, val);
					comp.setModel(new SpinnerNumberModel(
							(Number)value,
							Float.valueOf(minmax[0]), Float.valueOf(minmax[1]),
							Double.valueOf(0.1)));
				}
			} break;
			
			case AttributeDefinition.DOUBLE: {
				final double val = (value instanceof String) ? Double.parseDouble((String)value) : ((Number)value).doubleValue();
				if (ad.validate(String.valueOf(val)) != null) {
					final double[] minmax = getMinMax(-Double.MAX_VALUE, Double.MAX_VALUE, val);
					comp.setModel(new SpinnerNumberModel(
							(Number)value,
							Double.valueOf(minmax[0]), Double.valueOf(minmax[1]),
							Double.valueOf(0.1)));
				}
			} break;
		}
		
		mcl.addComp2Monitor(comp);
		return comp;
	}
	
	@Override
	public Number getValue() {
		return fromObject(getSpinnerValue());
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
	
	private String getSpinnerValue() {
		try { comp.commitEdit(); } catch (ParseException e) {
			final JComponent editor = comp.getEditor();
			if (editor instanceof DefaultEditor)
				((DefaultEditor)editor).getTextField().setText(comp.getValue().toString());
		}
		String val = comp.getValue().toString();
		if (ad.getType() == AttributeDefinition.FLOAT || ad.getType() == AttributeDefinition.DOUBLE) {
			final int idx = val.indexOf('.');
			if (idx > -1)
				val = val.substring(0, idx);
		}
		return val;
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
	
	private static final int MINMAX_LONG_ITERATIONS = 63;			// max. iterations needed for type long
	private static final int MINMAX_FLOAT_ITERATIONS =
		(int)Math.ceil((Math.log(Float.MAX_VALUE) - Math.log(Float.MIN_VALUE)) / Math.log(2.0)); // 277
	private static final int MINMAX_DOUBLE_ITERATIONS =
		(int)Math.ceil((Math.log(Double.MAX_VALUE) - Math.log(Double.MIN_VALUE)) / Math.log(2.0)); // 2098
	
	private float[] getMinMax(final float min, final float max, final float mid) {
		final boolean omitMin = (ad.validate(Float.toString(min)).length() == 0);
		final boolean omitMax = (ad.validate(Float.toString(max)).length() == 0);
		if (omitMin && omitMax)
			return new float[] { min, max };
		
		float minlo = min;
		float minhi = (omitMin) ? min : mid;
		float maxlo = (omitMax) ? max : mid;
		float maxhi = max;
		
		for (int i=0; i<MINMAX_FLOAT_ITERATIONS; i++) {
			if (!omitMin) {
				final float nextMin = minhi - (minhi - minlo) / 2;
				if (ad.validate(Float.toString(nextMin)).length() == 0) {
					minhi = nextMin;
				} else {
					minlo = nextMin;
				}
			}
			
			if (!omitMax) {
				final float nextMax = maxhi - (maxhi - maxlo) / 2;
				if (ad.validate(Float.toString(nextMax)).length() == 0) {
					maxlo = nextMax;
				} else {
					maxhi = nextMax;
				}
			}
		}
		
		return new float[] { minhi, maxlo };
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
		
		for (int i=0; i<MINMAX_DOUBLE_ITERATIONS; i++) {
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
		
		for (int i=0; i<MINMAX_LONG_ITERATIONS; i++) {
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
