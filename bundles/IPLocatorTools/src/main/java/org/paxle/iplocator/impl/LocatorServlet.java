/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 * 
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.iplocator.impl;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LocatorServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse rsp) throws IOException {
		String hostNameIP = req.getParameter("ip");
		if (hostNameIP == null || hostNameIP.length() == 0) {
			rsp.setStatus(404);
			return;
		}
		
		IconData icon = LocatorTool.getIcon(hostNameIP);
		if (icon != null) {
			rsp.setContentType(icon.mimeType);
			
			OutputStream out = rsp.getOutputStream();
			out.write(icon.data);
			out.close();
			return;
		} else {
			rsp.setStatus(404);
			return;
		}	
	}
}
