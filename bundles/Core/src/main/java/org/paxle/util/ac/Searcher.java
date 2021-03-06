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

package org.paxle.util.ac;

import java.util.Iterator;
import java.util.NoSuchElementException;

// package private
class Searcher<E> implements Iterable<SearchResult<E>> {
	
	private final byte[] search;
	private final int off;
	private final int to;
	private final AhoCorasick<E> ac;
	
	Searcher(final byte[] search, final int off, final int to, final AhoCorasick<E> ac) {
		this.search = search;
		this.off = off;
		this.to = to;
		this.ac = ac;
	}
	
	public Iterator<SearchResult<E>> iterator() {
		return new Iterator<SearchResult<E>>() {
			
			private AhoCorasick<E>.SearchNodeResult isr = ac.matchingNode(search, ac.root, off, to);
			private int idx = 0;
			private SearchResult<E> next = next0();
			
			private SearchResult<E> next0() {
				while (isr != null) {
					if (idx < isr.size())
						return isr.result(idx++);
					idx = 0;
					isr = ac.matchingNode(search, isr.node, isr.idx + 1, to); 
				}
				return null;
			}
			
			public boolean hasNext() {
				return next != null;
			}
			
			public SearchResult<E> next() {
				if (next == null)
					throw new NoSuchElementException();
				final SearchResult<E> c = next;
				next = next0();
				return c;
			}
			
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
