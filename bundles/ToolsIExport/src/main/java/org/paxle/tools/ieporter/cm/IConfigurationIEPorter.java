package org.paxle.tools.ieporter.cm;

import java.io.File;
import java.util.Dictionary;
import java.util.Map;
import java.util.zip.ZipFile;

import org.osgi.framework.Bundle;

public interface IConfigurationIEPorter {
	public Map<String, Dictionary<String, Object>> importConfigurations(File xmlFile) throws Exception;
	
	/**
	 * @param a map containing the {@link org.osgi.framework.Constants#SERVICE_PID pid} of all registered {@link org.osgi.service.cm.ManagedService services} 
	 * 	 	  whose configuration should be exported as {@link Map.Entry#getKey() key}. 
	 * 		  The {@link Bundle#getLocation()} must be passed in as {@link Map.Entry#getValue() value}
	 * @return a {@link ZipFile} containing an XML-file for each {@link org.osgi.service.cm.ManagedService service} 
	 * @throws Exception
	 */
	public File exportConfigsAsZip(Map<String, String> pidBundleLocationTupel) throws Exception;
}
