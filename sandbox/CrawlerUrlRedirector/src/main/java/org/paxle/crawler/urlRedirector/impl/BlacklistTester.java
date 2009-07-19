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
package org.paxle.crawler.urlRedirector.impl;

import java.net.URI;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.paxle.filter.blacklist.IBlacklistManager;
import org.paxle.filter.blacklist.IFilterResult;

@Component(immediate=true, metatype=false)
@Service(IUrlTester.class)
@Property(name = IUrlTester.TYPE, value = "BlacklistTester")
public class BlacklistTester extends AUrlTester {

	@Reference
	protected IBlacklistManager manager;
	
	public boolean reject(URI requestUri) {
		// test URI against blacklist
		final IFilterResult result = manager.isListed(requestUri.toString());		
		if (result.hasStatus(IFilterResult.LOCATION_REJECTED)) {
			this.logger.info(String.format(
					"Rejecting URL '%s' due to matching blacklist pattern: %s",
					requestUri,
					result.getRejectPattern()
			));					
			return true;			
		}
		return false;
	}

}
