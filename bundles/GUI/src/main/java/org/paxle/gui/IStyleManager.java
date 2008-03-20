package org.paxle.gui;

import java.io.File;
import java.util.Collection;

public interface IStyleManager {
	public void searchForStyles();
	public void setStyle(String name);
	public Collection<String> getStyles();	
	public File getDataPath();
}
