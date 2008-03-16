package org.paxle.gui.impl.servlets;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.paxle.gui.ALayoutServlet;
import org.paxle.gui.impl.ServiceManager;

public class BundleView extends ALayoutServlet {

	private static final long serialVersionUID = 1L;
	
    private static final Map<Integer,String> states = new HashMap<Integer,String>();
    static {
    	states.put(Integer.valueOf(Bundle.ACTIVE), "active");
    	states.put(Integer.valueOf(Bundle.INSTALLED), "installed");
    	states.put(Integer.valueOf(Bundle.RESOLVED), "resolved");
    	states.put(Integer.valueOf(Bundle.STARTING), "starting");
    	states.put(Integer.valueOf(Bundle.STOPPING), "stopping");
    	states.put(Integer.valueOf(Bundle.UNINSTALLED), "uninstalled");
    }
    
	public BundleView(String bundleLocation) {
		super(bundleLocation);
	}
	
    public Template handleRequest( HttpServletRequest request,
            HttpServletResponse response,
            Context context ) {
    	
        Template template = null;
        try {
            template = this.getTemplate("/resources/templates/BundleView.vm");
            
            ServiceManager manager = (ServiceManager) context.get(SERVICE_MANAGER);
            if (request.getParameter("bundleID") != null) {
            	Long bundleID = Long.parseLong(request.getParameter("bundleID"));
            	Bundle bundle = manager.getBundle(bundleID);
        		if (bundle == null) {
        			String errorMsg = String.format(
        					"Bundle with ID '%s' not found.", 
        					request.getParameter("bundleID")
        			);
        			this.logger.warn(errorMsg);
        			context.put("errorMsg",errorMsg);
        		} else {
        			try {
        				if (request.getParameter("update") != null) {
        					bundle.update();
        				} else if (request.getParameter("start") != null) {
        					bundle.start();
        				} else if (request.getParameter("stop") != null) {
        					bundle.stop();
        				} else if (request.getParameter("restart") != null) {
        					bundle.stop();
        					bundle.start();
        				}else if(request.getParameter("uninstall") != null){
        					bundle.uninstall();
        				}
        			} catch (BundleException e) {
            			String errorMsg = String.format(
            					"Unexpected exception while operating on bundle with ID '%s", 
            					request.getParameter("bundleID")
            			);
            			this.logger.warn(errorMsg, e);
            			context.put("errorMsg",e.getMessage());
        			}
        		}
            }else if(request.getParameter("installURL") != null && request.getParameter("bundlePath")!=null){
            	ServiceManager.context.installBundle(request.getParameter("bundlePath"));
            } else if (request.getParameter("details") != null) {
            	Bundle bundle = manager.getBundle(Long.parseLong(request.getParameter("bundleID")));
            	context.put("bundle", bundle);
            } else if (ServletFileUpload.isMultipartContent(request)) {

            	// Create a factory for disk-based file items
            	FileItemFactory factory = new DiskFileItemFactory();

//          	// Set factory constraints
//          	factory.setSizeThreshold(yourMaxMemorySize);
//          	factory.setRepository(yourTempDirectory);

            	// Create a new file upload handler
            	ServletFileUpload upload = new ServletFileUpload(factory);

//          	// Set overall request size constraint
//          	upload.setSizeMax(yourMaxRequestSize);

            	// Parse the request
            	List<FileItem> items = upload.parseRequest(request);

            	// Process the uploaded items
            	Iterator<FileItem> iter = items.iterator();
            	while (iter.hasNext()) {
            		FileItem item = iter.next();

            		if (!item.isFormField()) {
            			if (!item.getFieldName().equals("bundleJar")) {
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
            		    File targetFile = new File(fileName);
            		    if (targetFile.exists()) {
            		    	String errorMsg = String.format("Targetfile '%s' already exists.",targetFile.getCanonicalFile().toString());
            		    	this.logger.warn(errorMsg);
            		    	context.put("errorMsg",errorMsg);
            		    	continue;
            		    }            		    
            		    item.write(targetFile);
            		    
            		    // installing bundle
            			ServiceManager.context.installBundle(targetFile.toURL().toString());

            			// cleanup
            			item.delete();
            		}
            	}

            }
            
            
            context.put("bundles", bundles2map(manager.getBundles()));
            context.put("states", states);
        } catch (Throwable e) {
            System.err.println("Exception caught: " + e.getMessage());
            e.printStackTrace();
        }
        
        return template;
    }
    
    private static TreeMap<Long,Bundle> bundles2map(Bundle[] bundles) {
    	final TreeMap<Long,Bundle> r = new TreeMap<Long,Bundle>();
    	for (Bundle bundle : bundles)
    		if (bundle.getBundleId() > 0)
    			r.put(Long.valueOf(bundle.getBundleId()), bundle);
    	return r;
    }
}
