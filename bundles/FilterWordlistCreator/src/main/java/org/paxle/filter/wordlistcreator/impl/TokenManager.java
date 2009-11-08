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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.paxle.core.doc.ICommand;
import org.paxle.filter.wordlistcreator.ITokenManager;
import org.paxle.filter.wordlistcreator.ITokenizer;
import org.paxle.filter.wordlistcreator.impl.tokenizer.RegExpTokenizer;
import org.paxle.filter.wordlistcreator.impl.wordlist.StorageManager;

@Component(immediate=true, metatype=true, label="TokenManager")
@Property(name="dataDir", value="wordlistCreator")
@Service(ITokenManager.class)
public class TokenManager implements ITokenManager {

	private StorageManager storage = null;
	private ITokenizer tokenizer = new RegExpTokenizer();

	protected void activate(Map<String, Object> props) throws IOException {
		File dataDir = new File(System.getProperty("paxle.data"));
		if (props != null) {
			if (props.get("dataDir") != null)
				dataDir = new File(System.getProperty("paxle.data") + File.separatorChar + props.get("dataDir"));
		}
		this.storage = new StorageManager(dataDir);
	}

	public File exportAllTokens() {
		try {
			this.storage.getTokenList();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	synchronized public void registerContent(ICommand command) throws IOException {
		if (command.getParserDocument() == null) return;

		//Token cache
		HashMap<String, Integer> tc = getNewTokenCache();

		this.tokenizer.setContent(command.getParserDocument().getTextAsReader());

		while (this.tokenizer.hasNext()) {
			String token = this.tokenizer.next();
			if (tc.get(token) != null) {
				tc.put(token, tc.get(token)+1);
			} else {
				tc.put(token, 1);
			}
			if (tc.size() > 500) {
				this.storage.storeToken(tc);
				tc = getNewTokenCache();
			}
		}
		//This command is finished, dump cache
		this.storage.storeToken(tc);
	}
	
	private HashMap<String, Integer> getNewTokenCache() {
		return new HashMap<String, Integer>(200);
	}

}
