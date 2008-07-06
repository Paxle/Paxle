package org.paxle.core.queue;

import java.net.URI;
import java.util.regex.Pattern;

public class CommandProfile implements ICommandProfile {
	/**
	 * Primary key required by Object-EER mapping 
	 */
	private int _oid;
	
	/**
	 * Defines how many childs of the starting-{@link URI locations}
	 * should be processed.
	 */
	private int maxDepth = 0;
	
	/**
	 * The name of this profile
	 */
	private String name = null;
	
	/**
	 * Specifies which mode is used to filter links:
	 * <table>
	 * <tr><td><code>none</code></td><td>filtering disabled</td></tr>
	 * <tr><td><code>regexp</code></td><td>filtering using regular expressions</td></tr>
	 * </table>
	 */
	private LinkFilterMode linkFilterMode = LinkFilterMode.none;
	
	/**
	 * The expression that is used to filter links. For {@link LinkFilterMode#none} this value is <code>null</code>, 
	 * for {@link LinkFilterMode#regexp} this is a valid {@link Pattern} in {@link String}-format.
	 */
	private String linkFilterExpression = null;
	
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
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		
		buf.append("[").append(this._oid).append("] ")
		   .append(this.name).append(": ")
		   .append("maxDepth=").append(this.maxDepth).append(", ")
		   .append("filter=").append(this.linkFilterMode).append(", ")
		   .append("filterExp=").append(this.linkFilterExpression==null?"":this.linkFilterExpression);
		
		return buf.toString();
	}	
}
