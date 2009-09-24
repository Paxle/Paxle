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
package org.paxle.filter.wordlistcreator.impl;

import java.io.File;
import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.paxle.core.doc.ICommand;
import org.paxle.filter.wordlistcreator.impl.wordlist.StorageManager;
import org.paxle.filter.wordlistcreator.impl.wordlist.Token;
import org.paxle.filter.wordlistcreator.impl.wordlist.TokenCollection;

public class TokenManager {

	/**
	 * The number of tokens cached until they are flushed to HDD
	 */
	private int cachesize = 10;

	private int mintokensize = 2;

	/**
	 * If set to true, the originating URI for every token is recorded
	 */
	private boolean recordUris = true;

	private TokenCollection tokencache = null;

	private StorageManager storage = null;

	public TokenManager(File dataDir) throws IOException {
		this.storage = new StorageManager(dataDir);
		reInitTokencache();
	}

	synchronized public void registerToken(@Nonnull String stoken, @CheckForNull ICommand tokencommand) {
		if (stoken.length() < mintokensize) return;
		Token token = new Token(stoken);
		if (tokencommand != null) {
			if (recordUris) {
				token.setUri(tokencommand.getLocation());
			}
		}
		this.tokencache.addToken(token);
		if (this.tokencache.isFull()) {
			this.storage.store(this.tokencache);
			reInitTokencache();
		}
	}

	public void close() {
		this.storage.store(this.tokencache);
	}
	
	private void reInitTokencache() {
		this.tokencache = new TokenCollection(cachesize);
	}

}
