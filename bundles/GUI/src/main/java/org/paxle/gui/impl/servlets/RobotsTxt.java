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

package org.paxle.gui.impl.servlets;

import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.paxle.gui.ALayoutServlet;

@Component(metatype=false, immediate=true)
@Service(Servlet.class)
@Properties({
	@Property(name="org.paxle.servlet.path", value="/robots.txt"),
	@Property(name="org.paxle.servlet.doUserAuth", boolValue=false)
})
public class RobotsTxt extends ALayoutServlet {

	private static final long serialVersionUID = 1L;
	
	/** The configuration data for this class */
	private Map<String, Object> config = null;
	
	private static final String PID = RobotsTxt.class.getName();
	
	/** The text of the robots.txt file */
	public static final String ROBOTSTXT = PID + '.' + "robotstxt-txt";
	
	protected void activate(Map<String, Object> props) {
		this.config = props;
	}
	
	@Override
	protected void fillContext(Context context, HttpServletRequest request) {
		context.put("layout", "plain.vm");
		if (config.get(ROBOTSTXT) != null) {
			context.put("robotstxt", config.get(ROBOTSTXT));
		} else {
			context.put("robotstxt", "User-agent: *\r\nDisallow: /");
		}
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