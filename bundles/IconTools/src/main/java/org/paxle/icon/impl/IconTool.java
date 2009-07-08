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
package org.paxle.icon.impl;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.text.html.parser.ParserDelegator;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.paxle.core.io.IOTools;

public class IconTool {
	/**
	 * Connection manager used for http connection pooling
	 */
	private static MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();

	/**
	 * A map containing a mapping between <code>mime-types</code> and
	 * <code>file-names</code>, e.g.<br />
	 * <pre>application/pdf=pdf.png</pre>
	 * 
	 * This map is loaded from the properties file <code>iconmap.properties</code>.
	 */
	static Properties iconMap = new Properties();

	/**
	 * default icon for unknown mime-types
	 */
	static IconData defaultIcon = null;

	/**
	 * default icon for html pages without favicon
	 */
	static IconData defaultHtmlIcon = null;

	/*
	 * Initialize the icon-map and load the default icons
	 */
	static {
		// configure connection manager
		HttpConnectionManagerParams cmParams = connectionManager.getParams();
		cmParams.setDefaultMaxConnectionsPerHost(10);
		cmParams.setConnectionTimeout(15000);
		cmParams.setSoTimeout(15000);
		
		/* configure mime-type to resource-file map */
		InputStream mapStream = null;
		try {
			mapStream = IconTool.class.getResourceAsStream("/resources/iconmap.properties");
			iconMap.load(mapStream);
			mapStream.close();

			// load the default icon
			byte[] data = readFileIconPng("unknown");
			if (data != null) defaultIcon = new IconData(data);

			data = readFileIconPng("text/html");
			if (data != null) defaultHtmlIcon = new IconData(data);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (mapStream != null) try { mapStream.close(); } catch (Exception e) {/* ignore this */}
		}
	}

	/**
	 * http client class
	 */
	private static HttpClient httpClient = new HttpClient(connectionManager); 	

	/**
	 * Loads the favicon for the given http-resource. If no favicon can be found,
	 * an icon is loaded using the icon-map
	 * 
	 * @param url the location of the resource, for which the favicon should be loaded
	 * @return the loaded image data.
	 */
	public static IconData getIcon(URL url) {
		return getIcon(url,0);
	}

	/**
	 * @param url the location of the resource, for which the favicon should be loaded
	 * @param depth protection against endless loops, e.g. if the icon link returned by
	 *        a page is another html site.
	 * @return the loaded image data.
	 */
	private static IconData getIcon(URL url, int depth) {	
		if (url == null) return defaultIcon;
		else if (depth > 20) return defaultIcon;

		GetMethod method = null;
		try {
			/*
			 * Handling of !http URLs
			 */
			if (!url.getProtocol().equals("http")) {				
				String expectedType = URLConnection.guessContentTypeFromName(url.getFile());
				byte[] data = readFileIconPng(expectedType);
				return data == null ? defaultIcon : new IconData(data);
			}

			/*
			 * Handling of http URLs 
			 */
			method = new GetMethod(url.toExternalForm());
			int status = httpClient.executeMethod(method);
			if (status != 200) {
				// TODO: logging
				return defaultIcon;
			}

			// getting the mimetype and charset
			String contentMimeType = null;
			Header contentTypeHeader = method.getResponseHeader("Content-Type");
			if (contentTypeHeader != null) {
				contentMimeType = contentTypeHeader.getValue();
				int idx = contentMimeType.indexOf(";");
				if (idx != -1) contentMimeType = contentMimeType.substring(0,idx);
			}

			byte[] body = null;
			String iconType = "image/png";
			if (contentMimeType.equals("text/html")) {
				if (url.getPath().equals("/favicon.ico")) {
					// the website returned the favicon.ico als html!
					return defaultHtmlIcon;
				} 

				/* Download content until we get the
				 * <LINK REL="SHORTCUT ICON" HREF="icons/matex.ico">
				 * header
				 */ 
				InputStream bodyStream = method.getResponseBodyAsStream();
				HtmlReader reader = new HtmlReader(bodyStream);
				HtmlParserCallback theParser = new HtmlParserCallback(reader);
				new ParserDelegator().parse(reader, theParser, true);

				// get the parsed favicon url
				String urlString = theParser.getFaviconUrl();
				URL faviconURL = null;
				if (urlString == null || urlString.length() == 0) {
					// website has no special icon, try to fetch the global favicon
					faviconURL = new URL(url,"/favicon.ico");
				} else {			
					faviconURL = new URL(url,urlString);				
				}
				IconData faviconData = getIcon(faviconURL,++depth);
				return (faviconData == null || faviconData == defaultIcon) ? defaultHtmlIcon : faviconData; 
			} else if (contentMimeType.equals("image/x-icon") || 
					contentMimeType.equals("image/vnd.microsoft.icon")) {
				byte[] data = method.getResponseBody();
				Image icon = FaviconReader.readIcoImage(data);
				if (icon != null) body = IconTool.toBytes(icon);
			} else if (contentMimeType.startsWith("image/")) {
				body = method.getResponseBody();
				iconType = contentMimeType;
			} else {
				InputStream bodyStream = null;
				ByteArrayOutputStream bout = null;
				try {
					bodyStream = method.getResponseBodyAsStream();
					bout = new ByteArrayOutputStream();
					IOTools.copy(bodyStream, bout, 4);

					byte[] bodyPrefix = bout.toByteArray();
					if ((bodyPrefix != null) && (bodyPrefix.length >= 4) && (bodyPrefix[0] == 0) && (bodyPrefix[1] == 0) && (bodyPrefix[2] == 1) && (bodyPrefix[3] == 0)) {
						IOTools.copy(bodyStream, bout);					
						byte[] data = bout.toByteArray();

						// trying to read icon
						Image icon = FaviconReader.readIcoImage(data);					
						if (icon != null) body = IconTool.toBytes(icon);
					} else {
						body = readFileIconPng(contentMimeType);
						iconType = "image/png";
					}
				} catch (IOException e) {
					// TODO: logging
				} finally {
					if (bodyStream != null) bodyStream.close();
					if (bout != null) bout.close();
				}
			}

			return (body == null) ? defaultIcon : new IconData(iconType,body);
		} catch (Exception e) {
			// TODO: logging
			e.printStackTrace();
			return defaultIcon;
		} finally {
			if (method != null) method.releaseConnection();
		}
	}

	public static byte[] readFileIconPng(String contentType) throws IOException {
		InputStream iconInput = null;
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try {
			String iconFileName = null; 
			if (contentType != null && iconMap.containsKey(contentType)) {						
				iconFileName = iconMap.getProperty(contentType);
			} else {
				iconFileName = iconMap.getProperty("unknown");
			}

			// get icon stream
			iconInput = IconTool.class.getResourceAsStream("/resources/icons/" + iconFileName);
			if (iconInput == null) return null;

			// load data
			IOTools.copy(iconInput, bout);
			return bout.toByteArray();
		} finally {
			bout.close();
			if (iconInput != null) iconInput.close();
		}
	}

	public static byte[] toBytes(Image icon) {
		try {
			BufferedImage bi = null;
			if (icon instanceof BufferedImage) {
				bi = (BufferedImage) icon;
			} else {

				int width = icon.getWidth(null); 
				int height = icon.getHeight(null);            
				if (width <= 0) width = (height > 0) ? height : 32; 
				if (height <= 0) height = (width > 0) ? width : 32;

				bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
				bi.createGraphics().drawImage(icon, 0, 0, width, height, null); 
			}

			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ImageIO.write(bi, "png", bout);
			bout.flush();
			bout.close();

			return bout.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
