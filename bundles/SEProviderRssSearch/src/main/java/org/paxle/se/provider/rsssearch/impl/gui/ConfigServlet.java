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
package org.paxle.se.provider.rsssearch.impl.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.paxle.gui.ALayoutServlet;
import org.paxle.se.provider.rsssearch.IRssSearchProviderManager;
import org.paxle.se.provider.rsssearch.impl.RssSearchProvider;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.htmlparser.Parser;
import org.htmlparser.PrototypicalNodeFactory;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;

/**
 * @scr.component immediate="true" 
 * 				  label="RSS Search Servlet"
 * 				  description="A Servlet to configure your RSS resources"
 * @scr.service interface="javax.servlet.Servlet"
 * @scr.property name="org.paxle.servlet.path" value="/rsssearchconfig"
 * @scr.property name="org.paxle.servlet.doUserAuth" value="true" type="Boolean"
 * @scr.property name="org.paxle.servlet.menu" value="%menu.administration/%menu.bundles/%configServlet.menu"
 * @scr.property name="org.paxle.servlet.menu.icon" value="/resources/icons/folder_feed.png"
 */
public class ConfigServlet extends ALayoutServlet {
	private static final long serialVersionUID = 1L;
	
	/** 
	 * @scr.reference
	 */
	protected IRssSearchProviderManager pManager;

	@Override
	public Template handleRequest(HttpServletRequest request, HttpServletResponse response, Context context) {
		Template template = null;
		try {
			template = this.getTemplate("/resources/templates/config.vm");
			if (request.getMethod().equals("POST")) {
				if (request.getParameter("opensearchurl") != null) {
					String url=request.getParameter("opensearchurl");
					this.addRssUrlFromOpensearchXMLUrl(url);
				} else if (request.getParameter("opensearchhtmlurl") != null) {
					HttpMethod hm = null;
					try {
						hm = new GetMethod(request.getParameter("opensearchhtmlurl"));
						HttpClient hc = new HttpClient();
						int status = hc.executeMethod(hm);
						if (status == 200) {
							Page page=new Page(hm.getResponseBodyAsString());
							page.setUrl(request.getParameter("opensearchhtmlurl"));
							Parser parser = new Parser(new Lexer(page));
							parser.setNodeFactory(new PrototypicalNodeFactory());
							OpenSearchLinkCollector oslc=new OpenSearchLinkCollector();
							parser.visitAllNodesWith(oslc);
							if(oslc.found()){
								this.addRssUrlFromOpensearchXMLUrl(oslc.getURL());
							}
							page.close();
						}
					} finally {
						if (hm != null) hm.releaseConnection();
					}
				}
			} 
			
			if (request.getParameter("urls") != null) {				
				String[] new_urls = request.getParameter("urls").split("\n");
				ArrayList<String> list = new ArrayList<String>();
				for (int i = 0; i < new_urls.length; i++)
					if (!new_urls[i].equals(""))
						list.add(new_urls[i].trim());
				this.pManager.setUrls(list);
				this.pManager.registerSearchers(list);
			}
			
			List<String> urls = this.pManager.getUrls();
			context.put("urls", urls);

		} catch (Exception e) {
			logger.warn("Unexpected Error:", e);
		}
		return template;
	}

	/**
	 * load the opensearch-xml url, extract the searchurl from the xml and replace {searchTerm} by %s
	 * the result is added to the {@link RssSearchProvider} list and all {@link RssSearchProvider}s will be reloaded
	 * @param url
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws HttpException
	 * @throws SAXException
	 */
	private void addRssUrlFromOpensearchXMLUrl(String url) throws ParserConfigurationException, IOException, HttpException, SAXException {
		HttpMethod hm = null;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			hm = new GetMethod(url);
			HttpClient hc = new HttpClient();
			int status = hc.executeMethod(hm);
			if (status == 200) {
				Document d = db.parse(hm.getResponseBodyAsStream());
				// <Url type="application/rss+xml"
				// template="http://example.com/?q={searchTerms}&amp;pw={startPage?}&amp;format=rss"/>

				NodeList elements = d.getElementsByTagName("Url");
				for (int i = 0; i < elements.getLength(); i++) {
					NamedNodeMap nnm = elements.item(i).getAttributes();
					Node typeNode = nnm.getNamedItem("type");
					String type = typeNode == null 
							    ? null
							    : typeNode.getNodeValue().toLowerCase();
					
					if (type.equals("application/rss+xml")) {
						Node templateNode = elements.item(i).getAttributes().getNamedItem("template");
						String urltemplate = templateNode.getNodeValue();
						urltemplate = urltemplate.replaceAll("\\{startPage\\?\\}", "1");
						urltemplate = urltemplate.replaceAll("\\{searchTerms\\}", "%s");
						
						ArrayList<String> urls = this.pManager.getUrls();
						urls.add(urltemplate);
						this.pManager.setUrls(urls);
						break;
					}

				}
			}
			
		} catch (IllegalArgumentException e) {// InputStream
			// cannot be null
			logger.warn("Problem adding opensearch xml",e);
		} finally {
			if (hm != null) hm.releaseConnection();
		}
	}
}
