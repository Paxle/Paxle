package org.paxle.se.provider.rsssearch.impl.gui;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.paxle.gui.ALayoutServlet;
import org.paxle.se.provider.rsssearch.impl.RssSearchProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;

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
				if(!request.getParameter("opensearchurl").equals("")){
					DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
					DocumentBuilder db=dbf.newDocumentBuilder();
					HttpMethod hm=new GetMethod(request.getParameter("opensearchurl"));
					HttpClient hc=new HttpClient();
					int status = hc.executeMethod(hm);
					if(status==200){
						Document d=db.parse(hm.getResponseBodyAsStream());
						// <Url type="application/rss+xml" 
						// template="http://example.com/?q={searchTerms}&amp;pw={startPage?}&amp;format=rss"/>

						NodeList elements= d.getElementsByTagName("Url");
						for(int i=0;i<elements.getLength();i++){
							NamedNodeMap nnm=elements.item(i).getAttributes();		
							if(nnm.getNamedItem("type")!=null && nnm.getNamedItem("type").toString().toLowerCase().equals("application/rss+xml")){
								System.out.println(nnm.toString());
								String urltemplate=elements.item(i).getAttributes().getNamedItem("template").toString();
								urltemplate.replace("{startPage?}", "1");
								urltemplate.replace("{searchTerms}", "%s");
								ArrayList<String> urls=RssSearchProvider.getUrls();
								urls.add(urltemplate);
								RssSearchProvider.setUrls(urls);
								break;
							}
							
						}
						
					}
				} else {
					String[] new_urls = request.getParameter("urls")
							.split("\n");
					ArrayList<String> list = new ArrayList<String>();
					for (int i = 0; i < new_urls.length; i++)
						if (!new_urls[i].equals(""))
							list.add(new_urls[i]);
					RssSearchProvider.setUrls(list);
					RssSearchProvider.registerSearchers(list);
				}
			}
			List<String> urls = RssSearchProvider.getUrls();
			context.put("urls", urls);
				
		} catch( Exception e ) {
			logger.warn("Unexpected Error:", e);
		}
		return template;
	}
}
