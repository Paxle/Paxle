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

package org.paxle.gui.impl.tools;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;

import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.ValidScope;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.monitor.MonitorAdmin;
import org.osgi.service.monitor.StatusVariable;
import org.paxle.core.io.IResourceBundleTool;
import org.paxle.gui.impl.ServletManager;
import org.paxle.gui.impl.tools.MonitorableTool.Monitorable.Variable;

@DefaultKey(MonitorableTool.TOOL_NAME)
@ValidScope(Scope.REQUEST)
public class MonitorableTool extends PaxleLocaleConfig {
	public static final String TOOL_NAME = "monitorableTool";
	
	/**
	 * The OSGi {@link MonitorAdmin} service.
	 */
	private MonitorAdmin ma;
	
	/**
	 * A tool to load {@link ResourceBundle resource-bundles} from {@link Bundle OSGi-bundles} 
	 */
	private IResourceBundleTool rbTool;
	
	/**
	 * This method is called by velocity during tool(box) initialization
	 * We need it to fetch a reference to the current {@link BundleContext} from the {@link ServletContext}.
	 * 
	 * The {@link BundleContext} was added to the {@link ServletContext} by the {@link ServletManager} during
	 * {@link Servlet} registration.
	 * 
	 * @param props
	 */
	public void configure(@SuppressWarnings("unchecked") Map props) {
		super.configure(props);
		
		// getting the monitor admin service
		if (this.context != null) {
			ServiceReference ref = this.context.getServiceReference(MonitorAdmin.class.getName());
			if (ref != null) {
				this.ma = (MonitorAdmin) this.context.getService(ref);
			}
			
			ref = this.context.getServiceReference(IResourceBundleTool.class.getName());
			if (ref != null) {
				this.rbTool = (IResourceBundleTool) this.context.getService(ref);
			}
		}
	}
	
	public boolean isMonitorable(String serviceID) {
		return this.exists(serviceID);
	}

	public boolean exists(String monitorableID) {
    	if (monitorableID == null) return false;
    	else if (this.ma == null) return false;
    	
    	final String[] monitorableNames = this.ma.getMonitorableNames();
    	if (monitorableNames == null || monitorableNames.length == 0) return false;
    	
    	for (String monitorableName : monitorableNames) {
    		if (monitorableName.equals(monitorableID)) {
    			return true;
    		}
    	}
    	
    	return false;
	}
	
    public Monitorable get(String monitorableID) {
    	if (monitorableID == null) return null;
    	else if (this.ma == null) return null;
    	else if (!this.exists(monitorableID)) return null;
    	
    	return new Monitorable(monitorableID);
    }
    
    public Variable getStatusVariable(String fullVarPath) {
    	if (fullVarPath == null) return null;
    	else if (fullVarPath.indexOf("/") == -1) return null;
    	else if (this.ma == null) return null;
    	
    	String[] pathParts = fullVarPath.split("/");
    	Monitorable m = this.get(pathParts[0]);
    	if (m == null) return null;
    	
    	Variable v = m.get(pathParts[1]);
    	return v;
    }
    
    public String[] getMonitorableNames() {
    	if (this.ma == null) return null;
    	return this.ma.getMonitorableNames();
    }
    
    public List<Monitorable> getMonitorables() {
    	return this.getMonitorables(this.getMonitorableNames());
    }
    
    public List<Monitorable> getMonitorables(String[] monitorableIDs) {
    	return this.getBundleMonitorables(null, monitorableIDs);
    }
    
    public List<Monitorable> getBundleMonitorables(Integer bundleID, String[] monitorableIDs) {
    	if (this.ma == null) return null;
    	
    	final ArrayList<Monitorable> monitorables = new ArrayList<Monitorable>();    	

    	if (monitorableIDs == null) monitorableIDs = this.getMonitorableNames();
    	if (monitorableIDs != null) {
    		for (String monitorableID : monitorableIDs) {
    			Monitorable m = this.get(monitorableID);
    			if (m != null) {
    				if (bundleID == null || bundleID.intValue() == m.getBundleID().intValue()) {
    					monitorables.add(m);
    				}
    			}
    		}
    	}
    	
    	return monitorables;
    }
    
    public boolean hasBundleMonitorables(Integer bundleID) {
    	return this.getBundleMonitorables(bundleID).size() > 0;
    }
    
    public boolean hasBundleMonitorables(Bundle bundle) {
    	return this.hasBundleMonitorables(Integer.valueOf((int)bundle.getBundleId()));
    }    
    
    public List<Monitorable> getBundleMonitorables(Integer bundleID) {   	
    	return this.getBundleMonitorables(bundleID, null);
    }
	
    /**
     * Internal class used to enable an elegant syntax for accessing
     * resources.
     */
    public final class Monitorable {
    	private static final String MONITORABLE_LOCALIZATION = "Monitorable-Localization";
		private final String monitorableID;    	
    	
    	public Monitorable(String monitorableID) {
    		this.monitorableID = monitorableID;
		}
    	
    	public boolean exists(String variableID) {
    		if (variableID == null) return false;
    		
    		String[] variableNames = ma.getStatusVariableNames(this.monitorableID);
    		if (variableNames == null || variableNames.length == 0) return false;
    		
    		for (String variableName : variableNames) {
    			if (variableName.equals(variableID)) {
    				return true;
    			}
    		}
    		return false;
    	}
    	
    	public Variable get(String variableID) {
    		if (variableID == null) return null;
    		else if (!this.exists(variableID)) return null;
    		
    		try {
    			return new Variable(variableID);
    		} catch (IllegalArgumentException e) {
    			// unknown variable? 
    			return null;
    		}
    	}
    	
