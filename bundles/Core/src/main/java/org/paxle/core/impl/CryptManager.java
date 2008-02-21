
package org.paxle.core.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;

import org.paxle.core.ICryptManager;
import org.paxle.core.crypt.ICrypt;

public class CryptManager implements ICryptManager {
	
	private final Hashtable<String,ICrypt> crypts = new Hashtable<String,ICrypt>();
	
	public ICrypt getCrypt(String name) {
		return this.crypts.get(name);
	}
	
	void addCrypt(String name, ICrypt crypt) {
		this.crypts.put(name, crypt);
	}
	
	void removeCrypt(String name) {
		this.crypts.remove(name);
	}
	
	public Collection<ICrypt> getCrypts() {
		return new HashSet<ICrypt>(this.crypts.values());
	}
	
	public Collection<String> getNames() {
		return new HashSet<String>(this.crypts.keySet());
	}
}
