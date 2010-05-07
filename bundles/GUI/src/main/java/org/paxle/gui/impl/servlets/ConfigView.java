/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2010 the original author or authors.
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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.tools.view.VelocityLayoutServlet;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.service.cm.Configuration;
import org.osgi.service.metatype.AttributeDefinition;
import org.osgi.service.metatype.ObjectClassDefinition;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.core.metadata.Attribute;
import org.paxle.core.metadata.Metadata;
import org.paxle.gui.IServiceManager;
import org.paxle.gui.IServletManager;
import org.paxle.gui.IStyleManager;
import org.paxle.gui.impl.tools.ConfigTool;
import org.paxle.gui.impl.tools.ConfigTool.Configurable;
import org.paxle.tools.ieporter.cm.IConfigurationIEPorter;

@Component(metatype=false, immediate=true)
@Service(Servlet.class)
@Properties({
	@Property(name="org.paxle.servlet.path", value="/config"),
	@Property(name="org.paxle.servlet.doUserAuth", boolValue=true),
	@Property(name="org.paxle.servlet.menu", value="%menu.administration/%menu.bundles/%menu.system.configManagement"), 
	@Property(name="org.paxle.servlet.menu.icon", value="/resources/images/bullet_wrench.png")
})
public class ConfigView extends VelocityLayoutServlet {
	private static final long serialVersionUID = 1L;
	
	private static final String ERROR_MSG = "errorMsg";
	
	/**
	 * A mapping between type-nr and type string
	 */
    private static final Map<Integer,String> dataTypes = new HashMap<Integer,String>();
    static {
    	dataTypes.put(Integer.valueOf(AttributeDefinition.BOOLEAN), "Boolean");
    	dataTypes.put(Integer.valueOf(AttributeDefinition.BYTE),"Byte");
    	dataTypes.put(Integer.valueOf(AttributeDefinition.CHARACTER),"Character");
    	dataTypes.put(Integer.valueOf(AttributeDefinition.DOUBLE),"Double");
    	dataTypes.put(Integer.valueOf(AttributeDefinition.FLOAT),"Float");
    	dataTypes.put(Integer.valueOf(AttributeDefinition.INTEGER),"Integer");
    	dataTypes.put(Integer.valueOf(AttributeDefinition.LONG),"Long");
    	dataTypes.put(Integer.valueOf(AttributeDefinition.SHORT),"Short");
    	dataTypes.put(Integer.valueOf(AttributeDefinition.STRING),"String");
    }	
    
    /**
     * For logging
     */
	private Log logger = LogFactory.getLog( this.getClass());

	/**
	 * Paxle internal temp file manager
	 */
	@Reference
	protected ITempFileManager tfm;	
	
