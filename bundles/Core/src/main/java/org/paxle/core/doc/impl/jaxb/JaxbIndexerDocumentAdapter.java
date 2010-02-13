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

package org.paxle.core.doc.impl.jaxb;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.paxle.core.doc.IIndexerDocument;

public class JaxbIndexerDocumentAdapter extends XmlAdapter<IIndexerListEntry[], IIndexerDocument[]> {

	@Override
	public IIndexerListEntry[] marshal(IIndexerDocument[] iDocs) throws Exception {
		if (iDocs==null) return null;
		
		final IIndexerListEntry[] iDocEntries = new IIndexerListEntry[iDocs.length];
		for (int i=0; i < iDocs.length; i++) {
			final IIndexerListEntry entry = new IIndexerListEntry();
			entry.setIndexerDocument(iDocs[i]);
			iDocEntries[i] = entry;
		}
		return iDocEntries;
	}

	@Override
	public IIndexerDocument[] unmarshal(IIndexerListEntry[] iDocEntries) throws Exception {
		if (iDocEntries==null) return null;
		
		final IIndexerDocument[] iDocs = new IIndexerDocument[iDocEntries.length];
		for (int i=0; i < iDocEntries.length; i++) {
			iDocs[i] = iDocEntries[i].getIndexerDocument();
		}
		return iDocs;
	}
}

class IIndexerListEntry {
	private IIndexerDocument indexerDocument;

    @XmlElement
    @XmlJavaTypeAdapter(JaxbDocAdapter.class)
	public IIndexerDocument getIndexerDocument() {
		return indexerDocument;
	}

	public void setIndexerDocument(IIndexerDocument indexerDocument) {
		this.indexerDocument = indexerDocument;
	}
}
