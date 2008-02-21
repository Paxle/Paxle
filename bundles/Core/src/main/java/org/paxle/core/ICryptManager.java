
package org.paxle.core;

import java.util.Collection;

import org.paxle.core.crypt.ICrypt;

public interface ICryptManager {
	
	public ICrypt getCrypt(String name);
	public Collection<ICrypt> getCrypts();
	public Collection<String> getNames();
}
