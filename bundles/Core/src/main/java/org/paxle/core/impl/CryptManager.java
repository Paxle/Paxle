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
