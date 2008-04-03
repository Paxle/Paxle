package org.paxle.core.queue;

import java.net.URI;

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
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		
		buf.append("[").append(this._oid).append("] ")
		   .append(this.name).append(": ")
		   .append("maxDepth=").append(this.maxDepth);
		
		return buf.toString();
	}
}
