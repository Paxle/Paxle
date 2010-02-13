/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2010 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.filter.robots.impl.store;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.paxle.filter.robots.impl.rules.RobotsTxt;

@Component(immediate=true, metatype=false, enabled=false)
@Service(IRuleStore.class)
public class FileStore implements IRuleStore {
	private static String DB_PATH = "robots-db";
	
	/**
	 * Path where {@link RobotsTxt} objects should be stored
	 */
	private File path;
	
	private FileStoreCleanupThread robotsTxtCleanupThread = null;
	
	protected void activate(ComponentContext context) {
		// getting data path
		final String dataPath = System.getProperty("paxle.data") + File.separatorChar + DB_PATH + File.separatorChar + "files";
		this.path = new File(dataPath);
		if (!this.path.exists()) this.path.mkdirs();
				
		// init a cleanup thread
		this.robotsTxtCleanupThread = new FileStoreCleanupThread(this.path);
		this.robotsTxtCleanupThread.start();
	}
	
	public RobotsTxt read(String hostPort) throws IOException {
		ObjectInputStream ois = null;
		File robotsTxtFile = null;
		try {
			// getting the host:port string
			hostPort = hostPort.replace(':', '_');			

			// getting the file
			robotsTxtFile = new File(this.path,hostPort);
			if (!robotsTxtFile.exists()) return null;

			// loading object from file
			ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(robotsTxtFile)));
			RobotsTxt robotsTxt = (RobotsTxt) ois.readObject();
			return robotsTxt;

		} catch (InvalidClassException e) {
			// just ignore this, class format has changed
			if (robotsTxtFile != null && robotsTxtFile.exists()) robotsTxtFile.delete();
			
			IOException io = new IOException("Invalid class version");
			io.initCause(e);
			throw io;
		} catch (Exception e) {
			IOException io = new IOException();
			io.initCause(e);
			throw io;
		} finally {
			if (ois != null) try { ois.close(); } catch (Exception e) {/* ingore this */}
		}
	}

	public void write(RobotsTxt robotsTxt) throws IOException {
		ObjectOutputStream oos = null;
		try {
			// getting the host:port string
			String hostPort = robotsTxt.getHostPort();
			hostPort = hostPort.replace(':', '_');

			// creating a file
			File robotsTxtFile = new File(this.path,hostPort);
			oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(robotsTxtFile)));
			oos.writeObject(robotsTxt);
			oos.flush();
		} catch (Exception e) {
			IOException io = new IOException();
			io.initCause(e);
			throw io;
		} finally {
			if (oos != null) try { oos.close(); } catch (Exception e) {/* ingore this */}
		}
	}

	public void close() throws IOException {
		if (this.robotsTxtCleanupThread != null) {
			this.robotsTxtCleanupThread.interrupt();
			this.robotsTxtCleanupThread = null;
		}
	}
	
	public int size() {
		return path.list().length;
	}
}
