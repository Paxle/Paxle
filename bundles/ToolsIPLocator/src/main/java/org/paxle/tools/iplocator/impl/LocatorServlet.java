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
package org.paxle.tools.iplocator.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.paxle.core.io.IIOTools;

@Component(metatype=false, immediate=true)
@Service(Servlet.class)
@Properties({
	@Property(name="org.paxle.servlet.path", value="/ipLocator"),
	@Property(name="org.paxle.servlet.doUserAuth", boolValue=false)
})
public class LocatorServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Reference
	protected LocatorTool locatorTool;
	
	@Reference
	protected IIOTools iotools;
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse rsp) throws IOException {
		String hostNameIP = null;
		
		if (req.getParameter("url") != null) {
			URI url = URI.create(req.getParameter("url"));
			hostNameIP = url.getHost();
		} else if (req.getParameter("ip") != null) {
			hostNameIP = req.getParameter("ip");
		}		
		
		if (hostNameIP == null || hostNameIP.length() == 0) {
			rsp.setStatus(404);
			
			PrintWriter writer = new PrintWriter(rsp.getOutputStream());
			writer.write("<html><body>");
			writer.write("<h1>IP-Locator Servlet</h1>");
			writer.write("<form action=''>");
			writer.write("Hostname: <input type='text' name='ip' value='' />");
			writer.write("<input type='submit' name='locate' value='locate'/>");
			writer.write("</form>");
			writer.write("</body></html>");
			writer.close();
			
			return;
		}
		
		InputStream icon = locatorTool.getIcon(hostNameIP);
		if (icon != null) {
			OutputStream out = rsp.getOutputStream();
			rsp.setContentType("image/png");
			iotools.copy(icon, out);
			out.close();
			return;
		} else {
			rsp.setStatus(404);
			return;
		}	
	}
}