	/**
	 * This function generates a map required by the {@link IConfigurationIEPorter} to export 
	 * {@link Configuration#getProperties() configuration-properties}
	 * 
	 * @param context the velocity-context
	 * @return a map containing {@link Constants#SERVICE_PID PIDs} as key and 
	 * 		{@link Bundle#getLocation() bundle-locations} as values.
	 */
	private Map<String, String> generatePidBundleLocationMap(HttpServletRequest request, Context context) {
		// getting the config-tool
		final ConfigTool configTool = (ConfigTool) context.get(ConfigTool.TOOL_NAME);
		if (configTool == null) throw new IllegalStateException("Config-Tool not found");
		
		// getting the bundle-id and service-PID (if available)
		final String bundleID = request.getParameter("bundleID");
		final String pid = request.getParameter("pid");
		
		// getting all specified configurables
		final List<Configurable> configurables = new ArrayList<Configurable>();		
		if (bundleID != null && pid != null) {
			Configurable configurable = configTool.getConfigurable(Integer.valueOf(bundleID), pid);
			if (configurable != null) configurables.add(configurable);
		} else {		
			configurables.addAll(configTool.getConfigurables());
		}
		
		// converting configurable list into a list of PID:Bundle-Location
		Map<String, String> pidBundleLocationTupel = new HashMap<String, String>();
		for (Configurable configurable : configurables) {
			pidBundleLocationTupel.put(configurable.getPID(), configurable.getBundle().getLocation());
		}		
		return pidBundleLocationTupel;
	}
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		if (request.getParameter("getImage") != null) {	
			try {
				/*
				 * this is a template less request therefore we need to overwrite doGet
				 */

				// create context
				Context context = this.createContext(request, response);
				this.writeImage(request, response, context);
			} catch (Exception e) {
				throw new IOException(e.getMessage());
			}
		} else {
			super.doGet(request, response);
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (request.getParameter("doExportConfig") != null) {
			InputStream fileIn = null;
			File zipFile = this.tfm.createTempFile(); //zip file where the config is stored
			try {
				// create context
				Context context = this.createContext(request, response);

				// getting the config-exporter
				IServiceManager manager = (IServiceManager) context.get(IServiceManager.SERVICE_MANAGER);
				IConfigurationIEPorter exporter = (IConfigurationIEPorter) manager.getService(IConfigurationIEPorter.class.getName());

				// export configuration
				response.setContentType("application/zip");
				response.setHeader("Content-Disposition", "attachment; filename=paxleConfig" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".zip");
				
				exporter.exportConfigsAsZip(this.generatePidBundleLocationMap(request, context), zipFile);
				fileIn = new FileInputStream(zipFile);

				IOUtils.copy(fileIn, response.getOutputStream());

				fileIn.close();
				this.tfm.releaseTempFile(zipFile);
			} catch (Exception e) {
				throw new IOException(e.getMessage());
			} finally {
				if (fileIn != null) try { fileIn.close(); } catch (Exception e) {/* ignore this */}
				if (this.tfm.isKnown(zipFile)) try { this.tfm.releaseTempFile(zipFile); } catch (Exception e) {/* ignore this */}
			}
		} else {
			super.doPost(request, response);
		}
	}
	
	@Override
	public Template handleRequest(HttpServletRequest request, HttpServletResponse response, Context context) throws Exception {
		
		Template template = null;
		try {
			// getting the servicemanager
			IServiceManager manager = (IServiceManager) context.get(IServiceManager.SERVICE_MANAGER);
			IServletManager sManager = (IServletManager) manager.getService(IServletManager.class.getName());

			/* ====================================================================================
			 * CONFIGURATION MANAGEMENT 
			 * ==================================================================================== */
				
			if (request.getParameter("doEditConfig") != null) {
				this.setPropertyValues(request, response, context, false);
				if ("org.paxle.gui.IServletManager".equals(request.getParameter("pid"))) {
					String newPrefix = request.getParameter("org.paxle.gui.IServletManager.pathPrefix");
					context.put("delayedRedirect",sManager.getFullAlias(newPrefix, "/config"));
				} else {
					response.sendRedirect(request.getServletPath());
				}
			} else if (request.getParameter("doResetConfig") != null) {
				this.setPropertyValues(request, response, context, true);
				if ("org.paxle.gui.IServletManager".equals(request.getParameter("pid"))) {
					String newPrefix = request.getParameter("org.paxle.gui.IServletManager.pathPrefix");
					context.put("delayedRedirect",sManager.getFullAlias(newPrefix, "/config"));
				} else {
					response.sendRedirect(request.getServletPath());
				}
				
			} else if (request.getParameter("viewImportedConfig") != null) {
				if (ServletFileUpload.isMultipartContent(request)) {
					// import config from file
					Map<String, Dictionary<String, Object>> propsMap = this.importConfig(request, context);
					
					// add it into the context so that the servlet can read it
					context.put("importedConfigProps", propsMap);
					
					// add it into the session so that the servlet can read it lateron
					HttpSession session = request.getSession(true);
					session.setAttribute("importedConfigProps", propsMap);
				} else {
					// read the imported-config from session into context so that the sevlet can read it
					HttpSession session = request.getSession(true);
										
					@SuppressWarnings("unchecked") 
					Map<String, Dictionary<String, Object>> propsMap = (Map<String, Dictionary<String, Object>>) session.getAttribute("importedConfigProps");
					context.put("importedConfigProps", propsMap);
					
					if (request.getParameter("doImportConfig") != null) {
						// configure properties
						this.setPropertyValues(request, response, context, false);
						
						// remove already imported properties from map
						propsMap.remove(request.getParameter("pid"));
						
						// redirect to overview
						response.sendRedirect(request.getServletPath() + "?viewImportedConfig=");
					}
				}
			} else if (request.getParameter("doInstallStyle") != null && ServletFileUpload.isMultipartContent(request)) {
				// getting a reference to the stylemanager
				IStyleManager styleManager = (IStyleManager) manager.getService(IStyleManager.class.getName());
				
				// get the file
				this.installStyle(styleManager, request, context);
			}

			context.put("dataTypes", dataTypes);
			context.put("configView", this);

			template = this.getTemplate( "/resources/templates/ConfigView.vm");
		} catch (ResourceNotFoundException e) {
			logger.error( "resource: " + e);
			e.printStackTrace();
		} catch (ParseErrorException e) {
			logger.error( "parse : " + e);
			e.printStackTrace();
		} catch (Exception e) {
			logger.error( "exception: " + e);
			e.printStackTrace();
		}

		return template;
	}
	
