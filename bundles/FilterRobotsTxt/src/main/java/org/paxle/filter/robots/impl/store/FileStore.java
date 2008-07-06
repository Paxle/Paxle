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

import org.paxle.filter.robots.impl.rules.RobotsTxt;

public class FileStore implements IRuleStore {

	/**
	 * Path where {@link RobotsTxt} objects should be stored
	 */
	private final File path;
	
	public FileStore(File path) {
		this.path = path;
		if (!this.path.exists()) this.path.mkdirs();
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
		// nothing todo here
	}

}