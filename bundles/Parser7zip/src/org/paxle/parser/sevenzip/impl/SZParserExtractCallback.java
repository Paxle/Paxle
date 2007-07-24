package org.paxle.parser.sevenzip.impl;

import java.io.IOException;
import java.io.OutputStream;

import org.paxle.core.doc.IParserDocument;
import org.paxle.parser.SubdocOutputStream;

import SevenZip.Archive.IArchiveExtractCallback;
import SevenZip.Archive.IInArchive;
import SevenZip.Archive.SevenZipEntry;

/**
 * This class is the call-back for the 7zip-extraction routine. It provides an
 * {@link SubdocOutputStream} which takes the extracted data and parses it into
 * a sub-document of the given parser document.
 * @see SevenZip.ArchiveExtractCallback
 */
public class SZParserExtractCallback implements IArchiveExtractCallback {
	
	private final IParserDocument pdoc;
	private final IInArchive handler;
	private String current = null;
	private OutputStream os = null;
	
	public SZParserExtractCallback(IParserDocument pdoc, IInArchive handler) {
		this.pdoc = pdoc;
		this.handler = handler;
	}
	
	public int PrepareOperation(int arg0) {
        return 0;
	}
	
	public int GetStream(int index, OutputStream[] oss, int askExtractMode) throws IOException {
		SevenZipEntry item = this.handler.getEntry(index);
		this.current = item.getName();
		this.os = oss[0] = (item.isDirectory()) ? null : new SubdocOutputStream(this.pdoc, this.current);
		return 0;
	}
	
	public int SetCompleted(long arg0) {
		return 0;
	}
	
	public int SetOperationResult(int arg0) throws IOException {
		if (this.os != null)
			this.os.close();
		
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
