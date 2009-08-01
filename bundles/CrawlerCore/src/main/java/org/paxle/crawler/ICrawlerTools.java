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
package org.paxle.crawler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Iterator;

import javax.annotation.Nonnull;
import javax.annotation.WillNotClose;

import org.paxle.core.charset.ICharsetDetector;
import org.paxle.core.crypt.ICryptManager;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.core.mimetype.IMimeTypeDetector;

public interface ICrawlerTools {
	
	/**
	 * Describes an entry in a dir-listing. If the method {@link DirlistEntry#getFileURI()}
	 * returns <code>null</code>, the resulting {@link URI} of the represented entry will be
	 * constructed out of the <code>location</code> of the given {@link ICrawlerDocument}.
	 * The implementation of this method therefore is optional. 
	 */
	public static interface DirlistEntry {		
		public abstract URI getFileURI();
		public abstract String getFileName();
		public abstract long getSize();
		public abstract long getLastModified();
	}	
	
	public static interface ILimitedRateCopier {
		 public long copy(
			@Nonnull @WillNotClose InputStream is, 
			@Nonnull @WillNotClose OutputStream os, 
			final long bytes
		 ) throws IOException;
	}
	
	public ILimitedRateCopier createLimitedRateCopier(final int maxKBps);
	
	public long saveInto(
			@Nonnull ICrawlerDocument doc, 
			@Nonnull @WillNotClose InputStream is
	) throws IOException;
	
	/**
	 * Copies all data from the given {@link InputStream} to the given {@link ICrawlerDocument crawler-document}.<br />
	 * Additionally this function ...
	 * <ul>
	 * 	<li>detects the charset-encoding of the content using an {@link ICharsetDetector}</li>
	 *  <li>detects the mime-type of the content using an {@link IMimeTypeDetector}</li>
	 *  <li>generates a MD5 checksum of the content using an {@link ICryptManager}</li>
	 * </ul>
	 * 
	 * @see #copy(InputStream, OutputStream, long) for details
	 * @param doc the crawler-document the content belongs to
	 * @param is the stream to read from
	 * @param lrc the {@link LimitedRateCopier} to use to copy the data, if any, otherwise <code>null</code>
	 * @param maxFileSize max allowed content-size to copy in bytes or <code>-1</code>
	 * @return the number of copied bytes
	 * 
	 * @throws IOException if an I/O-error occures
	 * @throws ContentLengthLimitExceededException if the content-length read via the input stream exceeds the
	 * 	limit defined via maxFileSize
	 */
	public long saveInto(
			@Nonnull ICrawlerDocument doc, 
			@Nonnull @WillNotClose InputStream is, 
			@Nonnull final ILimitedRateCopier lrc, 
			final int maxFileSize
	) throws IOException, ContentLengthLimitExceededException;
	
	/**
	 * Generates a file-listing in a standard format understood by Paxle. Currently this format
	 * consists of a rudimentary HTML-page linking to the files in the list given by
	 * <code>fileListIt</code>. The resulting format of this list not yet finalized and subject
	 * to change.
	 * 
	 * @param cdoc the {@link ICrawlerDocument} to save the dir-listing to
	 * @param tfm if <code>cdoc</code> does not already contain a
	 *        {@link ICrawlerDocument#getContent() content-file}, the {@link ITempFileManager} is
	 *        used to create one.
	 * @param fileListIt the file-listing providing the required information to include in the result
	 * @param compress determines whether the content should be compressed transparently (via GZip)
	 *        to save space. Compression reduces the size of the representation of large directories
	 *        up to a sixth.
	 */
	public void saveListing(
			@Nonnull ICrawlerDocument cdoc,
			@Nonnull Iterator<DirlistEntry> fileListIt,
			boolean inclParent,
			boolean compress
	) throws IOException;	
}
