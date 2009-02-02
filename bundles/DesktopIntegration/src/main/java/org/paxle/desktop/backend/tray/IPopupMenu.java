/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package org.paxle.desktop.backend.tray;

import org.paxle.desktop.backend.IPopupMenuListener;

public interface IPopupMenu {
	
	/**
	 * Adds an {@link IMenuItem} to the bottom of this menu 
	 * @param menuItem the item to add
	 */
	public void add(IMenuItem menuItem);
	
	/**
	 * Inserts an {@link IMenuItem} at the position (with <code>0</code> being the item
	 * at the top of the menu)
	 * @param menuItem the item to insert
	 * @param index position at which to insert the item
	 */
	public void insert(IMenuItem menuItem, int index);
	
	/**
	 * Adds a menu-separator to the bottom of this menu. Separators are used to separate
	 * entries which do not belong together in a sense of categories.
	 */
	public void addSeparator();
	
	/**
	 * Inserts a menu-separator at the specified position.
	 * @param index the position at which to insert the menu-separator
	 */
	public void insertSeparator(int index);
	
	/**
	 * Removes the {@link IMenuItem} at the given position (with <code>0</code> being the
	 * item at the top of the menu) from this menu
	 * @param index position from which to remove an item
	 */
	public void remove(int index);
	
	/**
	 * Removes an {@link IMenuItem} from this menu.
	 * @param menuItem the item to remove
	 */
	public void remove(IMenuItem menuItem);
	
	/**
	 * A convenience method to initially fill this menu or to add a chunk of new entries
	 * to the bottom of this menu.
	 * <p>
	 * Each {@link IMenuItem} passed to this method is added subsequentially, whereas
	 * <code>null</code>-values are treated as separators.
	 * @param items the {@link IMenuItem}s to add to the bottom of this menu
	 */
	public void init(IMenuItem... items);
	
	/**
	 * Adds a popup menu listener to this menu. It catches events whenever this popup-menu
	 * becomes visible and when it becomes invisible.
	 * <p><i>The 'canceled'-event is not necessarily implemented by all backends</i>. 
	 * @param l the {@link PopupMenuListener} to add to this menu
	 */
	public void addPopupMenuListener(IPopupMenuListener l);
}