	private Map<String, Dictionary<String, Object>> importConfig(HttpServletRequest request, Context context) throws Exception {

		// Create a factory for disk-based file items
		FileItemFactory factory = new DiskFileItemFactory();

		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);

		// Parse the request
		@SuppressWarnings("unchecked")
		List<FileItem> items = upload.parseRequest(request);

		// Process the uploaded items
		Iterator<FileItem> iter = items.iterator();
		while (iter.hasNext()) {
			FileItem item = iter.next();

			if (!item.isFormField()) {
				if (!item.getFieldName().equals("configFile")) {
					String errorMsg = String.format("Unknown file-upload field '%s'.",item.getFieldName());
					this.logger.warn(errorMsg);
					context.put("errorMsg",errorMsg);
					continue;
				}

				String fileName = item.getName();
				if (fileName != null) {
					fileName = FilenameUtils.getName(fileName);
				} else {
					String errorMsg = String.format("Fileupload field '%s' has no valid filename.", item.getFieldName());
					this.logger.warn(errorMsg);
					context.put("errorMsg",errorMsg);
					continue;
				}

				// write the bundle to disk
				File targetFile = File.createTempFile(fileName, ".tmp");      		    
				item.write(targetFile);

				// cleanup
				item.delete();
				
				// read Settings
				IServiceManager manager = (IServiceManager) context.get(IServiceManager.SERVICE_MANAGER);
				IConfigurationIEPorter importer = (IConfigurationIEPorter) manager.getService(IConfigurationIEPorter.class.getName());
				Map<String, Dictionary<String, Object>> propsMap = importer.importConfigurations(targetFile);
				return propsMap;
			}
		}
		
