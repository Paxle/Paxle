package org.paxle.parser.iotools;

import java.io.IOException;

public interface Seekable {
	
	public abstract long seekRelative(long pos) throws IOException;
	public abstract long seekAbsolute(long pos) throws IOException; 
}
