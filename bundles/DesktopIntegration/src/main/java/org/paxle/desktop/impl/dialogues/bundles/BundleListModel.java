/*
This file is part of the Paxle project.
Visit http://www.paxle.net for more information.
Copyright 2007-2008 the original author or authors.

Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
or in the file LICENSE.txt in the root directory of the Paxle distribution.

Unless required by applicable law or agreed to in writing, this software is distributed
on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*/

package org.paxle.desktop.impl.dialogues.bundles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.event.ListDataEvent;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;

public final class BundleListModel implements BundleListener {
	
	private static final long serialVersionUID = 1L;
	
	private final Map<Long,BundleListRow> bundleMap = new TreeMap<Long,BundleListRow>();
	private final ArrayList<BundleListRow> entryList = new ArrayList<BundleListRow>();
	private final BundlePanel listener;
	private String restriction = null;
	
	public BundleListModel(final BundlePanel listener) {
		this.listener = listener;
	}
	
	public BundleListRow getElementAt(int index) {
		return entryList.get(index);
	}
	
	public int getSize() {
		return entryList.size();
	}
	
	// binary search, key is the bundle-id
	private int getPosition(final Bundle bundle, final boolean insertPosition) {
		final long id = bundle.getBundleId();
		int lo = 0, hi = entryList.size() - 1;
		int mid = 0;
		while (lo <= hi) {
			mid = (lo + hi) / 2;
			final long bid = entryList.get(mid).bundle.getBundleId();
			if (id < bid) {
				hi = mid - 1;
			} else if (id > bid) {
				lo = mid + 1;
			} else {
				return (insertPosition) ? -1 : mid;
			}
		}
		if (insertPosition) {
			if (hi < 0) {
				mid = 0;
			} else if (lo > entryList.size() - 1) {
				mid = entryList.size();
			}
			return mid;
		} else {
			return -1;
		}
	}
	
	private boolean isAllowed(final Bundle bundle) {
		return (restriction == null || restriction.length() == 0 ||
				bundle.getHeaders().get(Constants.BUNDLE_NAME).toString().toLowerCase().indexOf(restriction) != -1);
	}
	
	private void clearList() {
		final int size = entryList.size();
		fireEvent(ListDataEvent.INTERVAL_REMOVED, 0, entryList.subList(0, size));
		entryList.clear();
	}
	
	private void fireEvent(final int type, final int idx, final List<BundleListRow> list) {
		listener.modelDataChanged(type, idx, list);
	}
	
	public void setRestriction(final String restriction) {
		this.restriction = restriction.toLowerCase();
		updateList();
	}
	
	private void updateList() {
		if (restriction == null || restriction.length() == 0) {
			if (entryList.size() == bundleMap.size())
				return;
			clearList();
			entryList.addAll(bundleMap.values());
			fireEvent(ListDataEvent.INTERVAL_ADDED, 0, entryList.subList(0, entryList.size() - 1));
		} else {
			if (bundleMap.size() == 0) {
				clearList();
			} else if (entryList.size() == 0) {
				for (final BundleListRow cc : bundleMap.values())
					addList(cc);
			} else {
				clearList();
				for (final BundleListRow cc : bundleMap.values())
					addList(cc);
			}
		}
	}
	
	private boolean update(final Bundle bundle) {
		final int idx = getPosition(bundle, false);
		final boolean allowed = isAllowed(bundle);
		if (idx != -1) {
			if (allowed) {
				fireEvent(ListDataEvent.CONTENTS_CHANGED, idx, Arrays.asList(entryList.get(idx)));
			} else {
				removeList(bundle);
			}
		} else if (allowed) {
			addList(new BundleListRow(this, bundle));
		}
		return allowed;
	}
	
	private void addList(final BundleListRow cc) {
		final Bundle bundle = cc.bundle;
		if (!isAllowed(bundle))
			return;
		final int idx = getPosition(bundle, true);
		if (idx != -1) {
			cc.index = idx;
			entryList.add(idx, cc);
			fireEvent(ListDataEvent.INTERVAL_ADDED, idx, Arrays.asList(cc));
		}
	}
	
	private void removeList(final Bundle bundle) {
		final int idx = getPosition(bundle, false);
		if (idx > -1) {
			final BundleListRow cc = entryList.remove(idx);
			cc.index = -1;
			fireEvent(ListDataEvent.INTERVAL_REMOVED, idx, Arrays.asList(cc));
		}
	}
	
	public void bundleChanged(BundleEvent event) {
		bundleChanged(event.getBundle(), event.getType());
	}
	
	void bundleChanged(final Bundle bundle, final int type) {
		switch (type) {
			case BundleEvent.INSTALLED: // fall-through
			case BundleEvent.RESOLVED:
				// add the bundle to this list
				final BundleListRow cc = new BundleListRow(this, bundle);
				bundleMap.put(Long.valueOf(bundle.getBundleId()), cc);
				addList(cc);
				break;
				
			case BundleEvent.UNINSTALLED: // fall-through
			case BundleEvent.UNRESOLVED:
				// remove the bundle from this list
				bundleMap.remove(Long.valueOf(bundle.getBundleId()));
				removeList(bundle);
				break;
			
			default:
				// update the corresponding cell of this list
				cellChanged(bundleMap.get(Long.valueOf(bundle.getBundleId())));
				break;
		}
	}
	
	void cellChanged(final BundleListRow cc) {
		update(cc.bundle);
		listener.modelDataChanged(ListDataEvent.CONTENTS_CHANGED, getPosition(cc.bundle, false), Arrays.asList(cc));
		
	}
}