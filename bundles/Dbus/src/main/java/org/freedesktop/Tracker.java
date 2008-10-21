/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 * 
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.freedesktop;

import java.util.List;
import java.util.Map;

import org.freedesktop.dbus.DBusInterface;

/**
 * @see <a href="http://www.gnome.org/projects/tracker/">Homepage</a>
 * @see <a href="http://svn.gnome.org/viewvc/tracker/trunk/src/trackerd/tracker-dbus-methods.c?view=markup">tracker-dbus-methods.c</a>
 */
public interface Tracker extends DBusInterface {
	public static final String SERVICE_FILES = "Files"; 
	
	/**
	 * @see <a href="http://svn.gnome.org/viewvc/tracker/trunk/src/trackerd/tracker-dbus-metadata.c?view=markup">tracker-dbus-metadata.c</a>
	 */
	public interface Metadata extends DBusInterface {
		/**
		 * <pre>
		 * <!-- Retrieves an array of metadata values for the specified array of metadata keys for a service and id pair-->
		 * <method name="Get">
		 * 			<arg type="s" name="service" direction="in" />
		 * 			<arg type="s" name="id" direction="in" />
		 * 			<arg type="as" name="keys" direction="in" />
		 * 			<arg type="as" name="values" direction="out" />
		 * </method>
		 * </pre>
		 */
		public List<String> Get(String service, String uri, List<String> keys);

		/**
		 * <pre>
		 * <!-- returns an array of all metadata types that are registered for a certain class -->
		 * <method name="GetRegisteredTypes">
		 * 			<arg type="s" name="metadata_class" direction="in" />
		 * 			<arg type="as" name="result" direction="out" />
		 * </method>
		 * </pre>
		 */
		public List<String> GetRegisteredTypes(String metadataClass);
		
		/**
		 * <pre>
		 * <!-- returns an array of all metadata type classes that are registered -->
		 * <method name="GetRegisteredClasses">
		 * 		<arg type="as" name="result" direction="out" />
		 * </method>
		 * </pre>
		 */
		public List<String> GetRegisteredClasses();
	}
	
	/**
	 * @see <a href="http://svn.gnome.org/viewvc/tracker/trunk/src/trackerd/tracker-dbus-search.c?view=markup">tracker-dbus-search.c</a>
	 */
	public interface Search extends DBusInterface {		
		/**
		 * <pre>
		 * <!-- searches specified service for entities that match the specified search_text. Returns uri of all hits. -->
		 * <method name="Text">
		 * 			<arg type="i" name="live_query_id" direction="in" />
		 * 			<arg type="s" name="service" direction="in" />
		 * 			<arg type="s" name="search_text" direction="in" />
		 * 			<arg type="i" name="offset" direction="in" />
		 * 			<arg type="i" name="max_hits" direction="in" />
		 * 			<arg type="as" name="result" direction="out" />
		 * 	</method>
		 * </pre>
		 * 
		 * @return uri of all hits
		 */
		public List<String> Text(int liveQueryId, String service, String searchText, int offset, int maxHits);
		
		/**
		 * <pre>
		 * <method name="TextDetailed">
		 * 			<arg type="i" name="live_query_id" direction="in" />
		 * 			<arg type="s" name="service" direction="in" />
		 * 			<arg type="s" name="search_text" direction="in" />
		 * 			<arg type="i" name="offset" direction="in" />
		 * 			<arg type="i" name="max_hits" direction="in" />
		 * 			<arg type="aas" name="result" direction="out" />
		 * </method>
		 * </pre>
		 * 
		 * @return hits in array format [uri, service, mime]
		 */
		public List<List<String>> TextDetailed(int liveQueryId, String service, String searchText, int offset, int maxHits);
		
		/**
		 * <pre>
		 * <!-- searches all file based entities that match the specified search_text.
		 *      Returns dict/hashtable with the uri as key and the following fields as the 
		 *      variant part in order: file service category, File:Format, File:Size, File:Rank, File:Modified
		 *      If group_results is True then results are sorted and grouped by service type.
		 * -->
		 * <method name="FilesByText">
		 * 			<arg type="i" name="live_query_id" direction="in" />
		 * 			<arg type="s" name="search_text" direction="in" />
		 * 			<arg type="i" name="max_hits" direction="in" />
		 * 			<arg type="b" name="group_results" direction="in" />
		 * 			<arg type="a{sv}" name="result" direction="out" />
		 * </method>
		 * </pre>
		 * 
		 * FIXME: seems not to work!
		 */
		public Map<String,List<String>> FilesByText(int liveQueryId, String query, int offset, int maxHits, boolean groupResults);
		
		/**
		 * <pre>
		 * <!-- Returns a search snippet of text with matchinhg text enclosed in bold tags -->
		 * 		<method name="GetSnippet">
		 * 			<arg type="s" name="service" direction="in" />
		 * 			<arg type="s" name="id" direction="in" />
		 * 			<arg type="s" name="search_text" direction="in" />
		 * 			<arg type="s" name="result" direction="out" />
		 * </method>
		 * </pre>
		 * @return the sinippet text
		 */
		public String GetSnippet(String service, String uri, String searchText);
	}

	/**
	 * <pre>
	 * <method name="GetVersion">
	 * 	<arg type="i" name="version" direction="out" />
	 * </method>
	 * </pre>
	 * @return the tracker version
	 */
	public int GetVersion();
	
	/**
	 * <pre>
	 * <method name="GetServices">
	 * 			<arg type="b" name="main_services_only" direction="in" />
	 * 			<arg type="a{sv}" name="result" direction="out" />
	 * </method>
	 * </pre>
	 */
	public Map<String,List<String>> GetServices(boolean mainServicesOnly);
}
