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

package org.paxle.crawler.fs.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.paxle.core.charset.ACharsetDetectorInputStream;
import org.paxle.core.charset.ICharsetDetector;
import org.paxle.core.doc.CrawlerDocument;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.core.mimetype.IMimeTypeDetector;
import org.paxle.core.queue.ICommandProfile;

import org.paxle.crawler.CrawlerContext;
import org.paxle.crawler.CrawlerTools;
import org.paxle.crawler.CrawlerTools.FileListIt;
import org.paxle.crawler.fs.IFsCrawler;

/**
 * @scr.component
 * @scr.service interface="org.paxle.crawler.ISubCrawler"
 * @scr.property name="Protocol" value="file"
 */
public class FsCrawler implements IFsCrawler {
	
	private final Log logger = LogFactory.getLog(FsCrawler.class);
	
	public ICrawlerDocument request(URI location) {
		
		final ICrawlerDocument cdoc = new CrawlerDocument();
		
		final CrawlerContext ctx = CrawlerContext.getCurrentContext();
		if (ctx == null) {
			cdoc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE,
					"Cannot access CrawlerContext from " + Thread.currentThread().getName());
			return cdoc;
		}
		final ITempFileManager tfm = ctx.getTempFileManager();
		if (tfm == null) {
			cdoc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE,
					"Cannot access ITempFileMananger from " + Thread.currentThread().getName());
			return cdoc;
		}
		
		final ICommandProfile cmdProfile = ctx.getCommandProfile();
		boolean omitHidden = true;
		String readMode = VAL_READ_MODE_STD;
		if (cmdProfile != null) {
			Serializable val;
			if ((val = cmdProfile.getProperty(PROP_VALIDATE_NOT_HIDDEN)) != null)
				omitHidden = ((Boolean)val).booleanValue();
			if ((val = cmdProfile.getProperty(PROP_READ_MODE)) != null)
				readMode = (String)val;
		}
		
		ICrawlerDocument.Status status = ICrawlerDocument.Status.OK;
		String err = null;
		final File file = new File(location);
		
		if (!file.exists()) {
			err = "File not found";
			status = ICrawlerDocument.Status.NOT_FOUND;
		} else if (!file.canRead()) {
			err = "Read permission denied";
			status = ICrawlerDocument.Status.UNKNOWN_FAILURE;/*		java 1.6
		} else if (file.isDirectory() && !file.canExecute()) {
			err = "Permission to enter directory denied";
			status = ICrawlerDocument.Status.UNKNOWN_FAILURE;*/
		} else if (omitHidden && file.isHidden()) {
			err = "Hidden";
			status = ICrawlerDocument.Status.UNKNOWN_FAILURE;
		}
		
		cdoc.setStatus(status);
		if (err != null) {
			logger.warn(String.format("Error crawling %s: %s", location, err));
			cdoc.setStatusText(err);
			return cdoc;
		}
		
		cdoc.setCrawlerDate(new Date());
		cdoc.setLastModDate(new Date(file.lastModified()));
		cdoc.setLocation(location);
		
		if (file.isDirectory()) try {
			CrawlerTools.saveInto(cdoc, generateListing(file, location, omitHidden));
		} catch (IOException e) {
			final String msg = String.format("Error generating dir-listing for '%s': %s", location, e.getMessage());
			cdoc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE, msg);
			logger.error(msg, e);
			return cdoc;
		} else {
			cdoc.setContent(generateContentFile(readMode, file, cdoc, tfm));
		}
		
		return cdoc;
	}
	
	static InputStream generateListing(final File dir, final URI location, final boolean omitHidden) {
		return CrawlerTools.generateListing(new FileListIt<File>(dir.listFiles()) {
			@Override protected boolean isDisallowed() { return omitHidden && current.isHidden(); }
			@Override public String getFileName() { return current.getName(); }
			@Override public long getLastModified() { return current.lastModified(); }
			@Override public long getSize() { return current.length(); }
		}, location);
	}
	
	private File generateContentFile(final String readMode, final File file, final ICrawlerDocument cdoc, ITempFileManager tfm) {
		final File content;
		if (VAL_READ_MODE_DIRECT.equals(readMode)) {
			// TODO: prevent content from being deleted
			content = file;
		} else if (VAL_READ_MODE_STD.equals(readMode)) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(file);
				CrawlerTools.saveInto(cdoc, fis);
				content = cdoc.getContent();
			} catch (IOException e) {
				logger.error(String.format("Error saving '%s': %s", cdoc.getLocation(), e.getMessage()), e);
				cdoc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE, e.getMessage());
				return null;
			} finally {
				if (fis != null) try { fis.close(); } catch (IOException e) {/* ignore */}
			}
		} else if (VAL_READ_MODE_CHANNELED.equals(readMode) ||
				VAL_READ_MODE_CHANNELED_FSYNC.equals(readMode)) {
			FileInputStream fis = null;
			FileOutputStream fos = null;
			File out = null;
			try {
				out = tfm.createTempFile();
				fis = new FileInputStream(file);
				fos = new FileOutputStream(out);
				
				final FileChannel in_fc = fis.getChannel();
				final FileChannel out_fc = fos.getChannel();
				
				long txed = 0L;
				while (txed < in_fc.size())
					txed += in_fc.transferTo(txed, in_fc.size() - txed, out_fc);
				
				if (VAL_READ_MODE_CHANNELED_FSYNC.equals(readMode))
					out_fc.force(false);
				out_fc.close();
				
				try {
					detectFormats(cdoc, fis);
				} catch (IOException ee) {
					logger.warn(String.format("Error detecting format of '%s': %s", cdoc.getLocation(), ee.getMessage()));
				}
			} catch (IOException e) {
				logger.error(String.format("Error copying '%s' to '%s': %s", cdoc.getLocation(), out, e.getMessage()), e);
				cdoc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE, e.getMessage());
				return null;
			} finally {
				if (fis != null) try { fis.close(); } catch (IOException e) {/* ignore */}
				if (fos != null) try { fos.close(); } catch (IOException e) {/* ignore */}
			}
			content = out;
		} else {
			throw new RuntimeException("switch statement does not cover read-mode: " + readMode);
		}
		return content;
	}
	
	private void detectFormats(final ICrawlerDocument cdoc, InputStream is) throws IOException {
		final CrawlerContext ctx = CrawlerContext.getCurrentContext();
		final ICharsetDetector chardet = ctx.getCharsetDetector();
		final IMimeTypeDetector mimedet = ctx.getMimeTypeDetector();
		if (chardet == null && mimedet == null)
			return;
		
		ACharsetDetectorInputStream acis = null;
		if (chardet != null)
			is = acis = chardet.createInputStream(is);
		
		String mimeType = null;
		String charset = null;
		
		int bufsize = 10240;					// needs to be big enough for the mime-type detector to detect it in one pass
		if (cdoc.getSize() < bufsize)
			bufsize = (int)cdoc.getSize();
		final byte[] buf = new byte[bufsize];
		
		int read = 0;
		boolean mimeTypeTested = false;
		while ((read = is.read(buf)) != -1) {
			if (mimedet != null && !mimeTypeTested) {
				byte[] test_buf = buf;
				if (read < bufsize) {
					test_buf = new byte[read];
					System.arraycopy(buf, 0, test_buf, 0, read);
				}
				
				try {
					mimeType = mimedet.getMimeType(test_buf, "FS-Crawler");
				} catch (Exception e) {
					logger.warn(String.format("Error detecting mime-type of '%s': %s", cdoc.getLocation(), e.getMessage()));
				}
				
				mimeTypeTested = true;
			}
			
			if (charset == null && chardet != null && acis.charsetDetected())
				charset = acis.getCharset();
			
			if ((mimedet == null || mimeType != null) && (chardet == null || charset != null))
				break;
		}
		cdoc.setCharset(charset);
		cdoc.setMimeType(mimeType);
	}
}
