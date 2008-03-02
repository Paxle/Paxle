
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
