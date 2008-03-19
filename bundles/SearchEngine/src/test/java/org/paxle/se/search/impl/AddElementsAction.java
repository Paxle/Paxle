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