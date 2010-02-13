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

package org.paxle.gui;

import java.util.Map;

import javax.servlet.Servlet;

public interface IServletManager {
	/**
	 * @return a map containing all currently known servlets.
	 */
	public Map<String, Servlet> getServlets();
	
	/**
	 * @return a map containing all currenty known resources.
	 */
	public Map<String, String> getResources();
	
	/**
	 * @return <code>true</code> if a servlet was registered using the specified path
	 */
	public boolean hasServlet(String path);
	
	/**
	 * @return the servlet-path-prefix + the given servlet-alias
	 */
	public String getFullAlias(String alias);
	public String getFullAlias(String prefix, String alias);
	public String getFullServletPath(String servletPID);
}
