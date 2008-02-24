package org.paxle.icon.impl;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.text.html.parser.ParserDelegator;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
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
	private static Properties iconMap = new Properties();
	
	/**
	 * default icon for unknown mime-types
	 */
	private static IconData defaultIcon = null;
	
	/**
	 * default icon for html pages without favicon
	 */
	private static IconData defaultHtmlIcon = null;
	
	/*
	 * Initialize the icon-map and load the default icons
	 */
	static {
		// configure connection manager
		connectionManager.getParams().setDefaultMaxConnectionsPerHost(10);
		
		/* configure mime-type to resource-file map */		
		try {
			InputStream mapStream = IconTool.class.getResourceAsStream("/resources/iconmap.properties");
			iconMap.load(mapStream);
			mapStream.close();
			
			// load the default icon
			byte[] data = readFileIconPng("unknown");
			if (data != null) defaultIcon = new IconData(data);
			
			data = readFileIconPng("text/html");
			if (data != null) defaultHtmlIcon = new IconData(data);
		} catch (Exception e) {
			e.printStackTrace();
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
		if (url == null) return defaultIcon;
		if (!url.getProtocol().equals("http")) return defaultIcon;
		
		// TODO: for non http resources we should just load the icons from file
		
		GetMethod method = null;
		try {
			method = new GetMethod(url.toExternalForm());
			int status = httpClient.executeMethod(method);
			if (status != 200) {
				// TODO: logging
				return null;
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
					faviconURL = new URL(url,"/favicon.ico");
				} else {			
					faviconURL = new URL(url,urlString);				
				}
				IconData faviconData = getIcon(faviconURL);
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
				InputStream bodyStream = method.getResponseBodyAsStream();
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
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
				bout.close();
			}
			
			return (body == null) ? defaultIcon : new IconData(iconType,body);
		} catch (Exception e) {
			// TODO: logging
			e.printStackTrace();
			return null;
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
			if (bout != null) bout.close();
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
