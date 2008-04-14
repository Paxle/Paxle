package org.paxle.gui.impl.servlets;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.osgi.framework.Bundle;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.metatype.AttributeDefinition;
import org.osgi.service.metatype.MetaTypeInformation;
import org.osgi.service.metatype.MetaTypeService;
import org.osgi.service.metatype.ObjectClassDefinition;
import org.paxle.gui.ALayoutServlet;
import org.paxle.gui.IStyleManager;
import org.paxle.gui.impl.ServiceManager;


public class SettingsView extends ALayoutServlet {
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
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
	public Template handleRequest( HttpServletRequest request, HttpServletResponse response, Context context) throws Exception {
		
		Template template = null;
		try {
			if (request.getParameter("doEdit") != null) {
				this.setPropertyValues(request, response, context);
			} else if (ServletFileUpload.isMultipartContent(request)) {
				// getting the servicemanager
				ServiceManager manager = (ServiceManager) context.get(SERVICE_MANAGER);
				
				// getting a reference to the stylemanager
				IStyleManager styleManager = (IStyleManager) manager.getService(IStyleManager.class.getName());
				
				// get the file
				this.installStyle(styleManager, request, context);
			}

			context.put("dataTypes", dataTypes);
			context.put("settingsView", this);

			template = this.getTemplate( "/resources/templates/SettingsView.vm");
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
	
	private void installStyle(IStyleManager styleManager, HttpServletRequest request, Context context) throws Exception {

		// Create a factory for disk-based file items
		FileItemFactory factory = new DiskFileItemFactory();

		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);

		// Parse the request
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
		String pid = request.getParameter("pid");
		if (pid == null) {
			response.sendError(501, "No pid supplied.");
			return;
		}
		
		String bundleID = request.getParameter("bundleID");
		if (bundleID == null) {
			response.sendError(501, "No bundle-ID supplied.");
			return;
		}
		
		ServiceManager manager = (ServiceManager) context.get(SERVICE_MANAGER);
		if (bundleID == null) {
			response.sendError(501, "No ServiceManager found.");
			return;
		}
		
		// getting the bundle the managed service belongs to
		Bundle bundle = manager.getBundle(Long.valueOf(bundleID));
		if (bundleID == null) {
			response.sendError(501, String.format("No bundle with ID '%s' found.",bundleID));
			return;
		}
		
		// getting configuration meta-data
		MetaTypeService metaType = (MetaTypeService) manager.getService(MetaTypeService.class.getName());
		if (metaType == null) {
			response.sendError(501, "Metatype service not found.");
			return;
		}
		
		MetaTypeInformation metaTypeInfo = metaType.getMetaTypeInformation(bundle);
		if (metaTypeInfo == null) {
			response.sendError(501, String.format("No MetaTypeInformation found for service with PID '%s'.",pid));
			return;
		}
		
		String locale = "en";		
		ObjectClassDefinition ocd = metaTypeInfo.getObjectClassDefinition(pid, locale);
		if (ocd == null) {
			response.sendError(501, String.format("No ObjectClassDefinition found for service with PID '%s' and locale '%s'.",pid,locale));
			return;
		}		
		
		try {
			// trying to find a proper icon
			int[] sizes = new int[] {16,32,64,128,256};

			for (int size : sizes) {
				InputStream in = ocd.getIcon(16);
				if (in != null) {
					BufferedImage img = ImageIO.read(in);
					response.setHeader("Content-Type","image/png");
					ImageIO.write(img, "png", response.getOutputStream());
					return;
				} 
			}

			// no icon found. loading a default icon now
			BufferedImage img = ImageIO.read(this.getClass().getResourceAsStream("/resources/images/cog.png"));
			response.setHeader("Content-Type","image/png");
			ImageIO.write(img, "png", response.getOutputStream());
			return;

		} catch (Exception e) {			
			response.sendError(404, e.getMessage());
			return;
		}
	}
		
	
	public void setPropertyValues(HttpServletRequest request, HttpServletResponse response, Context context) throws Exception {		
		Dictionary<String, Object> props = new Hashtable<String, Object>();		
		
		String pid = request.getParameter("pid");
		if (pid == null) {
			context.put(ERROR_MSG, "No pid supplied.");
			return;
		}
		
		String bundleID = request.getParameter("bundleID");
		if (bundleID == null) {
			context.put(ERROR_MSG, "No bundle-ID supplied.");
			return;
		}
		
		ServiceManager manager = (ServiceManager) context.get(SERVICE_MANAGER);
		if (bundleID == null) {
			context.put(ERROR_MSG, "No ServiceManager found.");
			return;
		}
		
		// getting the bundle the managed service belongs to
		Bundle bundle = manager.getBundle(Long.valueOf(bundleID));
		if (bundleID == null) {
			context.put(ERROR_MSG, String.format("No bundle with ID '%s' found.",bundleID));
			return;
		}
		
		// getting configuration meta-data
		MetaTypeService metaType = (MetaTypeService) manager.getService(MetaTypeService.class.getName());
		if (metaType == null) {
			context.put(ERROR_MSG, "Metatype service not found.");
			return;
		}
		
		ConfigurationAdmin configAdmin = (ConfigurationAdmin) manager.getService(ConfigurationAdmin.class.getName());
		if (configAdmin == null) {
			context.put(ERROR_MSG, "ConfigurationAdmin service not found.");
			return;
		}
		
		MetaTypeInformation metaTypeInfo = metaType.getMetaTypeInformation(bundle);
		if (metaTypeInfo == null) {
			context.put(ERROR_MSG, String.format("No MetaTypeInformation found for service with PID '%s'.",pid));
			return;
		}
		
		String locale = "en";
		ObjectClassDefinition ocd = metaTypeInfo.getObjectClassDefinition(pid, locale);
		if (ocd == null) {
			context.put(ERROR_MSG, String.format("No ObjectClassDefinition found for service with PID '%s' and locale '%s'.",pid,locale));
			return;
		}
		
		AttributeDefinition[] attributes = ocd.getAttributeDefinitions(ObjectClassDefinition.ALL);
		if (attributes == null) {
			context.put(ERROR_MSG, String.format("No AttributeDefinitions found for service with PID '%s' and locale '%s'.",pid,locale));
			return;
		}
		
		for(AttributeDefinition attribute : attributes) {
			String attributeID = attribute.getID();
			String[] attributeDefaults = attribute.getDefaultValue();
			int attributeType = attribute.getType();
			Object attributeValue = null;
			
			String attributeValueStr = request.getParameter(attributeID);
			if (attributeValueStr == null && attributeDefaults != null && attributeDefaults.length == 1) {
				attributeValueStr = attributeDefaults[0];
			}
			
			if(attributeValueStr != null) {				
				// validate value
				String validationProblem = attribute.validate(attributeValueStr);
				if (validationProblem != null && validationProblem.length() > 0) {
					context.put(ERROR_MSG, String.format("Parameter '%s' has a wrong value: %s",attributeID,validationProblem));
					return;
				}
				
				try {
					switch (attributeType) {
						case AttributeDefinition.BOOLEAN:
							attributeValue = Boolean.valueOf(attributeValueStr);
							break;
						case AttributeDefinition.BYTE:
							attributeValue = Byte.valueOf(attributeValueStr);
							break; 
						case AttributeDefinition.CHARACTER:
	//						attributeValue = Character.
							break; 
						case AttributeDefinition.DOUBLE:
							attributeValue = Double.valueOf(attributeValueStr);
							break; 
						case AttributeDefinition.FLOAT:
							attributeValue = Float.valueOf(attributeValueStr);
							break; 
						case AttributeDefinition.INTEGER:
							attributeValue = Integer.valueOf(attributeValueStr);
							break; 
						case AttributeDefinition.LONG:
							attributeValue = Long.valueOf(attributeValueStr);
							break; 
						case AttributeDefinition.SHORT:
							attributeValue = Short.valueOf(attributeValueStr);
							break; 
						case AttributeDefinition.STRING:
							attributeValue = attributeValueStr;
							break; 
						default:
							break;
					}
				} catch (NumberFormatException e) {
					context.put(ERROR_MSG, String.format("The supplied parameter has a wront format: %s",e.getMessage()));
					return;
				}
				
				if (attributeValue != null) {
					props.put(attributeID, attributeValue);
				}
			}					
		}
		
		// update configuration		
		Configuration config = configAdmin.getConfiguration(pid, bundle.getLocation());
		config.update(props);			
	}
	
	public Object getPropertyValue(Configuration config, AttributeDefinition attribute) {
		if (config == null) throw new NullPointerException("Configuration object is null");
		if (attribute == null) throw new NullPointerException("Attribute definition is null");
		
		String propertyKey = attribute.getID();
		Dictionary props = (config==null)?null:config.getProperties();
		Object value = (props == null)?null:props.get(propertyKey);
		String[] defaultValues = attribute.getDefaultValue();
		
		if (value != null) {
			return value;
		} else if (defaultValues != null && defaultValues.length > 0){
			return defaultValues[0];
		} else {
			return null;
		}
	}
}
