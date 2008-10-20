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

package org.paxle.se.search.impl;

import java.util.Collection;

import org.hamcrest.Description;
import org.jmock.api.Action;
import org.jmock.api.Invocation;

/**
 * Helper action for JMock which appends elements
 * to a collection
 */
public class AddElementsAction<T> implements Action {
    private Collection<T> elements;
    int paramIdx = 0;
    
    /**
     * @param elements the elements that should be appended
     * @param paramIdx the position within the input-parameter arguments
     */
    public AddElementsAction(Collection<T> elements, int paramIdx) {
        this.elements = elements;
        this.paramIdx = paramIdx;
    }
    
    @SuppressWarnings("unchecked")
	public Object invoke(Invocation invocation) throws Throwable {
        ((Collection<T>)invocation.getParameter(this.paramIdx)).addAll(elements);
        return null;
    }

	public void describeTo(Description description) {
        description.appendText("adds ")
        .appendValueList("", ", ", "", elements)
        .appendText(" to a collection");		
	}
}