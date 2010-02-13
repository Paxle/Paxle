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

import javax.swing.event.TableColumnModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

final class ConsoleTableColumnModel extends DefaultTableColumnModel {
	
	private static final long serialVersionUID = 1L;
	
	private static final DefaultTableCellRenderer TCR = new DefaultTableCellRenderer();
	
	public ConsoleTableColumnModel() {
	}
	
	@Override
	public TableColumn getColumn(int columnIndex) {
		return prepareColumn(super.getColumn(columnIndex));
	}
	
	private static TableColumn prepareColumn(final TableColumn tc) {
		if (tc.getCellRenderer() == null)
			tc.setCellRenderer(TCR);
		return tc;
	}
	
	@Override
	protected void fireColumnAdded(TableColumnModelEvent e) {
		for (int i=e.getFromIndex(); i<=e.getToIndex(); i++)
			prepareColumn(tableColumns.get(i));
		super.fireColumnAdded(e);
	}
}