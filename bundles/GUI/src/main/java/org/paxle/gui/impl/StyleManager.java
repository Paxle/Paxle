package org.paxle.gui.impl;

import java.util.HashMap;
import java.util.Vector;

public class StyleManager
{
	/** Vector containing available styles */
	private Vector<Class> styles = new Vector<Class>();

	
	public static HashMap<String, String> getStyles()
	{
		HashMap<String, String> tmp = new HashMap<String, String>();
		tmp.put( "test", "style.jar");
		tmp.put( "default", "ugly.jar");
		

		return tmp;
	}
	
	private void searchForStyles()
	{
		//TODO
	}
	
	
	private void updateStyle()
	{
		//TODO
	}
	
	public void setStyle()
	{
		return;
	}
	
}


