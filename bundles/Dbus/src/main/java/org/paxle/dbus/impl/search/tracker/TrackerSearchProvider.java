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
package org.paxle.dbus.impl.search.tracker;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.freedesktop.Tracker;
import org.freedesktop.DBus.Error.NoReply;
import org.freedesktop.Tracker.Metadata;
import org.freedesktop.Tracker.Search;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.osgi.service.component.ComponentContext;
import org.paxle.core.doc.Field;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.se.index.IFieldManager;
import org.paxle.se.search.ISearchProvider;
import org.paxle.se.search.ISearchProviderContext;
import org.paxle.se.search.ISearchRequest;
import org.paxle.se.search.SearchProviderContext;

@Component(immediate=false)
@Service(ISearchProvider.class)
public class TrackerSearchProvider implements ISearchProvider {
	public static final String TRACKER_BUSNAME = "org.freedesktop.Tracker";
	public static final String TRACKER_OBJECTPATH = "/org/freedesktop/tracker";

	public static final String SERVICE_FILE = "File";
	public static final String FILE_NAME = SERVICE_FILE + ":Name";
	public static final String FILE_LINK = SERVICE_FILE + ":Link";
	public static final String FILE_MIME = SERVICE_FILE + ":Mime";
	public static final String FILE_SIZE = SERVICE_FILE + ":Size";
	public static final String FILE_MODIFIED = SERVICE_FILE + ":Modified";
	
	@SuppressWarnings("serial")
	private static ArrayList<String> fileProperties = new ArrayList<String>() {{
			add(FILE_NAME); 
			add(FILE_MIME); 
			add(FILE_SIZE);
			add(FILE_MODIFIED);
	}};
	
	@SuppressWarnings("serial")
	private static HashMap<String, Field<?>> propToFieldMapper = new HashMap<String, Field<?>>(){{
		put(FILE_NAME, IIndexerDocument.TITLE);
		put(FILE_MIME, IIndexerDocument.MIME_TYPE);
		put(FILE_SIZE, IIndexerDocument.SIZE);
		put(FILE_MODIFIED, IIndexerDocument.LAST_MODIFIED);
	}};
	
//	public static ArrayList<String> properties = new ArrayList<String>(Arrays.asList(new String[]{
////			"DC:Title",
////			"DC:Creator",
////			"DC:Language",
////			"DC:Keywords",
////			"DC:Description",
////			"DC:Type",
//			"File:Name", 
//			"File:Link", 
//			"File:Mime", 
//			"File:Size",
//			"File:Modified"
////			"Doc:Title",
////			"Doc:Subject",
////			"Doc:Author",
////			"Doc:Keywords",
////			"Doc:Comments"
//	}));
	
	/**
	 * The connection to the dbus
	 */
	private DBusConnection conn = null;
	
	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * A reference to the Tracker service
	 */
	private Tracker tracker = null;
	
	/**
	 * A reference to the Tracker-search service
	 */
	private Search search = null;
	
	/**
	 * A reference to the Tracker-metadata service
	 */
	private Metadata metadata = null;
	
	/**
	 * A reference to the paxle field-manager
	 */
	@Reference
	protected IFieldManager fieldManager;
	
	private AtomicInteger searchID = new AtomicInteger(0);
	
	protected void activate(ComponentContext context) throws DBusException {
		try {
			// connect to dbus
			this.logger.info(String.format("Connecting to dbus ..."));
			this.conn = DBusConnection.getConnection(DBusConnection.SESSION); 

			this.logger.info(String.format("Getting reference to %s ...",TRACKER_BUSNAME));
			this.tracker = conn.getRemoteObject(TRACKER_BUSNAME, TRACKER_OBJECTPATH, Tracker.class);
			this.logger.info(String.format("%s version %d detected.",TRACKER_BUSNAME, Integer.valueOf(tracker.GetVersion())));
			// TODO: test if we are supporting the given tracker version
			System.out.println(tracker.GetVersion());

			this.search = conn.getRemoteObject(TRACKER_BUSNAME, TRACKER_OBJECTPATH, Tracker.Search.class);	
			this.metadata = conn.getRemoteObject(TRACKER_BUSNAME, TRACKER_OBJECTPATH, Tracker.Metadata.class);
		} catch (DBusExecutionException e) {
			if (e instanceof NoReply) {
				this.logger.error(String.format("'%s' did not reply within specified time.", TRACKER_BUSNAME));
			} else {
				this.logger.warn(String.format(
						"Unexpected '%s' while trying to connect to '%s'.",
						e.getClass().getName(),
						TRACKER_BUSNAME
				),e);
			}
			
			// disabling this component
			context.disableComponent(this.getClass().getName());
			// XXX: should we re-throw the exceptoin here? 
			// throw e;
		}
	}
	
	protected void deactivate(ComponentContext context) {
		this.conn.disconnect();
	}
	
	
	@SuppressWarnings("unchecked")
	public void search(ISearchRequest searchRequest, List<IIndexerDocument> results) throws IOException, InterruptedException {
		
		long start = System.currentTimeMillis();
		try {
			final ISearchProviderContext context = SearchProviderContext.getCurrentContext();
			final String request = new TrackerQueryFactory().transformToken(searchRequest.getSearchQuery());
			final int maxCount = searchRequest.getMaxResultCount();
			final long timeout = searchRequest.getTimeout();
			
			List<String> result = this.search.Text(this.searchID.incrementAndGet(), Tracker.SERVICE_FILES, request, 0, maxCount);
			if (result != null) {
				for (String uri : result) {
					// check if we need to hurry up
					if (System.currentTimeMillis()-start >= timeout-500) break;
					
					IIndexerDocument indexerDoc = context.createDocument();
					indexerDoc.set(IIndexerDocument.PROTOCOL, "file");
					indexerDoc.set(IIndexerDocument.LOCATION, "file://" + uri);        		

					// load document snippet
					String snippet = this.search.GetSnippet(Tracker.SERVICE_FILES, uri, request);
					if (snippet != null && snippet.length() > 0) {
						indexerDoc.set(IIndexerDocument.SNIPPET,snippet);
					}

					// get document metadata
					List<String> fileProps = this.metadata.Get(Tracker.SERVICE_FILES, uri, fileProperties);
					for (int i=0; i < fileProperties.size(); i++) {
						String propName = fileProperties.get(i);
						String propValue = fileProps.get(i);
						if (propValue != null && propValue.length() > 0) {
							Field propField = propToFieldMapper.get(propName);
							if (propField != null) {
								Class type = propField.getType();
								if (type.equals(String.class)) {
									indexerDoc.set(propField, propValue);
								} else if (type.equals(Long.class)) {
									try {
										if (propValue.endsWith(".0")) propValue = propValue.substring(0,propValue.length()-2);
										Long longValue = Long.valueOf(propValue);
										indexerDoc.set(propField,longValue);
									} catch (NumberFormatException e) {
										e.printStackTrace();
									}
								} else if (type.equals(Date.class)) {
									try {
										SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
										Date dateValue = df.parse(propValue);
										indexerDoc.set(propField,dateValue);
									} catch (ParseException e) {
										e.printStackTrace();
									}
								}
							}
						}
					}
					
					results.add(indexerDoc);
//					String snippet = search.GetSnippet("Files", uri, "test");
//					System.out.println(String.format("%s%n%s",uri,snippet));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
