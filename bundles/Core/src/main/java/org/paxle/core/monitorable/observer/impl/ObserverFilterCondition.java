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
package org.paxle.core.monitorable.observer.impl;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.framework.Filter;
import org.paxle.core.monitorable.observer.IObserverCondition;

public class ObserverFilterCondition implements IObserverCondition {
	private final Filter filter;
	
	public ObserverFilterCondition(Filter filter) {
		this.filter = filter;
	}
	
	public boolean match(Dictionary dictionary) {
		return this.filter.match(dictionary);
	}

	public Filter getFilter() {
		return this.filter;
	}

	public List<String> getMonitorableVariableIDs() {
		ArrayList<String> variableIDs = new ArrayList<String>();

		Pattern pattern = Pattern.compile("\\((\\s*[^=><~()]*)\\s*(=|<|<=|>|>=|~=)\\s*([^=><~()]*)\\s*\\)");
		Matcher matcher = pattern.matcher(this.filter.toString());

		while (matcher.find()) {
			String fullPath = matcher.group(1).trim();
			variableIDs.add(fullPath);
		}	
			
		return variableIDs;
	}
	
	@Override
	public String toString() {
		return "filter[" + this.filter + "]";
	}
}
