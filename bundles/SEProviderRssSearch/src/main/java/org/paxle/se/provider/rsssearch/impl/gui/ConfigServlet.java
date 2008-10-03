package org.paxle.se.provider.rsssearch.impl.gui;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.paxle.gui.ALayoutServlet;
import org.paxle.se.provider.rsssearch.impl.RssSearchProvider;

public class ConfigServlet extends ALayoutServlet {

	private static final long serialVersionUID = 1L;

	public ConfigServlet() {
	}

	@Override
	public Template handleRequest( HttpServletRequest request,
			HttpServletResponse response,
			Context context ) {
		Template template = null;
		try {
			template = this.getTemplate("/resources/templates/config.vm");
			if(request.getMethod().equals("POST")){
				String[] new_urls=request.getParameter("urls").split("\n");
				ArrayList<String> list=new ArrayList<String>();
				for(int i=0;i<new_urls.length;i++)
					if(!new_urls[i].equals(""))
						list.add(new_urls[i]);
				RssSearchProvider.setUrls(list);
				RssSearchProvider.registerSearchers(list);
			}
			List<String> urls = RssSearchProvider.getUrls();
			context.put("urls", urls);
				
		} catch( Exception e ) {
			logger.warn("Unexpected Error:", e);
		}
		return template;
	}
}
