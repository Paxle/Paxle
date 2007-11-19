package org.paxle.dbus.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.freedesktop.Tracker;
import org.freedesktop.Tracker.Metadata;
import org.freedesktop.Tracker.Search;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.core.doc.IndexerDocument;
import org.paxle.dbus.IDbusService;
import org.paxle.se.index.IFieldManager;
import org.paxle.se.query.ITokenFactory;
import org.paxle.se.search.ISearchProvider;

public class TrackerSearchProvider implements ISearchProvider, IDbusService {
	public static final String TRACKER_BUSNAME = "org.freedesktop.Tracker";
	public static final String TRACKER_OBJECTPATH = "/org/freedesktop/tracker";

	public static ArrayList<String> properties = new ArrayList<String>(Arrays.asList(new String[]{
//			"DC:Title",
//			"DC:Creator",
//			"DC:Language",
//			"DC:Keywords",
//			"DC:Description",
//			"DC:Type",
			"File:Name", 
			"File:Link", 
			"File:Mime", 
			"File:Size",
			"File:Modified"
//			"Doc:Title",
//			"Doc:Subject",
//			"Doc:Author",
//			"Doc:Keywords",
//			"Doc:Comments"
	}));
	
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
	
	public ITokenFactory getTokenFactory() {
		return new TrackerTokenFactor();
	}

	public void search(String request, List<IIndexerDocument> results, int maxCount, long timeout) throws IOException, InterruptedException {	
		try {
			List<String> result = this.search.Text(searchID++, "Files", request, 0, maxCount);
			if (result != null) {
				for (String uri : result) {
					IIndexerDocument indexerDoc = new IndexerDocument();

					indexerDoc.set(IIndexerDocument.PROTOCOL, "file");
					indexerDoc.set(IIndexerDocument.LOCATION, "file://" + uri);        		

					// load document snippet
					String snippet = this.search.GetSnippet("Files", uri, request);
					indexerDoc.set(IIndexerDocument.SNIPPET,snippet);

//					List<String> a = this.metadata.Get("Files", uri, properties);
//					System.out.println(a);
					
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
