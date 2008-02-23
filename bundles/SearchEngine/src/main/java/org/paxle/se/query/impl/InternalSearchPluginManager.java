package org.paxle.se.query.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.paxle.se.query.IModTokenFactory;

public class InternalSearchPluginManager {
	
	private final Map<String,IModTokenFactory> factories = new HashMap<String,IModTokenFactory>();
	
	void addPlugin(String tokenString, IModTokenFactory plugin) {
		final String[] tokens = tokenString.split(";");
		for (final String token : tokens)
			this.factories.put(token, plugin);
	}
	
	void removePlugin(String tokenString) {
		final String[] tokens = tokenString.split(";");
		for (final String token : tokens)
			this.factories.remove(token);
	}
	
	public boolean isSupported(String mod) {
		return this.factories.containsKey(mod);
	}
	
	public IModTokenFactory getTokenFactory(String mod) {
		return this.factories.get(mod);
	}
	
	public Collection<IModTokenFactory> getTokenFactories() {
		return this.factories.values();
	}
	
	public Collection<String> getSupportedTokens() {
		return this.factories.keySet();
	}
}
