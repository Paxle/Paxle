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
}
