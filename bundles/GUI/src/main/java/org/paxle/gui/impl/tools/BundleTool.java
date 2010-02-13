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

package org.paxle.gui.impl.tools;

import java.util.Map;

import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.ValidScope;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

@DefaultKey(BundleTool.TOOL_NAME)
@ValidScope(Scope.REQUEST)
public class BundleTool extends PaxleLocaleConfig {
	public static final String TOOL_NAME = "bundleTool";
	
	@Override
	public void configure(@SuppressWarnings("unchecked") Map props) {
		super.configure(props);
	}
	
	public Bundle get(Integer bundleID) {
		// getting a reference to the requested bundle
		final Bundle bundle = this.context.getBundle(bundleID.longValue());
		if (bundle != null) return bundle;
		
		this.logger.warn(String.format(
				"No bundle found for ID '%d'.",
				bundleID
		));
		return null;
	}	
	
	public Bundle getByPID(String servicePID) {
		ServiceReference[] refs = null;
		try {
			refs = context.getAllServiceReferences(null, String.format("(%s=%s)",Constants.SERVICE_PID,servicePID));
			if (refs != null && refs.length > 0) {
				ServiceReference serviceRef = refs[0];
				return serviceRef.getBundle();
			}
			return null;
		} catch (Exception e) {
			this.logger.warn(String.format("No bundle found providing service '%s'.", servicePID));
		} finally {
			if (refs != null) for(ServiceReference ref : refs) context.ungetService(ref);
		}
		return null;
	}
}
