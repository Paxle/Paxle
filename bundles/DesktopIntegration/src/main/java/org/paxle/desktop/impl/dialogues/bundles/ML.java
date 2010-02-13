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

package org.paxle.desktop.impl.dialogues.bundles;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Set;

class ML extends MouseAdapter {
	
	private final HashMap<Component,BundleListRow> comps = new HashMap<Component,BundleListRow>();
	
	private BundleListRow lastExited;
	
	public void addToComps(final Set<Component> set, final BundleListRow row) {
		for (final Component c : set) {
			c.addMouseListener(this);
			comps.put(c, row);
		}
	}
	
	public void removeFromComps(final Set<Component> set) {
		comps.keySet().removeAll(set);
		for (final Component c : set)
			c.removeMouseListener(this);
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
		final BundleListRow row = comps.get(e.getComponent());
		if (row != lastExited) {
			if (lastExited != null)
				lastExited.updateDisplay(false, false);
			row.updateDisplay(true, true);
		}
	}
	
	@Override
	public void mouseExited(MouseEvent e) {
		lastExited = comps.get(e.getComponent());
	}
}