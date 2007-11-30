package org.paxle.icon.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class IconServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest req, HttpServletResponse rsp) throws IOException {
		String url = req.getParameter("url");
		if (url == null || url.length() == 0) {
			rsp.setStatus(404);
			return;
		}
		
		IconData icon = IconTool.getIcon(new URL(url));
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
