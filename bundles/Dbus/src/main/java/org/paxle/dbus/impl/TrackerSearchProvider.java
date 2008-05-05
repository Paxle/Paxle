package org.paxle.dbus.impl;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.freedesktop.Tracker;
import org.freedesktop.Tracker.Metadata;
import org.freedesktop.Tracker.Search;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.paxle.core.doc.Field;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.core.doc.IndexerDocument;
import org.paxle.dbus.IDbusService;
import org.paxle.se.index.IFieldManager;
import org.paxle.se.query.tokens.AToken;
import org.paxle.se.search.ISearchProvider;

public class TrackerSearchProvider implements ISearchProvider, IDbusService {
	public static final String TRACKER_BUSNAME = "org.freedesktop.Tracker";
	public static final String TRACKER_OBJECTPATH = "/org/freedesktop/tracker";

	public static final String SERVICE_FILE = "File";
	public static final String FILE_NAME = SERVICE_FILE + ":Name";
	public static final String FILE_LINK = SERVICE_FILE + ":Link";
	public static final String FILE_MIME = SERVICE_FILE + ":Mime";
	public static final String FILE_SIZE = SERVICE_FILE + ":Size";
	public static final String FILE_MODIFIED = SERVICE_FILE + ":Modified";
	
	public static ArrayList<String> fileProperties = new ArrayList<String>(Arrays.asList(new String[]{
			FILE_NAME, 
			FILE_MIME, 
			FILE_SIZE,
			FILE_MODIFIED
	}));
	
	public static HashMap<String, Field> propToFieldMapper = new HashMap<String, Field>();
	static {
		propToFieldMapper.put(FILE_NAME, IIndexerDocument.TITLE);
		propToFieldMapper.put(FILE_MIME, IIndexerDocument.MIME_TYPE);
		propToFieldMapper.put(FILE_SIZE, IIndexerDocument.SIZE);
		propToFieldMapper.put(FILE_MODIFIED, IIndexerDocument.LAST_MODIFIED);
	}
	
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
	
	private Tracker tracker = null;
	
	private Search search = null;
	
	private Metadata metadata = null;
	
	private IFieldManager fieldManager = null;
	
	private int searchID = 0;
	
	public TrackerSearchProvider() throws DBusException {
		// connect to dbus
		conn = DBusConnection.getConnection(DBusConnection.SESSION); 
		
		this.tracker = conn.getRemoteObject(TRACKER_BUSNAME, TRACKER_OBJECTPATH, Tracker.class);
		// TODO: test if we are supporting the given tracker version
		System.out.println(tracker.GetVersion());
		
		this.search = conn.getRemoteObject(TRACKER_BUSNAME, TRACKER_OBJECTPATH, Tracker.Search.class);	
		this.metadata = conn.getRemoteObject(TRACKER_BUSNAME, TRACKER_OBJECTPATH, Tracker.Metadata.class);
	}
	
	public void disconnect() {
		this.conn.disconnect();
	}
	
	@SuppressWarnings("unchecked")
	public void search(AToken token, List<IIndexerDocument> results, int maxCount, long timeout) throws IOException, InterruptedException {
		long start = System.currentTimeMillis();
		try {
			String request = TrackerQueryFactory.transformToken(token, new TrackerQueryFactory());
			
			List<String> result = this.search.Text(searchID++, Tracker.SERVICE_FILES, request, 0, maxCount);
			if (result != null) {
				for (String uri : result) {
					// check if we need to hurry up
					if (System.currentTimeMillis()-start >= timeout-500) break;
					
					IIndexerDocument indexerDoc = new IndexerDocument();
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
