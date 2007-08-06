package org.paxle.se.impl;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.se.ISearchPlugin;

public class SearchPluginManager {
	
	private final Log logger = LogFactory.getLog(SearchPluginListener.class);
	private final Hashtable<String,ISearchPlugin> searchPlugins = new Hashtable<String,ISearchPlugin>();
	
	public Set<ISearchPlugin> addSearchPlugin(ISearchPlugin plugin, String modifications) {
		this.logger.debug("Registering new SearchPlugin for modifications '" + modifications + "'");
		final String[] mods = modifications.split(";");
		final Set<ISearchPlugin> plugins = new HashSet<ISearchPlugin>();
		for (final String mod : mods) {
			final ISearchPlugin prevPlugin = this.searchPlugins.put(mod, plugin);
			if (prevPlugin != null)
				plugins.add(prevPlugin);
		}
		return plugins;
	}
	
	public Set<ISearchPlugin> removeSearchPlugin(String modifications) {
		this.logger.debug("Unregistering SearchPlugins for modifications '" + modifications + "'");
		final String[] mods = modifications.split(";");
		final Set<ISearchPlugin> plugins = new HashSet<ISearchPlugin>();
		for (final String mod : mods) {
			final ISearchPlugin plugin = this.searchPlugins.remove(mod);
			if (plugin != null)
				plugins.add(plugin);
		}
		return plugins;
	}
	
	public ISearchPlugin getModPlugin(String modification) {
		return this.searchPlugins.get(modification);
	}
	
	public boolean isSupported(String modification) {
		return this.searchPlugins.containsKey(modification);
	}
	
	public Set<ISearchPlugin> getSearchPlugins() {
		return new HashSet<ISearchPlugin>(this.searchPlugins.values());
	}
	
	public Set<String> getModifications() {
		return new HashSet<String>(this.searchPlugins.keySet());
	}
}
