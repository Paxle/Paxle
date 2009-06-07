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
package org.paxle.data.txt.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;

import org.paxle.core.doc.Command;
import org.paxle.core.doc.ICommand;
import org.paxle.data.impl.ACommandReader;

public class TextCommandReader extends ACommandReader {
	
	public TextCommandReader(InputStream inputStream) {
		super(inputStream);
	}
	
	@Override
	protected void read(Reader reader) throws IOException {
		if (reader == null) throw new NullPointerException("The reader is null");
		
		BufferedReader bufferedReader = new BufferedReader(reader);
		String line = null;
		while((line = bufferedReader.readLine())!=null) {
			line = line.trim();
			if (line.length() == 0) continue;
			else if (line.startsWith("#")) continue;
			try {
				ICommand cmd = Command.createCommand(new URI(line));
				this.enqueue(cmd);
			} catch (URISyntaxException e) {
				throw new IOException(String.format("location '%s' not a valid URI: %s", line, e.getMessage()));
			}
		}
	}	
}
