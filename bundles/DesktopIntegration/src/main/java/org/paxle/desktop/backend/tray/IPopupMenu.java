
package org.paxle.desktop.backend.tray;

import javax.swing.event.PopupMenuListener;

public interface IPopupMenu {
	
	/**
	 * Adds an {@link IMenuItem} to the bottom of this menu 
	 * @param menuItem the item to add
	 */
	public void add(IMenuItem menuItem);
	
	/**
	 * Adds a menu-separator to the bottom of this menu. Separators are used to separate
	 * entries which do not belong together in a sense of categories.
	 */
	public void addSeparator();
	
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
	public void addPopupMenuListener(PopupMenuListener l);
}
