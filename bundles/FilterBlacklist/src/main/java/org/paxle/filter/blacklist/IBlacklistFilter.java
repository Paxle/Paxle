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

package org.paxle.filter.blacklist;

import java.io.IOException;
import java.util.List;

import org.paxle.core.filter.IFilter;
import org.paxle.core.queue.ICommand;

public interface IBlacklistFilter extends IFilter<ICommand> {
	
	public void addList(String name);
	public List<String> getLists();
	public void removeList(String name);
	public void saveLists(boolean force);
	public void setActive(String name, boolean yes) throws IOException;
	public List<String> getActiveLists();
	
	public void addPattern(String pattern, String list);
	public void removePattern(String pattern, String list);
}
