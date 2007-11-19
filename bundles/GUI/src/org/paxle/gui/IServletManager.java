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
}
