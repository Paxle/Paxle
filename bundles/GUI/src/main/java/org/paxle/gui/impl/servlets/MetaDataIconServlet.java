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
package org.paxle.gui.impl.servlets;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.velocity.context.Context;
import org.paxle.core.metadata.IMetaData;
import org.paxle.gui.ALayoutServlet;
import org.paxle.gui.impl.tools.MetaDataTool;

@Component(metatype=false, immediate=true)
@Service(Servlet.class)
@Properties({
	@Property(name="org.paxle.servlet.path", value="/metaDataIcon"),
	@Property(name="org.paxle.servlet.doUserAuth", boolValue=false)
})
public class MetaDataIconServlet extends ALayoutServlet {
	private static final long serialVersionUID = 1L;
	private static final String REQ_PARAM_SERVICEPID = "servicePID";
	
	@Override
	protected void doRequest(HttpServletRequest request, HttpServletResponse response) {
		try {
			final Context context = this.getVelocityView().createContext(request, response);
			final String servicePID = request.getParameter(REQ_PARAM_SERVICEPID);
			
			if (servicePID != null) {
				// getting the metaDataTool
				final MetaDataTool tool = (MetaDataTool) context.get(MetaDataTool.TOOL_NAME);
				if (tool == null) {
					this.log("No MetaDataTool found");
					return;
				}
				
				final IMetaData metaData = tool.getMetaData(servicePID);
				if (metaData == null) {
					this.log(String.format("No metadata found for service with PID '%s'.",servicePID));
					return;
				}
				
				// getting the icon
				InputStream in = metaData.getIcon(16);
				if (in == null) in = this.getClass().getResourceAsStream("/resources/images/cog.png");
				
				// loading date
				final ByteArrayOutputStream bout = new ByteArrayOutputStream();
				IOUtils.copy(in, bout);
				bout.close();
				in.close();
				
				// trying to detect the mimetype of the image
				final ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
				String contentType = URLConnection.guessContentTypeFromStream(bin);
				bin.close();				
				
				// reading the image
				BufferedImage img = null;
				Iterator<ImageReader> readers = null;
				if (contentType != null) {
					readers = ImageIO.getImageReadersByMIMEType(contentType);				
					while (readers != null && readers.hasNext() && img == null) {
						// trying the next reader
						ImageReader reader = readers.next();
						
						InputStream input = null;
						try {
							input = new ByteArrayInputStream(bout.toByteArray());
							reader.setInput(ImageIO.createImageInputStream(input));
							img = reader.read(0);
						} catch (Exception e) {
							this.log(String.format(
								"Unable to read metadata icon for service with PID '%s'.",
								servicePID
							), e);
						} finally {					
							if (input != null) input.close();
						}
					}
				}
					
				if (img != null) {
					response.setHeader("Content-Type","image/png");
					ImageIO.write(img, "png", response.getOutputStream());
					return;
				} else {
					response.sendError(404);
					return;
				}
			} else {
				response.sendError(500, "Invalid usage");
			}
		} catch (Throwable e) {
			this.log(String.format("Unexpected '%s'.",e.getClass().getName(),e));
		}	
	}
}
