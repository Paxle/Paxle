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
package org.paxle.gui.impl.servlets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.paxle.core.io.IOTools;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.gui.ALayoutServlet;
import org.paxle.gui.impl.ServiceManager;
import org.paxle.util.StringTools;

public class BundleView extends ALayoutServlet {
	
	private static final String PARAM_BUNDLE_PATH = "bundlePath";
	private static final String PARAM_INSTALL_URL = "installURL";
	private static final String PARAM_BUNDLE_ID = "bundleID";
	private static final String PARAM_ACTION = "action";
	private static final String PARAM_REPLACE = "replaceExisting";
	
	private static final String ACTION_DETAILS = "details";
	private static final String ACTION_UNINSTALL = "uninstall";
	private static final String ACTION_RESTART = "restart";
	private static final String ACTION_STOP = "stop";
	private static final String ACTION_START = "start";
	private static final String ACTION_UPDATE = "update";
	
	private static final String PATH_UPLOADED = "uploaded-bundles";
	
	private static final long serialVersionUID = 1L;
	
	private static final Map<Integer,String> states = new HashMap<Integer,String>();
	static {
		states.put(Integer.valueOf(Bundle.ACTIVE), "active");
		states.put(Integer.valueOf(Bundle.INSTALLED), "installed");
		states.put(Integer.valueOf(Bundle.RESOLVED), "resolved");
		states.put(Integer.valueOf(Bundle.STARTING), "starting");
		states.put(Integer.valueOf(Bundle.STOPPING), "stopping");
		states.put(Integer.valueOf(Bundle.UNINSTALLED), "uninstalled");
	}
	
	@Override
	public Template handleRequest( HttpServletRequest request,
			HttpServletResponse response,
			Context context ) {
		
		Template template = null;
		try {
			template = this.getTemplate("/resources/templates/BundleView.vm");
			
			ServiceManager manager = (ServiceManager) context.get(SERVICE_MANAGER);
			if (request.getParameter(PARAM_BUNDLE_ID) != null) {
				context.put("stringTools", new StringTools());
				
				long bundleID = Long.parseLong(request.getParameter(PARAM_BUNDLE_ID));
				Bundle bundle = manager.getBundle(bundleID);
				if (bundle == null) {
					String errorMsg = String.format(
							"Bundle with ID '%s' not found.", 
							request.getParameter(PARAM_BUNDLE_ID)
					);
					this.logger.warn(errorMsg);
					context.put("errorMsg",errorMsg);
				} else if (request.getParameter(PARAM_ACTION) != null) {
					boolean doRedirect = false;
					String action = request.getParameter(PARAM_ACTION);
					try {
						if (action.equals(ACTION_DETAILS)) {
							context.put("bundle", bundle);
						} else {
							/* Check user authentication */
				    		if (!isUserAuthenticated(request, response, true)) return null;
							
				    		/* do action */
							if (action.equals( ACTION_UPDATE)) {
								bundle.update();
								doRedirect = true;
							} else if (action.equals( ACTION_START)) {
								bundle.start();
								doRedirect = true;
							} else if (action.equals( ACTION_STOP)) {
								bundle.stop();
								doRedirect = true;
							} else if (action.equals( ACTION_RESTART)) {
								bundle.stop();
								bundle.start();
								doRedirect = true;
							} else if(action.equals( ACTION_UNINSTALL)){
								bundle.uninstall();
								doRedirect = true;
							} else {
								context.put("errorMsg",String.format("Action '%s' not supported.",action));
							}
														
							// redirect to the bundle view
							if (doRedirect) {
								response.sendRedirect(request.getServletPath());
								return null;
							}
						}
					} catch (BundleException e) {
						String errorMsg = String.format(
								"Unexpected exception while doing action '%s' on bundle with ID '%s", 
								action,
								request.getParameter(PARAM_BUNDLE_ID)
						);
						this.logger.warn(errorMsg, e);
						context.put("errorMsg",e.getMessage());
					}
				}
			} else if (request.getParameter(PARAM_INSTALL_URL) != null && request.getParameter(PARAM_BUNDLE_PATH) != null) {
				// Check user authentication
	    		if (!isUserAuthenticated(request, response, true)) return null;
				
	    		// install bundle
				ServiceManager.context.installBundle(request.getParameter(PARAM_BUNDLE_PATH));
			} else if (ServletFileUpload.isMultipartContent(request)) {
				// Check user authentication
	    		if (!isUserAuthenticated(request, response, true)) return null;
	    		
	    		// install bundle
				this.handleFileUpload(request, context);
			}
			
			String filter = (request.getParameter("filter") != null) 
						  ? request.getParameter("filter")
						  : "(Bundle-SymbolicName=org.paxle.*)";
			context.put("filter", filter);
			
			if (filter.equals("(Bundle-SymbolicName=*)")) {
				context.put("bundles", bundles2map(manager.getBundles()));
			} else {
				context.put("bundles", bundles2map(manager.getBundles(filter)));
			}
			context.put("states", states);
		} catch (Throwable e) {
			System.err.println("Exception caught: " + e.getMessage());
			e.printStackTrace();
		}
		
		return template;
	}
	
