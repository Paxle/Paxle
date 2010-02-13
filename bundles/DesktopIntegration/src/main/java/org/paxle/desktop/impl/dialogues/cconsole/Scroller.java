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

package org.paxle.desktop.impl.dialogues.cconsole;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.table.DefaultTableModel;

final class Scroller implements Runnable {
	private final JScrollPane scroll;
	private final JTable table;
	private final DefaultTableModel model;
	
	private Vector<String> row;
	private AbstractButton clear;
	private int maxSize;
	
	public Scroller(final JScrollPane scroll, final JTable table, final DefaultTableModel model) {
		this.scroll = scroll;
		this.table = table;
		this.model = model;
	}
	
	public Scroller init(final Vector<String> row, final AbstractButton clear, final int maxSize) {
		this.row = row;
		this.clear = clear;
		this.maxSize = maxSize;
		return this;
	}
	
	public void run() {
		final JViewport vp = scroll.getViewport();
		final Rectangle viewRect = vp.getViewRect();
		if (viewRect.height + viewRect.y + 1 >= table.getHeight()) {
			if (model.getRowCount() == maxSize)
				model.removeRow(0);
			model.addRow(row);
			final Rectangle r = vp.getVisibleRect();
			r.setLocation(0, table.getHeight());
			vp.scrollRectToVisible(r);
		} else {
			final Point p = vp.getViewPosition();
			if (model.getRowCount() == maxSize) {
				model.removeRow(0);
				p.y = Math.max(0, p.y - table.getRowHeight());
			}
			vp.setViewPosition(p);
			model.addRow(row);
		}
		if (!clear.isEnabled())
			clear.setEnabled(true);
	}
}