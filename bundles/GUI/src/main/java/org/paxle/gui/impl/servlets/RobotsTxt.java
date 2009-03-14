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
package org.paxle.gui.impl.servlets;

import java.util.Dictionary;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.osgi.service.component.ComponentContext;
import org.paxle.gui.ALayoutServlet;

/**
 * @scr.component immediate="true" metatype="false" name="org.paxle.gui.impl.servlets.RobotsTxt"
 * @scr.service interface="javax.servlet.Servlet"
 * @scr.property name="path" value="/robots.txt"
 * @scr.property name="doUserAuth" value="false" type="Boolean"
 * @scr.property name="org.paxle.gui.impl.servlets.RobotsTxt.robotstxt-txt" value="User-agent: *\u000ADisallow: /"
 */
public class RobotsTxt extends ALayoutServlet {

	private static final long serialVersionUID = 1L;
	
	/** The configuration data for this class */
	private Dictionary<String, Object> config = null;
	
	private static final String PID = RobotsTxt.class.getName();
	
	/** The text of the robots.txt file */
	public static final String ROBOTSTXT = PID + '.' + "robotstxt-txt";
	
	@SuppressWarnings("unchecked")
	protected void activate(ComponentContext context) {
		this.config = context.getProperties();
	}
	
	@Override
	protected void fillContext(Context context, HttpServletRequest request) {
		context.put("layout", "plain.vm");
		context.put("robotstxt", config.get(ROBOTSTXT));
	}

	/**
	 * Choosing the template to use 
	 */
	@Override
	protected Template getTemplate(HttpServletRequest request, HttpServletResponse response) {
		return this.getTemplate("/resources/templates/RobotsTxt.vm");
	}
	
	@Override
	protected void setContentType(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("text/plain; charset=UTF-8");
	}
}