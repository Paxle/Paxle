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

package org.paxle.parser.sevenzip.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import org.paxle.core.charset.ICharsetDetector;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.parser.iotools.ParserDocOutputStream;
import org.paxle.parser.iotools.SubParserDocOutputStream;

import SevenZip.Archive.IArchiveExtractCallback;
import SevenZip.Archive.IInArchive;
import SevenZip.Archive.SevenZipEntry;

/**
 * This class is the call-back for the 7zip-extraction routine. It provides an
 * {@link ParserDocOutputStream} which takes the extracted data and parses it into
 * a sub-document of the given parser document.
 * @see SevenZip.ArchiveExtractCallback
 */
public class SZParserExtractCallback implements IArchiveExtractCallback {
	
	private final URI location;
	private final IParserDocument pdoc;
	private final IInArchive handler;
	private final ITempFileManager tfm;
	private final ICharsetDetector cd;
	private String current = null;
	
	public SZParserExtractCallback(final URI location, IParserDocument pdoc, IInArchive handler, ITempFileManager tfm, ICharsetDetector cd) {
		this.location = location;
		this.pdoc = pdoc;
		this.handler = handler;
		this.tfm = tfm;
		this.cd = cd;
	}
	
	public int PrepareOperation(int arg0) {
        return 0;
	}
	
	public int GetStream(int index, OutputStream[] oss, int askExtractMode) throws IOException {
		SevenZipEntry item = this.handler.getEntry(index);
		this.current = item.getName();
		oss[0] = (item.isDirectory()) ? null : new SubParserDocOutputStream(
				this.tfm,
				this.cd,
				this.pdoc,
				location,
				this.current,
				item.getSize());
		return 0;
	}
	
	public int SetCompleted(long arg0) {
		return 0;
	}
	
	public int SetOperationResult(int arg0) throws IOException {
		/* the output-stream is closed by SevenZip.Archive.Common.OutStreamWithCRC.ReleaseStream,
		 * which is called here-after.
		if (this.os != null)
			this.os.close();*/
		
		if (arg0 != IInArchive.NExtract_NOperationResult_kOK) {
			switch(arg0) {
				case IInArchive.NExtract_NOperationResult_kUnSupportedMethod:
					throw new IOException("Unsupported Method");
				case IInArchive.NExtract_NOperationResult_kCRCError:
					throw new IOException("CRC Failed");
				case IInArchive.NExtract_NOperationResult_kDataError:
					throw new IOException("Data Error");
				default:
					// throw new IOException("Unknown Error");
			}
		}
		return 0;
	}
	
	public int SetTotal(long arg0) {
		return 0;
	}
	
	public String getCurrentFilePath() {
		return this.current;
	}
}
