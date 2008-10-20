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

package org.paxle.util.ac;

public class SearchResult<E> {
	
	private final int start;
	private final int end;
	private final E value;
	
	public SearchResult(final int start, final int end, final E value) {
		this.start = start;
		this.end = end;
		this.value = value;
	}
	
	public int getMatchBegin() {
		return start;
	}
	
	public int getMatchEnd() {
		return end;
	}
	
	public E getValue() {
		return value;
	}
}
