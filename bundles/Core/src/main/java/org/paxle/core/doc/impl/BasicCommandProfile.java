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
package org.paxle.core.doc.impl;

import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.paxle.core.doc.ICommandProfile;


public class BasicCommandProfile implements ICommandProfile {
	/**
	 * Primary key required by Object-EER mapping 
	 */
	protected int _oid;
	
	/**
	 * Defines how many childs of the starting-{@link URI locations}
	 * should be processed.
	 */
	protected int maxDepth = 0;
	
	/**
	 * The name of this profile
	 */
	protected String name = null;
	
	/**
	 * Specifies which mode is used to filter links:
	 * <table>
	 * <tr><td><code>none</code></td><td>filtering disabled</td></tr>
	 * <tr><td><code>regexp</code></td><td>filtering using regular expressions</td></tr>
	 * </table>
	 */
	protected LinkFilterMode linkFilterMode = LinkFilterMode.none;
	
	/**
	 * The expression that is used to filter links. For {@link LinkFilterMode#none} this value is <code>null</code>, 
	 * for {@link LinkFilterMode#regexp} this is a valid {@link Pattern} in {@link String}-format.
	 */
	protected String linkFilterExpression = null;
	
	/**
	 * Other properties, e.g. special properties for command-filters
	 */
	protected Map<String, Serializable> properties = new HashMap<String, Serializable>();
	
    public int getOID(){ 
    	return _oid; 
    }

    public void setOID(int OID){ 
    	this._oid = OID; 
    }

	public int getMaxDepth() {
		return this.maxDepth;
	}

	public void setMaxDepth(int maxDepth) {
		if (maxDepth < 0) throw new IllegalArgumentException("Max-depth must be greater or equal 0.");
		this.maxDepth = maxDepth;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LinkFilterMode getLinkFilterMode() {
		return this.linkFilterMode;
	}

	public void setLinkFilterMode(LinkFilterMode mode) {
		if (mode == null) mode = LinkFilterMode.none;
		this.linkFilterMode = mode;
	}

	public String getLinkFilterExpression() {
		return this.linkFilterExpression;
	}	

	public void setLinkFilterExpression(String expression) {
		this.linkFilterExpression = expression;
	}	
	
	public Map<String, Serializable> getProperties() {
		return this.properties;
	}

	public void setProperties(Map<String, Serializable> props) {
		if (props == null) props = new HashMap<String, Serializable>();
		this.properties = props;
	}		
	
	public Serializable getProperty(String propertyName) {
		if (propertyName == null) return null;
		return this.properties.get(propertyName);
	}

	public void setProperty(String propertyName, Serializable propertyValue) {
		if (propertyName == null) throw new NullPointerException("The property-name must not be null");
		this.properties.put(propertyName, propertyValue);
	}	
	
	public Serializable removeProperty(String propertyName) {
		if (propertyName == null) throw new NullPointerException("The property-name must not be null");
		return this.properties.remove(propertyName);
	}	
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		
		buf.append("[").append(this._oid).append("] ")
		   .append(this.name).append(": ")
		   .append("maxDepth=").append(this.maxDepth).append(", ")
		   .append("filter=").append(this.linkFilterMode).append(", ")
		   .append("filterExp=").append(this.linkFilterExpression==null?"":this.linkFilterExpression).append(", ")
		   .append("props=").append(this.properties==null?"{}":this.properties.toString());
		
		return buf.toString();
	}
}
