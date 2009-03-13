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
package org.paxle.core.io.temp.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.monitor.Monitorable;
import org.osgi.service.monitor.StatusVariable;
import org.paxle.core.io.temp.ITempDir;
import org.paxle.core.io.temp.ITempFileManager;

public class TempFileManager implements ITempFileManager, Monitorable {
	/* =========================================================
	 * OSGi Monitorable CONSTANTS
	 * ========================================================= */		
	public static final String MONITOR_PID = "org.paxle.tempFileManager";
	private static final String MONITOR_FILES_USED = "files.used";
	private static final String MONITOR_FILES_TOTAL = "files.total";
	
	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());
	
	private int totalCount;
	private int openCount;
	
	/**
	 * The names of all {@link StatusVariable status-variables} supported by this {@link Monitorable}
	 */
	private static final HashSet<String> VAR_NAMES =  new HashSet<String>(Arrays.asList(new String[] {
			MONITOR_FILES_USED,
			MONITOR_FILES_TOTAL
	}));	
	
	/**
	 * Descriptions of all {@link StatusVariable status-variables} supported by this {@link Monitorable}
	 */
	private static final HashMap<String, String> VAR_DESCRIPTIONS = new HashMap<String, String>();
	static {
		VAR_DESCRIPTIONS.put(MONITOR_FILES_USED, "Number of currently used temp-files.");
		VAR_DESCRIPTIONS.put(MONITOR_FILES_TOTAL, "Total number of created temp-files.");
	}		
	
	private final Hashtable<String,ITempDir> classMap = new Hashtable<String,ITempDir>();
	
	/**
	 * A map containing all temp-files managed by this temp-file-manager
	 */
	private final Hashtable<File,ITempDir> fileMap = new Hashtable<File,ITempDir>();
	
	private final ITempDir defaultDir;
	private final boolean deleteOnExit;
	
	public TempFileManager(ITempDir defaultDir, final boolean deleteOnExit) {
		this.defaultDir = defaultDir;
		this.deleteOnExit = deleteOnExit;
	}
	
	public TempFileManager(final boolean deleteOnExit) {
		this(new DefaultTempDir(), deleteOnExit);
	}
	
	public TempFileManager() {
		this(true);
	}
	
	public void removeTempDirFor(String... classNames) {
		for (final String className : classNames)
			this.classMap.remove(className);
	}
	
	public void setTempDirFor(ITempDir dir, String... classNames) {
		for (final String className : classNames)
			this.classMap.put(className, dir);
	}
	
	public File createTempFile() throws IOException {
		final String className = new Exception().getStackTrace()[1].getClassName();		// FIXME: slow!
		ITempDir dir = this.classMap.get(className);
		if (dir == null) dir = defaultDir;
		
		// creating a new temp file
		final File ret = dir.createTempFile(className, ".tmp");
		
		// remember the created temp file for later cleanup
		this.fileMap.put(ret, dir);				
		
		if (deleteOnExit) {
			ret.deleteOnExit();
		}
		
		synchronized (this) {
			this.totalCount++;
			this.openCount++;
		}
		
		return ret;
	}
	
	public boolean isKnown(File file) {
		if (file == null) return false;
		return this.fileMap.containsKey(file);
	}
	
	public void releaseTempFile(File file) throws FileNotFoundException, IOException {
		if (!this.isKnown(file)) return;
		
		final ITempDir dir = this.fileMap.get(file);
		boolean success = ((dir == null) ? defaultDir : dir).releaseTempFile(file);
		
		if (success) {
			synchronized (this) {
				this.openCount--;
			}
		} else {
			this.logger.warn(String.format(
					"Unable to release tempfile '%s'.",
					file
			));
		}
	}

	public String getDescription(String id) throws IllegalArgumentException {
		if (!VAR_NAMES.contains(id)) {
			throw new IllegalArgumentException("Invalid Status Variable name " + id);
		}		
		
		return VAR_DESCRIPTIONS.get(id);
	}

	public StatusVariable getStatusVariable(String id) throws IllegalArgumentException {
		if (!VAR_NAMES.contains(id)) {
			throw new IllegalArgumentException("Invalid Status Variable name " + id);
		}
		
		int val = 0;
		int type = StatusVariable.CM_GAUGE;
		
		if (id.equals(MONITOR_FILES_TOTAL)) {
			val = this.totalCount;
			type = StatusVariable.CM_CC;
		} else if (id.equals(MONITOR_FILES_USED)) {
			val = this.openCount;
		}
		
		return new StatusVariable(id, type, val);
	}

	public String[] getStatusVariableNames() {
		return VAR_NAMES.toArray(new String[VAR_NAMES.size()]);
	}

	public boolean notifiesOnChange(String id) throws IllegalArgumentException {
		return false;
	}

	public boolean resetStatusVariable(String id) throws IllegalArgumentException {
		return false;
	}
}
