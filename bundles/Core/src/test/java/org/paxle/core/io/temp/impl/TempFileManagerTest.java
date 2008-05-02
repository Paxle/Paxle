package org.paxle.core.io.temp.impl;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

public class TempFileManagerTest extends TestCase {

	private TempFileManager manager = null;

	protected void setUp() throws Exception {
		super.setUp();
		this.manager = new TempFileManager();
	}

	public void testCreateTempFile() throws IOException {
		File temp = this.manager.createTempFile();
		assertNotNull(temp);
		assertTrue(temp.exists());
		assertTrue(temp.canRead());
		assertTrue(temp.canWrite());
	}

	public void testCreateTempFileInTempDir() throws IOException {
		File temp = null;
		try {
			File tempDir = new File("target/test");
			FSTempDir dir = new FSTempDir(tempDir);
			this.manager.setTempDirFor(dir, this.getClass().getName());

			temp = this.manager.createTempFile();
			assertNotNull(temp);
			assertTrue(temp.exists());
			assertTrue(temp.canRead());
			assertTrue(temp.canWrite());
			assertTrue(temp.getCanonicalPath().startsWith(tempDir.getCanonicalPath()));
		} finally {
			if (temp != null) try { temp.delete(); } catch (Exception e){/* ignore this */}
		}
	}

	public void testReleaseTempFile() throws IOException {
		File temp = null;
		try {
			temp = this.manager.createTempFile();		
			assertNotNull(temp);
			assertTrue(temp.exists());

			this.manager.releaseTempFile(temp);
			assertFalse(temp.exists());
		} finally {
			if (temp != null) try { temp.delete(); } catch (Exception e){/* ignore this */}
		}
	}

	public void testReleaseTempFileInTempDir() throws IOException {
		File tempDir = new File("target/test");
		FSTempDir dir = new FSTempDir(tempDir);
		this.manager.setTempDirFor(dir, this.getClass().getName());

		File temp = this.manager.createTempFile();		
		assertNotNull(temp);
		assertTrue(temp.exists());

		this.manager.releaseTempFile(temp);
		assertFalse(temp.exists());
	}

	public void testReleaseUnknownTempFile() throws IOException {
		File temp = File.createTempFile(this.getClass().getName(), ".tmp");			
		this.manager.releaseTempFile(temp);
		assertFalse(temp.exists());
	}

	/**
	 * TODO: this does now work jet
	 * XXX: should this work?
	 */
	public void _testReleaseNullTempFile() throws IOException {
		File temp = null;		
		this.manager.releaseTempFile(temp);
	}
}
