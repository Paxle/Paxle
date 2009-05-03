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
package org.paxle.se.index.lucene.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.WordlistLoader;

public class MultiFormatWordlistLoader extends WordlistLoader {
	
	public static Set<String> getSnowballSet(final File stopwords) throws IOException {
		return getSnowballSet(new BufferedReader(new FileReader(stopwords)));
	}

	public static Set<String> getSnowballSet(final Reader reader) throws IOException {
		final BufferedReader br = (reader instanceof BufferedReader) ? (BufferedReader)reader : new BufferedReader(reader);
		final Set<String> r = new HashSet<String>();
		try {
			String line;
			while ((line = br.readLine()) != null) {
				final int comment = line.indexOf('|');
				if (comment > -1)
					line = line.substring(0, comment);
				line = line.trim();
				if (line.length() > 0)
					r.add(line);
			}
		} finally { br.close(); }
		return r;
	}
}
