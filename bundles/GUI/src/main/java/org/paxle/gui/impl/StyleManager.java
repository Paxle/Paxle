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
package org.paxle.gui.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Services;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.http.HttpContext;
import org.osgi.service.metatype.AttributeDefinition;
import org.osgi.service.metatype.MetaTypeProvider;
import org.osgi.service.metatype.ObjectClassDefinition;
import org.paxle.core.io.IResourceBundleTool;
import org.paxle.gui.IServletManager;
import org.paxle.gui.IStyleManager;

@Component(immediate=true, metatype=false, name=StyleManager.PID)
@Services({
	@Service(IStyleManager.class),
	@Service(MetaTypeProvider.class)
})
public class StyleManager implements IStyleManager, MetaTypeProvider {
	public static final String PID = "org.paxle.gui.IStyleManager";	
	private static final String PROP_STYLE = PID + '.' + "style";

	/** 
	 * for logging
	 */
	private Log logger = LogFactory.getLog( StyleManager.class);

	@Reference
	private IResourceBundleTool resourceBundleTool;

	/**
	 * A manager to manage http servlets and resources.
	 */
	@Reference
	private IServletManager servletManager;

	/**
	 * Path where all downloaded or installed styles are located
	 */
	private File dataPath = null;

	/**
	 * A list of {@link Locale} for which a {@link ResourceBundle} exists
	 * @see MetaTypeProvider#getLocales()
	 */
	private String[] locales;	

	/** HashMap containing available styles */
	private final HashMap<String, File> styles = new HashMap<String, File>();	

	protected void activate(Map<String, Object> props) {
		// the supported locales
		this.locales = this.resourceBundleTool.getLocaleArray(IStyleManager.class.getSimpleName(), Locale.ENGLISH);

		// the data-path to use
		final String dataPathName = System.getProperty("paxle.data") + File.separatorChar + "styles";
		this.dataPath = new File(dataPathName);	
		if (!dataPath.exists()) {
			if (!dataPath.mkdirs()) {
				this.logger.error("Unable to create stylesheet-manager directory: " + dataPath);
			}
		}

		// search for available styles
		this.searchForStyles();

		// getting the style to use
		String style = null;
		if (props != null) 
			style = (String) props.get(PROP_STYLE);

		// load the current style for now
		this.setStyle(style==null?"default":style);
	}

	protected void deactivate() {
		this.styles.clear();
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
		if(name.equals( "default")) {

			((ServletManager)this.servletManager).unregisterAllResources();			
			((ServletManager)this.servletManager).addResources("/css","/resources/templates/layout/css");
			((ServletManager)this.servletManager).addResources("/js","/resources/js");			
			((ServletManager)this.servletManager).addResources("/images", "/resources/images");

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
					((ServletManager)this.servletManager).removeResource( alias);
					((ServletManager)this.servletManager).addResources( alias, alias, httpContextStyle);
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
		return this.locales==null?null:this.locales.clone();
	}

	/**
	 * This function dynamically generates the metatype description of the configuration options of this 
	 * {@link ManagedService}. This data is displayed in the configuraton dialog.
	 * 
	 * @see MetaTypeProvider#getObjectClassDefinition(String, String)
	 */
	public ObjectClassDefinition getObjectClassDefinition(String id, String localeStr) {
		// refresh the style list
		this.searchForStyles();

		Locale locale = (localeStr==null) ? Locale.ENGLISH : new Locale(localeStr);
		final ResourceBundle rb = this.resourceBundleTool.getLocalization(IStyleManager.class.getSimpleName(), locale);

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
				return PID;
			}

			public InputStream getIcon(int size) throws IOException {
				return (size == 16) 
				? this.getClass().getResourceAsStream("/OSGI-INF/images/palette.png")
						: null;
			}

			public String getName() {
				return rb.getString("styleManager.name");
			}
		};

		return ocd;
	}
}
