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
package org.paxle.core.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import junit.framework.TestCase;
import junitx.framework.FileAssert;

public class IOToolsTest extends TestCase {
	private static final int TESTFILE_INPUT = 0;
	private static final int TESTFILE_OUTPUT = 1;

	private IIOTools iotools;
	
	protected void setUp() throws Exception {
		super.setUp();
		this.iotools = new org.paxle.core.io.impl.IOTools();
	}

	private File createTempData(int size) throws IOException {
		BufferedOutputStream fout = null;
		try {
			// create a tempfile
			File tempFile = File.createTempFile("IOToolsTestIn", ".tmp");
			tempFile.deleteOnExit();

			// open stream and write data to it
			fout = new BufferedOutputStream(new FileOutputStream(tempFile));
			Random rand = new Random();

			while (size > 0) {
				int chunkSize = Math.min(size, 512);
				byte[] chunk = new byte[chunkSize];
				rand.nextBytes(chunk);
				fout.write(chunk);
				fout.flush();
				size -= chunkSize;
			}			
			fout.close();			
			
			return tempFile;
		} finally {
			if (fout != null) try { fout.close(); } catch (Exception e) {/* ignore this */}
		}
	}		
	
	private File[] createInOutFiles(int inputFileSize) throws IOException {
		// create input-file
		File inputFile = this.createTempData(inputFileSize);
		assertEquals(inputFileSize, inputFile.length());	
		
		// create empty output-file
		File outputFile = File.createTempFile("IOToolsTestOut", ".tmp");
		return new File[]{inputFile,outputFile};
	}
	
	public void testCopy() throws IOException {
		File[] testFiles = null;
		try {
			// create input-file
			int size = new Random().nextInt(1024*20);
			testFiles = this.createInOutFiles(size);
			
			// create streams
			InputStream fIn = new BufferedInputStream(new FileInputStream(testFiles[TESTFILE_INPUT]));
			OutputStream fOut = new BufferedOutputStream(new FileOutputStream(testFiles[TESTFILE_OUTPUT]));
			
			// copy data
			this.iotools.copy(fIn, fOut);
			fIn.close();
			fOut.close();
			
			assertEquals(size, testFiles[TESTFILE_OUTPUT].length());
			FileAssert.assertBinaryEquals(testFiles[TESTFILE_INPUT], testFiles[TESTFILE_OUTPUT]);
		} finally {
			if (testFiles != null) {
				for (File testFile : testFiles) {
					try { testFile.delete(); } catch (Exception e) {/* ignore this */}
				}
			}
		}
	}
	
	public void testCopyWithLimit() throws IOException {
		File[] testFiles = null;
		try {
			// create input-file
			Random rand = new Random();
			int size = rand.nextInt(1024*20);
			int limit = rand.nextInt(size);
			testFiles = this.createInOutFiles(size);
			
			// create streams
			InputStream fIn = new BufferedInputStream(new FileInputStream(testFiles[TESTFILE_INPUT]));
			OutputStream fOut = new BufferedOutputStream(new FileOutputStream(testFiles[TESTFILE_OUTPUT]));
			
			// copy data
			this.iotools.copy(fIn, fOut, limit);
			fIn.close();
			fOut.close();
			
			assertEquals(limit, testFiles[TESTFILE_OUTPUT].length());
			System.out.println(String.format("TotalSize=%d, Limit=%d", size,limit));
		} finally {
			if (testFiles != null) {
				for (File testFile : testFiles) {
					try { testFile.delete(); } catch (Exception e) {/* ignore this */}
				}
			}
		}
	}
	
	public void testCopyWithLimitKBps() throws IOException {
		File[] testFiles = null;
		try {
			int size = 1024*3;
			int limitKBps = 1;
			long expectedTime = (size / (1 << 10)) *1000;
			
			// create input-file
			testFiles = this.createInOutFiles(size);
			
			// create streams
			InputStream fIn = new BufferedInputStream(new FileInputStream(testFiles[TESTFILE_INPUT]));
			OutputStream fOut = new BufferedOutputStream(new FileOutputStream(testFiles[TESTFILE_OUTPUT]));
			
			long start = System.currentTimeMillis();
			this.iotools.copy(fIn, fOut, size, limitKBps);
			long end = System.currentTimeMillis();
			
			fIn.close();
			fOut.close();
			
			// check assertions
			assertEquals(size, testFiles[TESTFILE_OUTPUT].length());
			FileAssert.assertBinaryEquals(testFiles[TESTFILE_INPUT], testFiles[TESTFILE_OUTPUT]);
			assertTrue(String.format("Copying took %d ms but should be at least %d",end-start,expectedTime), expectedTime <= end - start);
			
			System.out.println(String.format("Copying %d kb took %d ms.",size>>10,end-start));
		} finally {
			if (testFiles != null) {
				for (File testFile : testFiles) {
					try { testFile.delete(); } catch (Exception e) {/* ignore this */}
				}
			}
		}
	}
}