		return null;
	}
	
	private void installStyle(IStyleManager styleManager, HttpServletRequest request, Context context) throws Exception {

		// Create a factory for disk-based file items
		FileItemFactory factory = new DiskFileItemFactory();

		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);

		// Parse the request
		@SuppressWarnings("unchecked")
		List<FileItem> items = upload.parseRequest(request);

		// Process the uploaded items
		Iterator<FileItem> iter = items.iterator();
		while (iter.hasNext()) {
			FileItem item = iter.next();

			if (!item.isFormField()) {
				if (!item.getFieldName().equals("styleJar")) {
					String errorMsg = String.format("Unknown file-upload field '%s'.",item.getFieldName());
					this.logger.warn(errorMsg);
					context.put("errorMsg",errorMsg);
					continue;
				}

				String fileName = item.getName();
				if (fileName != null) {
					fileName = FilenameUtils.getName(fileName);
				} else {
					String errorMsg = String.format("Fileupload field '%s' has no valid filename.", item.getFieldName());
					this.logger.warn(errorMsg);
					context.put("errorMsg",errorMsg);
					continue;
				}

				// write the bundle to disk
				File targetFile = new File(styleManager.getDataPath(),fileName);
				if (targetFile.exists()) {
					String errorMsg = String.format("Targetfile '%s' already exists.",targetFile.getCanonicalFile().toString());
					this.logger.warn(errorMsg);
					context.put("errorMsg",errorMsg);
					continue;
				}            		    
				item.write(targetFile);

				// cleanup
				item.delete();
			}
		}	
	}
	
	public void writeImage(HttpServletRequest request, HttpServletResponse response, Context context) throws Exception {	
		final String pid = request.getParameter("pid");
		if (pid == null) {
			response.sendError(501, "No pid supplied.");
			return;
		}
		
		final String bundleID = request.getParameter("bundleID");
		if (bundleID == null) {
			response.sendError(501, "No bundle-ID supplied.");
			return;
		}
		
		final ConfigTool configTool = (ConfigTool) context.get(ConfigTool.TOOL_NAME);
		if (configTool == null) {
			response.sendError(501, "Config-Tool not found.");
			return;
		}
		
		final Configurable configurabel = configTool.getConfigurable(Integer.valueOf(bundleID), pid);
		if (configurabel == null) {
			response.sendError(501, String.format(
					"No configurable component found for bundle-ID '%s' and PID '%s'.",
					bundleID,
					pid
			));
			return;
		}
		
		// loading metadata
		final ObjectClassDefinition ocd = configurabel.getObjectClassDefinition();
		if (ocd == null) {
			response.sendError(501, String.format("No ObjectClassDefinition found for service with PID '%s'.",pid));
			return;
		}		
		
		try {
			// trying to find a proper icon
			final int[] sizes = new int[] {16,32,64,128,256};

			BufferedImage img = null;
			for (int size : sizes) {
				// trying to find an icon
				InputStream in = ocd.getIcon(size);
				if (in == null) {
					if (size == sizes[sizes.length-1]) {
						// fallback to the default image
						in = this.getClass().getResourceAsStream("/resources/images/cog.png");
					} else continue;
				}
				
				// loading date
				final ByteArrayOutputStream bout = new ByteArrayOutputStream();
				IOUtils.copy(in, bout);
				bout.close();
				in.close();
				
				// trying to detect the mimetype of the image
				ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
				String contentType = URLConnection.guessContentTypeFromStream(bin);
				bin.close();
				
				Iterator<ImageReader> readers = null;
				if (contentType != null) {
					readers = ImageIO.getImageReadersByMIMEType(contentType);
				} else {
					readers = ImageIO.getImageReadersByFormatName("png"); 
				}
				
				while (readers.hasNext() && img == null) {
					// trying the next reader
					final ImageReader reader = readers.next();
					
					InputStream input = null;
					try {
						input = new ByteArrayInputStream(bout.toByteArray());
						reader.setInput(ImageIO.createImageInputStream(input));
						img = reader.read(0);
					} catch (Exception e) {
						this.log("Unable to read image for pid " + pid, e);
					} finally {					
						if (input != null) input.close();
					}
				}

				if (img != null) {
					response.setHeader("Content-Type","image/png");
					ImageIO.write(img, "png", response.getOutputStream());
					return;
				}
			}

			// no icon found. 
			response.sendError(404);
		} catch (Throwable e) {			
			response.sendError(404, e.getMessage());
			return;
		}
	}	
	
	public void setPropertyValues(HttpServletRequest request, HttpServletResponse response, Context context, final boolean reset) throws Exception {		
		final Dictionary<String, Object> props = new Hashtable<String, Object>();		

		final String pid = request.getParameter("pid");
		if (pid == null) {
			context.put(ERROR_MSG, "No pid supplied.");
			return;
		}
		
		final String bundleID = request.getParameter("bundleID");
		if (bundleID == null) {
			context.put(ERROR_MSG, "No bundle-ID supplied.");
			return;
		}
		
		final ConfigTool configTool = (ConfigTool) context.get(ConfigTool.TOOL_NAME);
		if (configTool == null) {
			context.put(ERROR_MSG, "Config-Tool not found.");
			return;
		}
		
		final Configurable configurabel = configTool.getConfigurable(Integer.valueOf(bundleID), pid);
		if (configurabel == null) {
			context.put(ERROR_MSG, String.format(
					"No configurable component found for bundle-ID '%s' and PID '%s'.",
					bundleID,
					pid
			));
			return;
		}
		
		final Configuration config = configurabel.getConfiguration();
		if (config == null) {
			context.put(ERROR_MSG, "Configuration object not found.");
			return;
		}
		
		final ObjectClassDefinition ocd = configurabel.getObjectClassDefinition();
		if (ocd == null) {
			context.put(ERROR_MSG, String.format("No ObjectClassDefinition found for service with PID '%s'.",pid));
			return;
		}
		
		final AttributeDefinition[] attributes = ocd.getAttributeDefinitions(ObjectClassDefinition.ALL);
		if (attributes == null) {
			context.put(ERROR_MSG, String.format("No AttributeDefinitions found for service with PID '%s'.",pid));
			return;
		}
		
		for(AttributeDefinition attribute : attributes) {
			String[] attributeDefaults = attribute.getDefaultValue();
			
			// getting configured values
			String attributeID = attribute.getID();	
			String[] attributeValueStrings = request.getParameterValues(attributeID);
			
			// if no values were found use defaults for the initial configuration
			/*
			if (attributeValueStrings == null && attributeDefaults != null && attributeDefaults.length > 0) {
				attributeValueStrings = attributeDefaults;
			}	
			*/
			
			final Object finalAttributeValues = convertAttributeValues(
					attribute,
					(reset) ? attributeDefaults : attributeValueStrings,
					context);
			
			if (finalAttributeValues != null) {
				props.put(attributeID, finalAttributeValues);
			}				
		}
		
		// update configuration		
		config.update(props);			
	}
	
	@SuppressWarnings("unchecked")
	private static Object convertAttributeValues(
			final AttributeDefinition attribute,
			final String[] attributeValueStrings,
			final Context context) {
		
		// getting metadata
		String attributeID = attribute.getID();	
		int attributeType = attribute.getType();
		int cardinality = attribute.getCardinality();		
		
		// init result structure
		Object finalAttributeValues = null;
		if (cardinality != 0) {				
			if (cardinality >= 1) {
				int valueArraySize = Math.min(Math.abs(cardinality), (attributeValueStrings == null) ? 0 : attributeValueStrings.length);
				
				// the attribute-value-list must be an array of ....
				switch (attributeType) {
					case AttributeDefinition.BOOLEAN:
						finalAttributeValues = new boolean[valueArraySize];
						break;
					case AttributeDefinition.BYTE:
						finalAttributeValues = new byte[valueArraySize];
						break; 
					case AttributeDefinition.CHARACTER:
						finalAttributeValues = new char[valueArraySize];
						break; 
					case AttributeDefinition.DOUBLE:
						finalAttributeValues = new double[valueArraySize];
						break; 
					case AttributeDefinition.FLOAT:
						finalAttributeValues = new float[valueArraySize];
						break; 
					case AttributeDefinition.INTEGER:
						finalAttributeValues = new int[valueArraySize];
						break; 
					case AttributeDefinition.LONG:
						finalAttributeValues = new long[valueArraySize];
						break; 
					case AttributeDefinition.SHORT:
						finalAttributeValues = new short[valueArraySize];
						break; 
					case AttributeDefinition.STRING:
						finalAttributeValues = new String[valueArraySize];
						break; 
					default:
						break;
				}
			} else {
				// the attribute-value list must be a vector
				finalAttributeValues = new Vector<Object>();
			}
		}			
		
		if(attributeValueStrings != null) {								
			try {
				int counter = 0;
				for (String attributeValueString : attributeValueStrings) {
					if (cardinality != 0 && (counter + 1 > Math.abs(cardinality))) {
						context.put(ERROR_MSG, String.format("Too many values found for parameter '%s': %d", attributeID, Integer.valueOf(counter+1)));
						return null;
					}

					// validate value
					String validationProblem = attribute.validate(attributeValueString);
					if (validationProblem != null && validationProblem.length() > 0) {
						context.put(ERROR_MSG, String.format("Parameter '%s' has a wrong value: %s",attributeID,validationProblem));
						return null;
					}

					// convert value
					Object finalAttributeValue = null;
					switch (attributeType) {
						case AttributeDefinition.BOOLEAN:
							finalAttributeValue = Boolean.valueOf(attributeValueString);
							if (cardinality >= 1) ((boolean[])finalAttributeValues)[counter] = ((Boolean)finalAttributeValue).booleanValue();
							else if (cardinality <= -1) ((Vector<Boolean>)finalAttributeValues).add((Boolean)finalAttributeValue);
							break;
						case AttributeDefinition.BYTE:
							finalAttributeValue = Byte.valueOf(attributeValueString);
							if (cardinality >= 1) ((byte[])finalAttributeValues)[counter] = ((Byte)finalAttributeValue).byteValue();
							else if (cardinality <= -1) ((Vector<Byte>)finalAttributeValues).add((Byte)finalAttributeValue);								
							break; 
						case AttributeDefinition.CHARACTER:
							//						attributeValue = Character.
							break; 
						case AttributeDefinition.DOUBLE:
							finalAttributeValue = Double.valueOf(attributeValueString);
							if (cardinality >= 1) ((double[])finalAttributeValues)[counter] = ((Double)finalAttributeValue).doubleValue();
							else if (cardinality <= -1) ((Vector<Double>)finalAttributeValues).add((Double)finalAttributeValue);							
							break; 
						case AttributeDefinition.FLOAT:
							finalAttributeValue = Float.valueOf(attributeValueString);
							if (cardinality >= 1) ((float[])finalAttributeValues)[counter] = ((Float)finalAttributeValue).floatValue();
							else if (cardinality <= -1) ((Vector<Float>)finalAttributeValues).add((Float)finalAttributeValue);								
							break; 
						case AttributeDefinition.INTEGER:
							finalAttributeValue = Integer.valueOf(attributeValueString);
							if (cardinality >= 1) ((int[])finalAttributeValues)[counter] = ((Integer)finalAttributeValue).intValue();
							else if (cardinality <= -1) ((Vector<Integer>)finalAttributeValues).add((Integer)finalAttributeValue);								
							break; 
						case AttributeDefinition.LONG:
							finalAttributeValue = Long.valueOf(attributeValueString);
							if (cardinality >= 1) ((long[])finalAttributeValues)[counter] = ((Long)finalAttributeValue).longValue();
							else if (cardinality <= -1) ((Vector<Long>)finalAttributeValues).add((Long)finalAttributeValue);								
							break; 
						case AttributeDefinition.SHORT:
							finalAttributeValue = Short.valueOf(attributeValueString);
							if (cardinality >= 1) ((short[])finalAttributeValues)[counter] = ((Short)finalAttributeValue).shortValue();
							else if (cardinality <= -1) ((Vector<Short>)finalAttributeValues).add((Short)finalAttributeValue);								
							break; 
						case AttributeDefinition.STRING:
							finalAttributeValue = attributeValueString;
							if (cardinality >= 1) ((String[])finalAttributeValues)[counter] = attributeValueString;
							else if (cardinality <= -1) ((Vector<String>)finalAttributeValues).add((String)finalAttributeValue);								
							break; 
						default:
							break;
					}
					
					if (cardinality == 0) {
						finalAttributeValues = finalAttributeValue;
					} 
					counter++;
				}
			} catch (NumberFormatException e) {
				context.put(ERROR_MSG, String.format("The supplied parameter has a wrong format: %s",e.getMessage()));
				return null;
			}
		}
		
		return finalAttributeValues;
	}
	
	public HashMap<String,Attribute> getAttributeMetadataMap(final ObjectClassDefinition ocd) {
		final HashMap<String,Attribute> attrMetadata = new HashMap<String,Attribute>();
		final Metadata metadata = ocd.getClass().getAnnotation(Metadata.class);
		if (metadata != null)
			for (final Attribute attr : metadata.value())
				attrMetadata.put(attr.id(), attr);
		return attrMetadata;
	}
}
