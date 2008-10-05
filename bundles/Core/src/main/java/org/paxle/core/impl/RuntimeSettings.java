package org.paxle.core.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.metatype.AttributeDefinition;
import org.osgi.service.metatype.MetaTypeProvider;
import org.osgi.service.metatype.ObjectClassDefinition;

/**
 * A component to allow the user to change the java-runtime properties
 * stored in <code>start.ini</code> via the OSGI configuration-management.
 */
public class RuntimeSettings implements MetaTypeProvider, ManagedService {
	public static final String PID = RuntimeSettings.class.getName();
	private static final String CM_XMX = "jvm.xmx";
	private static final String CM_XMS = "jvm.xms";
	
	private static final Map<String,String[]> OPTIONS;
	static {
		final Map<String,String[]> options = new HashMap<String,String[]>();
		options.put(CM_XMX, new String[] { "-Xmx", "128m" });
		options.put(CM_XMS, new String[] { "-Xms", "64m" });
		OPTIONS = Collections.unmodifiableMap(options);
	}

	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(RuntimeSettings.class);
	
	/**
	 * A list of {@link Locale} for which a {@link ResourceBundle} exists
	 * @see MetaTypeProvider#getLocales()
	 */
	private final String[] locales;		
	
	/**
	 * Config file where the runtime-options are stored, e.g.
	 * <code>start.ini</code>
	 */
	private final File iniFile;
	
	public RuntimeSettings(String[] locales) {
		this(locales, new File("start.ini"));
	}
	
	public RuntimeSettings(String[] locales, File iniFile) {
		if (locales == null) throw new NullPointerException("The locale array is null");
		if (iniFile == null) throw new NullPointerException("The ini-file is null");
		
		this.locales = locales;
		this.iniFile = iniFile;
	}	
	
	private synchronized List<String> readSettings() {
		BufferedReader reader = null;
		ArrayList<String> configOptions = new ArrayList<String>();
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(this.iniFile), "UTF-8"));

			String line = null;		
			while ((line = reader.readLine())!=null) {
				configOptions.add(line.trim());
			}

			reader.close();
		} catch (Exception e) {
			this.logger.error("Unable to read settings from file: " + this.iniFile.toString() ,e);
		} finally {
			if (reader != null) try { reader.close(); } catch (Exception e) {/* ignore this */}
		}
		return configOptions;
	}
	
	private synchronized void writeSettings(List<String> runtimeSettings) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.iniFile),"UTF-8"));
			
			if (runtimeSettings != null) {
				for (String item : runtimeSettings) {
					writer.println(item.trim());
				}
			}
			
			writer.flush();
			writer.close();
			writer = null;
		} catch (Exception e) {
			this.logger.error("Unable to write settings to file: " + this.iniFile.toString() ,e);
		} finally {
			if (writer != null) try { writer.close(); } catch (Exception e) {/*ignore this*/}
		}		
	}
	
	/**
	 * @see MetaTypeProvider#getLocales()
	 */
	public String[] getLocales() {
		return this.locales;
	}

	/**
	 * @see MetaTypeProvider#getObjectClassDefinition(String, String)
	 */
	public ObjectClassDefinition getObjectClassDefinition(String id, String localeStr) {
		final Locale locale = (localeStr==null) ? Locale.ENGLISH : new Locale(localeStr);
		final ResourceBundle rb = ResourceBundle.getBundle("OSGI-INF/l10n/" + RuntimeSettings.class.getSimpleName(), locale);		
		
		return new ObjectClassDefinition() {
			public AttributeDefinition[] getAttributeDefinitions(int filter) {
				final ArrayList<AttributeDefinition> attribs = new ArrayList<AttributeDefinition>();
				
				// loading all currently avilable jvm options
				List<String> runtimeSettings = readSettings();
				if (runtimeSettings != null) {
					
					for (String setting : runtimeSettings)
						for (final Map.Entry<String,String[]> e : OPTIONS.entrySet())
							if (setting.startsWith(e.getValue()[0])) {
								attribs.add(new AttributeDefinition(){						
									public String getID() { return PID + '.' + e.getKey(); }
									public int getCardinality() { return 0; }
									public String[] getDefaultValue() { return new String[] { e.getValue()[1] }; }
									public String getDescription() { return rb.getString(e.getKey() + ".desc"); }
									public String getName() { return rb.getString(e.getKey() + ".name"); }
									public String[] getOptionLabels() { return null; }
									public String[] getOptionValues() { return null; }
									public int getType() { return AttributeDefinition.STRING; }
									public String validate(String value) { return null; }
								});
								break;
							}
				}
				
				return attribs.toArray(new AttributeDefinition[attribs.size()]);
			}

			public String getDescription() {
				try {
					return MessageFormat.format(
							rb.getString("runtimeSettings.desc"),
							iniFile.getCanonicalFile().toString()
					);
				} catch (IOException e) {
					logger.error(e);
					return rb.getString("runtimeSettings.desc");
				}
			}

			public String getID() {
				return PID;
			}

			public InputStream getIcon(int size) throws IOException {
				return null;
			}

			public String getName() {				
				return rb.getString("runtimeSettings.name");
			}			
		};				
	}

	/**
	 * @see ManagedService#updated(Dictionary)
	 */	
	@SuppressWarnings("unchecked")
	public void updated(Dictionary properties) throws ConfigurationException {
		if (properties == null) return;
		
		// loading current settings
		List<String> currentSettings = this.readSettings();
		
		boolean changesDetected = false;
		Enumeration<String> keys = properties.keys();
		outer: while(keys.hasMoreElements()) {
			String key = keys.nextElement();
			Object value = properties.get(key);
			
			for (final Map.Entry<String,String[]> e : OPTIONS.entrySet()) {
				if (key.equals(PID + '.' + e.getKey())) {
					changesDetected |= this.updateSetting(currentSettings, e.getValue()[0], (String)value);
					continue outer;
				}
			}
			
			// entry not known
		}
		
		if (changesDetected) {
			// write changes into file
			this.writeSettings(currentSettings);
		}
	}
	
	public Dictionary<?,?> getCurrentIniSettings() {
		final Dictionary<String,Object> props = new Hashtable<String,Object>();
		final List<String> iniSettings = readSettings();
		outer: for (final String s : iniSettings) {
			for (final Map.Entry<String,String[]> e : OPTIONS.entrySet())
				if (s.startsWith(e.getValue()[0])) {
					props.put(PID + '.' + e.getKey(), s.substring(e.getValue()[0].length()));
					continue outer;
				}
			
			// entry not known
		}
		return props;
	}

	private boolean updateSetting(List<String> currentSettings, String prefix, String value) {
		boolean found = false;
		boolean updated = false;
		
		// loop through the settings to find the current value
		for (int i=0; i<currentSettings.size(); i++) {
			String setting = currentSettings.get(i);
			if (setting.startsWith(prefix)) {
				found = true;
				if (!setting.equals(value)) {
					// replace with new value
					currentSettings.set(i, prefix + value);
					updated = true;
				}
			}
		}
		
		if (!found) {
			// just append value
			currentSettings.add(value);
			updated = true;
		}
		
		return updated;
	}
}
 