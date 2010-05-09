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

package org.paxle.console.impl.gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.view.VelocityLayoutServlet;
import org.eclipse.osgi.framework.console.ConsoleSession;
import org.osgi.service.component.ComponentContext;

//@Component(metatype=false, immediate=true,
//		label="Console Servlet"
//)
//@Service(Servlet.class)
//@Properties({
//	@Property(name="org.paxle.servlet.path", value="/console"),
//	@Property(name="org.paxle.servlet.doUserAuth", boolValue=false)
//})
public class ConsoleServlet extends VelocityLayoutServlet implements Servlet {

	private static final long serialVersionUID = 1L;
	
    /**
     * Logger
     */
    protected Log logger = LogFactory.getLog(this.getClass());		

	protected void activate(ComponentContext context) throws IOException {

	}
    
	@Override
	protected void fillContext(Context context, HttpServletRequest request) {
		try {
		
		} catch (Exception e) {
			this.logger.error(e);
		}
	}
	
	/**
	 * Choosing the template to use 
	 */
	@Override
	protected Template getTemplate(HttpServletRequest request, HttpServletResponse response) {
		return this.getTemplate("/resources/templates/Console.vm");
	}	
}
