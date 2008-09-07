package org.paxle.gui.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.http.HttpContext;
import org.osgi.service.metatype.AttributeDefinition;
import org.osgi.service.metatype.MetaTypeProvider;
import org.osgi.service.metatype.ObjectClassDefinition;
import org.paxle.gui.IStyleManager;


public class StyleManager implements IStyleManager, MetaTypeProvider, ManagedService {
	public static final String PID = IStyleManager.class.getName();
	
	private static final String PROP_STYLE = "style";

	/** 
	 * for logging
	 */
	private Log logger = LogFactory.getLog( StyleManager.class);

	/**
	 * A manager to manage http servlets and resources.
	 */
	private ServletManager servletManager = null;
	
	/**
	 * Path where all downloaded or installed styles are located
	 */
	private File dataPath = null;

	/** HashMap containing available styles */
	private final HashMap<String, File> styles = new HashMap<String, File>();	

	public StyleManager(File dataPath, ServletManager servletManager) {
		if (dataPath == null) throw new NullPointerException("The datapath is null");
		if (servletManager == null) throw new NullPointerException("ServletManager is null");
		
		if (!dataPath.exists()) dataPath.mkdirs();
		this.dataPath = dataPath;
		this.servletManager = servletManager;
		
		// search for available styles
		this.searchForStyles();
		
		// load the current style for now
		this.setStyle("default");
	}
	
	public File getDataPath() {
		return this.dataPath;
	}

	public Collection<String> getStyles() {
		return Collections.unmodifiableCollection(styles.keySet());
	}

	public void searchForStyles() {
		// create temp map to remember found styles
		HashMap<String, File> temp = new HashMap<String, File>();

		// do a directory listing fo find all files
		File[] files = this.dataPath.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				temp.put( files[i].getName(), files[i]);
			}
		}

		this.styles.clear();
		this.styles.putAll(temp);
	}


	public void setStyle(String name) {
		if( name.equals( "default")) {

			this.servletManager.unregisterAllResources();			
			this.servletManager.addResources("/css","/resources/templates/layout/css");
			this.servletManager.addResources("/js","/resources/js");			
			this.servletManager.addResources("/images", "/resources/images");

			return;
		}
		
		try {
			File styleFile = new File(this.dataPath,name);
			HttpContext httpContextStyle = new HttpContextStyle(styleFile);
			
			JarFile styleJarFile = new JarFile(styleFile);
			Enumeration<?> jarEntryEnum = styleJarFile.entries();

			while (jarEntryEnum.hasMoreElements()) {
				JarEntry entry = (JarEntry) jarEntryEnum.nextElement();
				if (entry.isDirectory()) {
					String alias = "/" + entry.getName().substring( 0, entry.getName().length() - 1);
					servletManager.removeResource( alias);
					servletManager.addResources( alias, alias, httpContextStyle);
				}
			}
		} catch (IOException e) {
			logger.error( "io: " + e);
			e.printStackTrace();
		}
		return;
	}

	/**
	 * @see MetaTypeProvider#getLocales()
	 */
	public String[] getLocales() {
		return new String[]{Locale.ENGLISH.getLanguage()};
	}

	/**
	 * This function dynamically generates the metatype description of the configuration options of this 
	 * {@link ManagedService}. This data is displayed in the configuraton dialog.
	 * 
	 * @see MetaTypeProvider#getObjectClassDefinition(String, String)
	 * 
	 * TODO: localization required here
	 */
	public ObjectClassDefinition getObjectClassDefinition(String id, String localeStr) {
		// refresh the style list
		this.searchForStyles();
		
		Locale locale = (localeStr==null) ? Locale.ENGLISH : new Locale(localeStr);
		final ResourceBundle rb = ResourceBundle.getBundle("localization/" + IStyleManager.class.getSimpleName(), locale);
		
		// create metadata
		ObjectClassDefinition ocd = new ObjectClassDefinition() {
			public AttributeDefinition[] getAttributeDefinitions(int filter) {
				return new AttributeDefinition[]{
						new AttributeDefinition() {
							public int getCardinality() {
								return 0;
							}

							public String[] getDefaultValue() {
								return new String[]{"default"};
							}

							public String getDescription() {
								return rb.getString("styleManager.style.desc");
							}

							public String getID() {
								return PROP_STYLE;
							}

							public String getName() {
								return rb.getString("styleManager.style.name");
							}

							public String[] getOptionLabels() {
								ArrayList<String> labels = new ArrayList<String>();
								labels.addAll(styles.keySet());
								labels.add(rb.getString("styleManager.style.default.name"));
								return labels.toArray(new String[styles.size()]);
							}

							public String[] getOptionValues() {
								ArrayList<String> values = new ArrayList<String>();
								values.addAll(styles.keySet());
								values.add("default");
								return values.toArray(new String[styles.size()]);
							}

							public int getType() {
								return AttributeDefinition.STRING;
							}

							public String validate(String value) {
								return null;
							}							
						}
				};
			}

			public String getDescription() {
				return rb.getString("styleManager.desc");
			}

			public String getID() {
				return IStyleManager.class.getName();
			}

			public InputStream getIcon(int size) throws IOException {
				return (size == 16) 
					? this.getClass().getResourceAsStream("/resources/images/palette.png")
					: null;
			}

			public String getName() {
				return rb.getString("styleManager.name");
			}
		};

		return ocd;
	}

	/**
	 * @return the default configuration of this service
	 */
	public Hashtable<String,Object> getDefaults() {
		Hashtable<String,Object> defaults = new Hashtable<String,Object>();
		defaults.put(PROP_STYLE, "default");
		defaults.put(Constants.SERVICE_PID, PID);
		return defaults;
	}
	
	/**
	 * Updates the manager with the configuration changed by the user
	 * @see ManagedService#updated(Dictionary)
	 */
	@SuppressWarnings("unchecked")
	public void updated(Dictionary configuration) throws ConfigurationException {
		if (configuration == null ) {
			/*
			 * Generate default configuration
			 */
			configuration = this.getDefaults();
		}
		
		// getting the configured style
		String style = (String) configuration.get(PROP_STYLE);
		this.setStyle(style);
	}

}
