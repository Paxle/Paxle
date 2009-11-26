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
package org.paxle.parser.tika.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.apache.tika.metadata.Metadata;
import org.paxle.core.doc.IParserDocument;

public class ParserMetaData extends Metadata {
	final IParserDocument parserDoc;
	
	public ParserMetaData(IParserDocument parserDoc) {
		super();
		this.parserDoc = parserDoc;
	}
	
	@Override
	public void set(String name, String value) {
		super.set(name, value);
		
		if (name != null && value != null) {
			if (name.equals(Metadata.TITLE)) {
				this.parserDoc.setTitle(value);
			} else if (name.equals(Metadata.AUTHOR)) {
				this.parserDoc.setAuthor(value);
			} else if (name.equals(Metadata.KEYWORDS)) {
				String[] keywordArray = value.split("[,;\\s]");
				if (keywordArray != null && keywordArray.length > 0) {
					this.parserDoc.setKeywords(Arrays.asList(keywordArray));
				}
			} else if (name.equals(Metadata.LAST_MODIFIED)) {
				try {
					final SimpleDateFormat formatter = new SimpleDateFormat();
					final Date lastMod = formatter.parse(value);
					this.parserDoc.setLastChanged(lastMod);
				} catch (ParseException e) {
					// ignore this
				}
			} else if (name.equals(Metadata.SUBJECT)) {
				this.parserDoc.setSummary(value);
			}
		}
	}
}
