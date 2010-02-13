/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2010 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.desktop.impl.dialogues.cconsole;

import org.paxle.desktop.impl.Messages;

enum Columns {
	// ICommand specific
	COMPONENT, DEPTH, LOCATION, RESULT, RESULTTEXT, LAST_FILTER,
	// ICrawlerDocument specific
	C_CHARSET, C_DATE, C_LANGS, C_LASTMOD, C_MD5, C_MIMETYPE, C_SIZE, C_STATUS, C_STATUSTEXT,
	// IParserDocument specific
	P_AUTHOR, P_CHARSET, P_HEADLINES, P_IMAGES, P_KEYWORDS, P_LANGUAGES, P_LASTMOD, P_LINKS,
	P_MIMETYPE, P_STATUS, P_STATUSTEXT, P_SUBDOCS, P_SUMMARY, P_TITLE;
	
	public String getL10n() {
		return Messages.getString("crawlingConsole.col." + name().toLowerCase());
	}
}