    	public String getID() {
    		return this.monitorableID;
    	}
    	
    	public Bundle getBundle() {
    		ServiceReference[] refs = null;
    		try {
    			refs = context.getAllServiceReferences(
    					"org.osgi.service.monitor.Monitorable",
    					String.format("(service.pid=%s)",this.monitorableID)
    			);
    			if (refs != null && refs.length > 0) {
    				ServiceReference serviceRef = refs[0];
    				return serviceRef.getBundle();
    			}
    			return null;
    		} catch (Exception e) {
    			return null;
    		} finally {
    			if (refs != null) for(ServiceReference ref : refs) context.ungetService(ref);
    		}	
    	}
    	
    	public Long getBundleID() {
    		Bundle b = this.getBundle();
    		return (b == null) ? Long.valueOf(-1) : Long.valueOf(b.getBundleId());
    	}
    	
    	public ResourceBundle getResourceBundle(Locale locale) {
    		String bundleBase = null;
    		Bundle osgiBundle = null;
    		
    		ServiceReference[] refs = null;
    		try {
    			refs = context.getAllServiceReferences(
    					"org.osgi.service.monitor.Monitorable",
    					String.format("(service.pid=%s)",this.monitorableID)
    			);
    			if (refs != null && refs.length > 0) {
    				ServiceReference serviceRef = refs[0];
    				
    				// getting the bundle-name to use
    				bundleBase = (String) serviceRef.getProperty(MONITORABLE_LOCALIZATION);
    				if (bundleBase == null) {
    					// fallback to bundle localization header
    					bundleBase = (String) serviceRef.getBundle().getHeaders().get(Constants.BUNDLE_LOCALIZATION);
    				}
    				
    				// getting the osgi-bundle to use
    				osgiBundle = serviceRef.getBundle();
    			}
    			
    			if (bundleBase == null) return null;
    			else if (osgiBundle == null) return null;
    			
    			// loading the resource-bundle
				return rbTool.getLocalization(osgiBundle,bundleBase, locale);
    		} catch (Exception e) {
    			return null;
    		} finally {
    			if (refs != null) for(ServiceReference ref : refs) context.ungetService(ref);
    		}
    	}
    	
    	public String[] getVariableNames() {
    		return ma.getStatusVariableNames(this.monitorableID);
    	}
    	
    	public Variable[] getVariables() {
    		return this.getVariables(this.getVariableNames());
    	}
    	
    	public Variable[] getVariables(String[] variableIDs) {
        	ArrayList<Variable> variables = new ArrayList<Variable>();    	

        	if (variableIDs == null) variableIDs = this.getVariableNames();
        	if (variableIDs != null) {
        		for (String variableID : variableIDs) {
        			Variable m = this.get(variableID);
        			if (m != null) variables.add(m);
        		}
        	}
        	
        	return variables.size() == 0 ? null : variables.toArray(new Variable[variables.size()]);
    	}
    	
        public final class Variable {
        	private final String variableID;
        	private final StatusVariable var;
        	
        	public Variable(String variableID) {
        		this.variableID = variableID;
        		this.var = ma.getStatusVariable(this.getPath());
        	}
        	
        	@Override
        	public String toString() {
        		return this.getValue();
        	}
        	
        	public String getValue() {
        		switch (this.var.getType()) {
        			case StatusVariable.TYPE_INTEGER:
        				return Integer.toString(this.var.getInteger());

        			case StatusVariable.TYPE_FLOAT:
        				return Float.toString(this.var.getFloat());  				
        				
    				case StatusVariable.TYPE_BOOLEAN: 
    					return Boolean.toString(this.var.getBoolean());					
    						
    				default:
    					return this.var.getString();
    			}    		
        	}
        	
        	public Number getNumber() {
        		switch (this.var.getType()) {
	    			case StatusVariable.TYPE_INTEGER:
	    				return Integer.valueOf(this.var.getInteger());
	
	    			case StatusVariable.TYPE_FLOAT:
	    				return Float.valueOf(this.var.getFloat());
	    				
	    			default:
	    				return null;
        		}
        	}
        	
        	public String getPath() {
        		return monitorableID + "/" + this.variableID;
        	}
        	
        	public String getID() {
        		return this.var.getID();
        	}
        	
        	public int getType() {
        		return this.var.getType();
        	}
        	
        	public String getTypeName() {
	        	switch (this.var.getType()) {
	    			case StatusVariable.TYPE_INTEGER:
	    				return Integer.class.getSimpleName();
	
	    			case StatusVariable.TYPE_FLOAT:
	    				return Float.class.getSimpleName();  				
	    				
					case StatusVariable.TYPE_BOOLEAN: 
						return Boolean.class.getSimpleName();					
							
					default:
						return String.class.getSimpleName();
				}   
        	}
        	
        	public Date getTimeStamp() {
        		return this.var.getTimeStamp();
        	}
        	
        	public int getCollectionMethod() {
        		return this.var.getCollectionMethod();
        	}
        	
        	public String getDescription() {
        		return this.getDescription(getLocale());
        	}
        	
        	public String getDescription(Locale locale) {
        		if (locale == null) locale = Locale.ENGLISH;

        		final ResourceBundle rb = getResourceBundle(locale);
        		if (rb != null) {
	        		// trying to use the key "<monitorableID>/<variableID>
	        		try {
	        			return rb.getString(this.getPath());
	        		} catch (MissingResourceException e) { }
	        		
	        		// trying to use the key "<variableID>"
	        		try {
	        			return rb.getString(this.variableID);
	        		} catch (MissingResourceException e) { }
        		}
        		
        		// using the default description
        		return ma.getDescription(this.getPath());
        	}
        }
    }
}