	private void handleFileUpload(final HttpServletRequest request, final Context context) throws Exception {
		
		// Create a factory for disk-based file items
		FileItemFactory factory = new DiskFileItemFactory();
		
		//  	// Set factory constraints
		//  	factory.setSizeThreshold(yourMaxMemorySize);
		//  	factory.setRepository(yourTempDirectory);
		
		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);
		
		//  	// Set overall request size constraint
		//  	upload.setSizeMax(yourMaxRequestSize);
		
		final boolean replace = request.getParameter(PARAM_REPLACE) != null;
		final BundleUpdater updater = createUpdater(context, replace);
		if (updater == null)
			return;
		
		// Parse the request
		@SuppressWarnings("unchecked")
		List<FileItem> items = upload.parseRequest(request);
		
		// Process the uploaded items
		Iterator<FileItem> iter = items.iterator();
		while (iter.hasNext()) {
			FileItem item = iter.next();
			if (item.isFormField())
				continue;
			
			try {
				if (!item.getFieldName().equals("bundleJar")) {
					String errorMsg = String.format("Unknown file-upload field '%s'.",item.getFieldName());
					this.logger.warn(errorMsg);
					context.put("errorMsg",errorMsg);
					continue;
				}
				
				String fileName = item.getName();
				if (fileName != null) {
					fileName = FilenameUtils.getName(fileName);
				} else {
					String errorMsg = String.format("Fileupload field '%s' has no valid filename.", item.getFieldName());
					this.logger.warn(errorMsg);
					context.put("errorMsg",errorMsg);
					continue;
				}
				
				final InputStream content = item.getInputStream();
				try {
					if (!updater.findAndRemoveOldBundle(fileName, content))
						continue;
				} finally { try { content.close(); } catch (IOException e) { /* ignore */ } }
				
				// write the bundle to disk
				final File targetFile = updater.getSaveLocation(fileName, false);
				if (targetFile == null)
					continue;
				item.write(targetFile);
				
				// installing bundle
				final Bundle bundle = ServiceManager.context.installBundle(targetFile.toURI().toURL().toString());
				
				// start bundle if original bundle's state was started
				if (updater.wasOldBundleStarted())
					bundle.start();
				
			} finally {
				// cleanup
				item.delete();
			}
		}
	}
	
	private BundleUpdater createUpdater(final Context context, final boolean replace) throws IOException {
		final File dir = new File((replace) ? "." : PATH_UPLOADED);
		if (!dir.exists()) {
			final boolean ok = dir.mkdirs();
			if (!ok) {
				final String errorMsg = String.format("Unknown error while creating upload directory '%s', aborting.", dir.getCanonicalPath());
				logger.warn(errorMsg);
				context.put("errorMsg", errorMsg);
				return null;
			}
		} else if (!dir.isDirectory()) {
			final String errorMsg = String.format("Error while accessing upload path '%s': not a directory, aborting.", dir.getCanonicalPath());
			logger.warn(errorMsg);
			context.put("errorMsg", errorMsg);
			return null;
		} else if (!dir.canRead()) {
			final String errorMsg = String.format("Error while accessing upload directory '%s': read permission denied, aborting.", dir.getCanonicalPath());
			logger.warn(errorMsg);
			context.put("errorMsg", errorMsg);
			return null;
		}
		return new BundleUpdater(replace, dir, context);
	}
	
	private static int compareVersions(final String oldVersion, final String newVersion) {
		final StringTokenizer oldSt = new StringTokenizer(oldVersion, ".-_", false);
		final StringTokenizer newSt = new StringTokenizer(newVersion, ".-_", false);
		while (oldSt.hasMoreTokens() && newSt.hasMoreTokens()) {
			final String ot = oldSt.nextToken();
			final String nt = newSt.nextToken();
			
			final boolean isOldNumeric = ot.matches("\\d+");
			final boolean isNewNumeric = nt.matches("\\d+");
			
			if (!isOldNumeric) {
				return (isNewNumeric) ? 1 : 0;
			} else if (!isNewNumeric) {
				return -1;
			} else {
				final int cmp = Integer.valueOf(ot).compareTo(Integer.valueOf(nt));
				if (cmp != 0)
					return cmp;
			}
		}
		return 0;
	}
	
	private class BundleUpdater {
		
		private final boolean replace;
		private final File dir;
		private final ITempFileManager tfm;
		private final Context context;
		
		private HashMap<String,Bundle> bundleLocations = null;
		private HashMap<String,Bundle> bundleSymnames = null;
		private Bundle oldBundle = null;
		private String oldName = null;
		private String oldVersion = null;
		private String newVersion = null;
		private boolean wasStarted = false;
		
		public BundleUpdater(final boolean replace, final File dir, final Context context) {
			tfm = IOTools.getTempFileManager();
			if (tfm == null)
				logger.info("No temp-file manager found, inspection of uploaded bundles is disabled");
			this.replace = replace;
			this.dir = dir;
			this.context = context;
		}
		
		public File getSaveLocation(final String fileName, final boolean overrideOlderVersion) throws IOException {
			File file = new File(dir, fileName);
			if (!file.exists())
				return file;
			
			if (replace) {
				if (oldBundle == null) {
					final String msg = String.format("Targetfile '%s' already exists but the corresponding bundle cannot be found, deleting file...", file.getCanonicalFile().toString());
					logger.warn(msg);
				} else if (compareVersions(oldVersion, newVersion) < 0) {
					// TODO: remove .jar-file only if the new one is really newer
					final String msg = String.format(
							"Version of uploaded bundle is smaller than the version of the installed bundle '%s' (old: %s, new: %s)",
							oldName, oldVersion, newVersion);
					logger.warn(msg);
					if (!overrideOlderVersion) {
						context.put("errorMsg", msg);
						return null;
					}
				}
				
				if (!file.delete()) {
					String errorMsg = String.format("Targetfile '%s' already exists and cannot be deleted.", file.getCanonicalFile().toString());
					logger.warn(errorMsg);
					context.put("errorMsg",errorMsg);
					return null;
				}
				return file;
				
			} else {
				final int idx = fileName.lastIndexOf('.');
				final String name = ((idx == -1)
						? fileName + "_" + System.currentTimeMillis()
						: fileName.substring(0, idx) + "_" + System.currentTimeMillis() + fileName.substring(idx));
				return new File(dir, name);
			}
		}
		
		private void initMaps() {
			// initialize the map of location->bundle and symbolic-name->bundle
			bundleLocations = new HashMap<String,Bundle>();
			bundleSymnames = new HashMap<String,Bundle>();
			for (final Bundle bundle : ServiceManager.context.getBundles()) {
				final String loc = bundle.getLocation();
				bundleLocations.put(loc.substring(loc.lastIndexOf(':') + 1), bundle);
				bundleSymnames.put(bundle.getSymbolicName(), bundle);
			}
		}
		
		public boolean findAndRemoveOldBundle(final String fileName, final InputStream content) throws IOException {
			final String symbolicName;
			if (tfm != null) {
				// check symbolic-name and version of the file
				final File targetTemp = tfm.createTempFile();
				try {
					final FileOutputStream fos = new FileOutputStream(targetTemp);
					try {
						IOTools.copy(content, fos);
					} finally { fos.close(); }
					final JarFile targetJar = new JarFile(targetTemp);
					try {
						final Manifest targetManifest = targetJar.getManifest();
						if (targetManifest == null) {
							// no manifest, this is not a valid bundle
							final String msg = "Uploaded bundle does not specify a Manifest, skipping...";
							logger.warn(msg);
							context.put("errorMsg", msg);
							return false;
						}
						final Attributes attrs = targetManifest.getMainAttributes();
						symbolicName = attrs.getValue(Constants.BUNDLE_SYMBOLICNAME);
						newVersion = attrs.getValue(Constants.BUNDLE_VERSION);
					} finally { try { targetJar.close(); } catch (IOException e) { /* ignore */ } }
				} finally { try { tfm.releaseTempFile(targetTemp); } catch (IOException e) { /* ignore */ } }
				
				// find bundle belonging to the symbolic-name / version
				if (symbolicName != null) {
					if (bundleSymnames == null)
						initMaps();
					
					// stop & uninstall old bundle
					oldBundle = bundleSymnames.get(symbolicName);
				}
			} else {
				symbolicName = null;
			}
			
			logger.info("new bundle's symname: " + symbolicName + ", version: " + newVersion);
			
			if (oldBundle == null) {
				// try to get the corresponding bundle from the filename
				if (bundleLocations == null)
					initMaps();
				oldBundle = bundleLocations.get(fileName);
			}
			
			if (oldBundle != null) {
				wasStarted = (oldBundle.getState() == Bundle.ACTIVE || oldBundle.getState() == Bundle.RESOLVED);
				oldVersion = (String)oldBundle.getHeaders().get(Constants.BUNDLE_VERSION);
				oldName = (String)oldBundle.getHeaders().get(Constants.BUNDLE_NAME);
				logger.info(String.format("Stopping and replacing bundle '%s' due to manual update with file '%s'",
						oldName, fileName));
				
				int servicesUsed = 0;
				for (final ServiceReference ref : oldBundle.getRegisteredServices())
					servicesUsed += ref.getUsingBundles().length;
				if (servicesUsed > 0) {
					final String msg = String.format("Other bundles use %d services of the bundle '%s' to be replaced",
							Integer.valueOf(servicesUsed), oldName);
					logger.warn(msg);
				}
				
				try {
					oldBundle.stop();
					oldBundle.uninstall();
				} catch (BundleException e) {
					final String msg = String.format("The old bundle '%s' cannot be removed: %s", oldName, e.getMessage());
					context.put("errorMsg", msg);
					logger.warn(msg, e);
					return false;
				}
			}
			return true;
		}
		
		public boolean wasOldBundleStarted() {
			return wasStarted;
		}
		
		public Bundle getOldBundle() {
			return oldBundle;
		}
	}
	
	private static TreeMap<Long,Bundle> bundles2map(Bundle[] bundles) {
		final TreeMap<Long,Bundle> r = new TreeMap<Long,Bundle>();
		for (Bundle bundle : bundles)
//			if (bundle.getBundleId() > 0)
				r.put(Long.valueOf(bundle.getBundleId()), bundle);
		return r;
	}
}
