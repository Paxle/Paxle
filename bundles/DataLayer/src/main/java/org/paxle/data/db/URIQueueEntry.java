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
package org.paxle.data.db;

import java.net.URI;
import java.util.LinkedList;

public class URIQueueEntry {
	private final int profileID;
	private final int commandDepth;
	private final URI rootUri;
	private final LinkedList<URI> refs;
	private int known;

	public URIQueueEntry(URI rootUri, int profileID, int commandDepth, LinkedList<URI> refs) {
		this.rootUri = rootUri;
		this.profileID = profileID;
		this.commandDepth = commandDepth;
		this.refs = refs;
	}
	
	public void setKnown(int known) {
		this.known = known;
	}
	
	public int getKnown() {
		return this.known;
	}
	
	public int getProfileID() {
		return this.profileID;
	}

	public int getDepth() {
		return this.commandDepth;
	}

	public URI getRootURI() {
		return this.rootUri;
	}

	public LinkedList<URI> getReferences() {
		return this.refs;
	}
}
