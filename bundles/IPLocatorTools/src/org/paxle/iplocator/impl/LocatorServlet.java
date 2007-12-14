package org.paxle.iplocator.impl;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LocatorServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

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
