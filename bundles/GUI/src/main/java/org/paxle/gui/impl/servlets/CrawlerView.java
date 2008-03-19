
package org.paxle.gui.impl.servlets;

import java.io.BufferedReader;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.paxle.core.data.IDataSink;
import org.paxle.core.queue.Command;
import org.paxle.core.queue.ICommand;
import org.paxle.gui.ALayoutServlet;
import org.paxle.gui.impl.ServiceManager;

public class CrawlerView extends ALayoutServlet {
	
	private static final long serialVersionUID = 1L;
	
	private class UrlTank {
		
		private final IDataSink<ICommand> crawlerSink;
		private final Object robotsManager;
		private Method isDisallowed = null;
		private HashMap<String,String> errorUrls = null;
		
		@SuppressWarnings("unchecked")
		public UrlTank(final ServiceManager manager) throws Exception {
			Object[] services = manager.getServices(IDataSink.class.getName(),
					String.format("(%s=org.paxle.crawler.sink)", IDataSink.PROP_DATASINK_ID));
			if (services == null || services.length == 0 || services[0] == null)
				throw new Exception("crawler-sink not available");
			crawlerSink = (IDataSink<ICommand>)services[0];
			
			robotsManager = manager.getService("org.paxle.filter.robots.IRobotsTxtManager");
			if (robotsManager != null)
				isDisallowed = robotsManager.getClass().getMethod("isDisallowed", String.class);
		}
		
		public void putUrl2Crawl(final String location) throws Exception {
			final String url = location.trim();
			if (url.length() == 0) {
				putError(location, "URL '" + location + "' is not valid");
				return;
			}
			
			if (robotsManager != null && isDisallowed != null) {
				final Object result = isDisallowed.invoke(robotsManager, url);
				if (result != null && ((Boolean)result).booleanValue()) {
					final String msg = "Not allowed to begin crawling for the URL is blocked by robots.txt";
					logger.info(msg);
					putError(location, msg);
					return;
				}
			}
			
			logger.info("Initiated crawl of URL '" + url + "'");
			crawlerSink.putData(Command.createCommand(url));
		}
		
		private void putError(final String url, final String err) {
			if (errorUrls == null)
				errorUrls = new HashMap<String,String>();
			errorUrls.put(url, err);
		}
		
		public Map<String,String> getErrorUrls() {
			return errorUrls;
		}
	}
	
	public Template handleRequest( 
			HttpServletRequest request,
			HttpServletResponse response,
			Context context 
	) {
		
		Template template = null;
		
		try {
			String url;
			if ((url = request.getParameter("startURL")) != null) {
				// startURL denotes a single URL to crawl entered in an input-field
				final UrlTank tank = new UrlTank((ServiceManager)context.get(SERVICE_MANAGER));
				tank.putUrl2Crawl(url);
				context.put("errorUrls", tank.getErrorUrls());
				
			} else if ((url = request.getParameter("startURL2")) != null) {
				// startURL2 contains a whole bunch of URLs to crawl entered in a textarea
				final UrlTank tank = new UrlTank((ServiceManager)context.get(SERVICE_MANAGER));
				final BufferedReader startURLs = new BufferedReader(new StringReader(url));
				String line;
				while ((line = startURLs.readLine()) != null)
					tank.putUrl2Crawl(line);
				context.put("errorUrls", tank.getErrorUrls());
			}
			
			/*
			 * Setting template parameters
			 */
			template = this.getTemplate("/resources/templates/CrawlerView.vm");
		} catch( Exception e ) {
			logger.error("Error processing request: " + e.getMessage(), e);
		}
		
		return template;
	}
}
